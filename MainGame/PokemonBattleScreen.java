package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PokemonBattleScreen implements Screen {

    private Main game;
    private Stage stage;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    // --- SAVE DATA VARIABLES ---
    private String playerName;
    private int slotIndex;
    private float startX;
    private float startY;
    private int startLevel;

    public enum BattleState {
        START_WAIT, PLAYER_TURN, ANIMATING_PLAYER,
        ENEMY_TURN, ANIMATING_ENEMY, WON, LOST
    }
    private BattleState currentState = BattleState.START_WAIT;
    private float stateTimer = 0;
    private float animationTime = 0;

    private int playerMaxHp = 100;
    private int playerHp = 100;
    private int enemyMaxHp = 300;
    private int enemyHp = 5;

    private boolean damageApplied = false;
    private boolean isPotion = false;
    private int pendingDamage = 0;
    private String pendingMessage = "";

    private Label dialogLabel;
    private Table actionMenu;
    private Skin skin;

    private Texture bgTexture;

    private Animation<TextureRegion> pikaIdleAnim, pikaTackleAnim, pikaThunderAnim, pikaHurtAnim, pikaFaintAnim;
    private Animation<TextureRegion> charIdleAnim, charClawAnim, charFireAnim, charHurtAnim, charFaintAnim;

    private Animation<TextureRegion> currentPikaAnimation;
    private Animation<TextureRegion> currentCharAnimation;

    public PokemonBattleScreen(Main game, String playerName, int slotIndex, float startX, float startY, int startLevel) {
        this.game = game;
        this.playerName = playerName;
        this.slotIndex = slotIndex;
        this.startX = startX;
        this.startY = startY;
        this.startLevel = startLevel;

        this.viewport = new ExtendViewport(800, 480);
        this.stage = new Stage(viewport, game.batch);
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.font.getData().setScale(1.5f);

        Gdx.input.setInputProcessor(stage);

        createPlaceholderTextures();
        createDefaultSkin();
        setupUI();

        dialogLabel.setText("BATMAN sent out CHARIZARD!\nGo! PIKACHU!");
        actionMenu.setVisible(false);

        AudioManager.pokemonFightMusic.setLooping(true);
        AudioManager.pokemonFightMusic.setVolume(0.3f);
        AudioManager.pokemonFightMusic.play();
    }

    private void setupUI() {
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.bottom();

        Table dialogTable = new Table();
        dialogTable.setBackground(skin.newDrawable("white", Color.DARK_GRAY));

        Table innerDialog = new Table();
        innerDialog.setBackground(skin.newDrawable("white", Color.WHITE));

        dialogLabel = new Label("", skin, "blackText");
        dialogLabel.setWrap(true);
        dialogLabel.setAlignment(Align.topLeft);
        innerDialog.add(dialogLabel).expand().fill().pad(15);

        dialogTable.add(innerDialog).expand().fill().pad(4);

        actionMenu = new Table();
        actionMenu.setBackground(skin.newDrawable("white", Color.DARK_GRAY));

        Table innerMenu = new Table();
        innerMenu.setBackground(skin.newDrawable("white", Color.WHITE));

        TextButton btnTackle = new TextButton("Tackle", skin);
        TextButton btnThunder = new TextButton("Thunderbolt", skin);
        TextButton btnHeal = new TextButton("Potion", skin);
        TextButton btnSarcasm = new TextButton("Sarcasm", skin);

        btnTackle.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { playerAttack("Tackle", 15); }});
        btnThunder.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { playerAttack("Thunderbolt", 30); }});
        btnHeal.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { playerAttack("Potion", -40); }});
        btnSarcasm.addListener(new ClickListener() { @Override public void clicked(InputEvent event, float x, float y) { playerAttack("Sarcasm", 5); }});

        innerMenu.add(btnTackle).expand().fill().pad(5);
        innerMenu.add(btnThunder).expand().fill().pad(5).row();
        innerMenu.add(btnHeal).expand().fill().pad(5);
        innerMenu.add(btnSarcasm).expand().fill().pad(5);

        actionMenu.add(innerMenu).expand().fill().pad(4);

        rootTable.add(dialogTable).width(480).height(120).pad(10);
        rootTable.add(actionMenu).width(300).height(120).pad(10);

        stage.addActor(rootTable);
    }

    private void playerAttack(String moveName, int value) {
        if (currentState != BattleState.PLAYER_TURN) return;

        actionMenu.setVisible(false);
        currentState = BattleState.ANIMATING_PLAYER;
        stateTimer = 0;
        animationTime = 0;
        damageApplied = false;
        isPotion = false;
        pendingDamage = value;

        if (moveName.equals("Potion")) {
            isPotion = true;
            playerHp = Math.min(playerMaxHp, playerHp + Math.abs(value));
            dialogLabel.setText("You used a Potion on PIKACHU!\nRecovered 40 HP.");
        } else {
            AudioManager.pokemonPlayerAttack.play();
            dialogLabel.setText("PIKACHU used " + moveName + "!");
            pendingMessage = "It dealt " + value + " damage.";

            if (moveName.equals("Thunderbolt")) {
                currentPikaAnimation = pikaThunderAnim;
            } else {
                currentPikaAnimation = pikaTackleAnim;
            }
            currentCharAnimation = charIdleAnim;
        }
    }

    private void enemyAttack() {
        currentState = BattleState.ANIMATING_ENEMY;
        stateTimer = 0;
        animationTime = 0;
        damageApplied = false;

        AudioManager.pokemonPlayerAttack.play();

        int moveChoice = MathUtils.random(1, 2);
        if (moveChoice == 1) {
            pendingDamage = 35;
            dialogLabel.setText("CHARIZARD used Fire Blast!");
            pendingMessage = "It's super effective!";
            currentCharAnimation = charFireAnim;
        } else {
            pendingDamage = 15;
            dialogLabel.setText("CHARIZARD used Dragon Claw!");
            pendingMessage = "A critical hit!";
            currentCharAnimation = charClawAnim;
        }
        currentPikaAnimation = pikaIdleAnim;
    }

    private boolean updateLogic(float delta) {
        stateTimer += delta;
        animationTime += delta;

        switch (currentState) {
            case START_WAIT:
                if (stateTimer > 3.0f) {
                    dialogLabel.setText("What will PIKACHU do?");
                    actionMenu.setVisible(true);
                    currentState = BattleState.PLAYER_TURN;
                }
                break;

            case ANIMATING_PLAYER:
                if (isPotion) {
                    if (stateTimer > 2.0f) {
                        currentState = BattleState.ENEMY_TURN;
                        stateTimer = 0;
                    }
                    break;
                }

                if (!damageApplied) {
                    if (currentPikaAnimation.isAnimationFinished(animationTime)) {
                        damageApplied = true;
                        animationTime = 0;
                        stateTimer = 0;
                        currentPikaAnimation = pikaIdleAnim;

                        AudioManager.pokemonPlayerDamage.play();
                        enemyHp = Math.max(0, enemyHp - pendingDamage);

                        if (enemyHp <= 0) {
                            currentCharAnimation = charFaintAnim;
                            dialogLabel.setText("CHARIZARD fainted! You defeated BATMAN!");
                        } else {
                            currentCharAnimation = charHurtAnim;
                            dialogLabel.setText(pendingMessage);
                        }
                    }
                } else {
                    if (enemyHp > 0 && currentCharAnimation == charHurtAnim && currentCharAnimation.isAnimationFinished(animationTime)) {
                        currentCharAnimation = charIdleAnim;
                    }

                    if (stateTimer > 1.5f) {
                        if (enemyHp <= 0) {
                            if (stateTimer > 2.5f) {
                                currentState = BattleState.WON;
                                stateTimer = 0;
                            }
                        } else {
                            currentState = BattleState.ENEMY_TURN;
                            stateTimer = 0;
                        }
                    }
                }
                break;

            case ENEMY_TURN:
                if (stateTimer > 1.0f) {
                    enemyAttack();
                }
                break;

            case ANIMATING_ENEMY:
                if (!damageApplied) {
                    if (currentCharAnimation.isAnimationFinished(animationTime)) {
                        damageApplied = true;
                        animationTime = 0;
                        stateTimer = 0;
                        currentCharAnimation = charIdleAnim;

                        AudioManager.pokemonPlayerDamage.play();
                        playerHp = Math.max(0, playerHp - pendingDamage);

                        if (playerHp <= 0) {
                            currentPikaAnimation = pikaFaintAnim;
                            dialogLabel.setText("PIKACHU fainted! You blacked out!");
                        } else {
                            currentPikaAnimation = pikaHurtAnim;
                            dialogLabel.setText(pendingMessage);
                        }
                    }
                } else {
                    if (playerHp > 0 && currentPikaAnimation == pikaHurtAnim && currentPikaAnimation.isAnimationFinished(animationTime)) {
                        currentPikaAnimation = pikaIdleAnim;
                    }

                    if (stateTimer > 1.5f) {
                        if (playerHp <= 0) {
                            if (stateTimer > 2.5f) {
                                currentState = BattleState.LOST;
                                stateTimer = 0;
                            }
                        } else {
                            dialogLabel.setText("What will PIKACHU do?");
                            actionMenu.setVisible(true);
                            currentState = BattleState.PLAYER_TURN;
                            stateTimer = 0;
                        }
                    }
                }
                break;

            case WON:
                if (stateTimer > 2.0f) {
                    // FIXED: Re-loads PlayScreen in post-battle mode so AvengersCutscene triggers[cite: 22]
                    game.setScreen(new PlayScreen(game, playerName, slotIndex, startX, startY, startLevel, true));
                    dispose();
                    return false;
                }
                break;

            case LOST:
                if (stateTimer > 2.0f) {
                    // FIXED: Plays credits roll when losing the Pokemon fight[cite: 27]
                    game.setScreen(new EndCreditsScreen(game));
                    dispose();
                    return false;
                }
                break;
        }
        return true;
    }

    @Override
    public void render(float delta) {
        if (!updateLogic(delta)) {
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        TextureRegion pikaFrame = currentPikaAnimation.getKeyFrame(animationTime);
        TextureRegion charFrame = currentCharAnimation.getKeyFrame(animationTime);

        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();

        game.batch.draw(bgTexture, 0, 140, 800, 340);

        game.batch.draw(charFrame, 510, 240, 200, 200);
        game.batch.draw(pikaFrame, 80, 150, 180, 180);

        game.batch.end();

        drawHealthBars();
        stage.act(delta);
        stage.draw();
    }

    private void drawHealthBars() {
        game.batch.begin();
        font.setColor(Color.BLACK);
        font.draw(game.batch, "CHARIZARD  Lv50", 60, 440);
        font.draw(game.batch, "PIKACHU  Lv45", 480, 220);
        game.batch.end();

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(60, 400, 250, 15);
        float enemyHpPct = (float) enemyHp / enemyMaxHp;
        shapeRenderer.setColor(enemyHpPct > 0.5f ? Color.GREEN : (enemyHpPct > 0.2f ? Color.YELLOW : Color.RED));
        shapeRenderer.rect(60, 400, 250 * enemyHpPct, 15);

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(480, 180, 250, 15);
        float playerHpPct = (float) playerHp / playerMaxHp;
        shapeRenderer.setColor(playerHpPct > 0.5f ? Color.GREEN : (playerHpPct > 0.2f ? Color.YELLOW : Color.RED));
        shapeRenderer.rect(480, 180, 250 * playerHpPct, 15);

        shapeRenderer.end();
    }

    private void createDefaultSkin() {
        skin = new Skin();
        skin.add("default", new BitmapFont());

        BitmapFont blackFont = new BitmapFont();
        blackFont.getData().setScale(1.2f);
        skin.add("blackFont", blackFont);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.newDrawable("white", Color.LIGHT_GRAY);
        btnStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
        btnStyle.over = skin.newDrawable("white", Color.WHITE);
        btnStyle.font = skin.getFont("blackFont");
        btnStyle.fontColor = Color.BLACK;
        skin.add("default", btnStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("blackFont");
        labelStyle.fontColor = Color.BLACK;
        skin.add("blackText", labelStyle);
    }

    private void createPlaceholderTextures() {
        bgTexture = game.assets.manager.get(GameAssetManager.POKEMON_BG, Texture.class);

        pikaIdleAnim    = GameAssetManager.getAnimation(GameAssetManager.PIKA_IDLE_PREFIX, 6, 0.25f, Animation.PlayMode.LOOP, "%d");
        pikaTackleAnim  = GameAssetManager.getAnimation(GameAssetManager.PIKA_TACKLE_PREFIX, 7, 0.15f, Animation.PlayMode.NORMAL, "%d");
        pikaThunderAnim = GameAssetManager.getAnimation(GameAssetManager.PIKA_THUNDER_PREFIX, 8, 0.15f, Animation.PlayMode.NORMAL, "%d");
        pikaHurtAnim    = GameAssetManager.getAnimation(GameAssetManager.PIKA_FAINT_PREFIX, 3, 0.20f, Animation.PlayMode.NORMAL, "%d");
        pikaFaintAnim   = GameAssetManager.getAnimation(GameAssetManager.PIKA_FAINT_PREFIX, 5, 0.25f, Animation.PlayMode.NORMAL, "%d");

        charIdleAnim    = GameAssetManager.getAnimation(GameAssetManager.CHAR_IDLE_PREFIX, 6, 0.25f, Animation.PlayMode.LOOP, "%d");
        charClawAnim    = GameAssetManager.getAnimation(GameAssetManager.CHAR_CLAW_PREFIX, 5, 0.15f, Animation.PlayMode.NORMAL, "%d");
        charFireAnim    = GameAssetManager.getAnimation(GameAssetManager.CHAR_FIRE_PREFIX, 8, 0.15f, Animation.PlayMode.NORMAL, "%d");
        charHurtAnim    = GameAssetManager.getAnimation(GameAssetManager.CHAR_FAINT_PREFIX, 3, 0.20f, Animation.PlayMode.NORMAL, "%d");
        charFaintAnim   = GameAssetManager.getAnimation(GameAssetManager.CHAR_FAINT_PREFIX, 5, 0.25f, Animation.PlayMode.NORMAL, "%d");

        currentPikaAnimation = pikaIdleAnim;
        currentCharAnimation = charIdleAnim;
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void show() { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        AudioManager.pokemonFightMusic.stop();
        AudioManager.pokemonPlayerAttack.stop();
        AudioManager.pokemonPlayerDamage.stop();

        stage.dispose();
        shapeRenderer.dispose();
        font.dispose();
        if (skin != null) skin.dispose();
    }
}
