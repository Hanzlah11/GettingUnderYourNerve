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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PokemonBattleScreen implements Screen {

    private Main game;
    private Stage stage;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    public enum BattleState {
        START_WAIT, PLAYER_TURN, ANIMATING_PLAYER,
        ENEMY_TURN, ANIMATING_ENEMY, WON, LOST
    }
    private BattleState currentState = BattleState.START_WAIT;
    private float stateTimer = 0;

    private int playerMaxHp = 100;
    private int playerHp = 100;
    private int enemyMaxHp = 300; // Was 150
    private int enemyHp = 0;

    private Label dialogLabel;
    private Table actionMenu;
    private Skin skin;

    private Texture bgTexture;
    private Texture pikachuSprite;
    private Texture charizardSprite;

    public PokemonBattleScreen(Main game) {
        this.game = game;
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

        if (moveName.equals("Potion")) {
            playerHp = Math.min(playerMaxHp, playerHp + Math.abs(value));
            dialogLabel.setText("You used a Potion on PIKACHU!\nRecovered 40 HP.");
        } else {
            // --- NEW: Play Player Attack Sound ---
            AudioManager.pokemonPlayerAttack.play();

            enemyHp = Math.max(0, enemyHp - value);
            dialogLabel.setText("PIKACHU used " + moveName + "!\nIt dealt " + value + " damage.");
        }
    }

    private void enemyAttack() {
        currentState = BattleState.ANIMATING_ENEMY;
        stateTimer = 0;

        // Plays immediately when Charizard strikes
        AudioManager.pokemonPlayerDamage.play();

        int moveChoice = MathUtils.random(1, 2);
        if (moveChoice == 1) {
            // Nerfed from 55 to 35
            playerHp = Math.max(0, playerHp - 35);
            dialogLabel.setText("CHARIZARD used Fire Blast!\nIt's super effective!");
        } else {
            // Nerfed from 30 to 15
            playerHp = Math.max(0, playerHp - 15);
            dialogLabel.setText("CHARIZARD used Dragon Claw!\nA critical hit!");
        }
    }

    // --- FIX: CHANGED TO BOOLEAN SO WE CAN STOP RENDERING ---
    private boolean updateLogic(float delta) {
        stateTimer += delta;

        switch (currentState) {
            case START_WAIT:
                if (stateTimer > 3.0f) {
                    dialogLabel.setText("What will PIKACHU do?");
                    actionMenu.setVisible(true);
                    currentState = BattleState.PLAYER_TURN;
                }
                break;

            case ANIMATING_PLAYER:
                if (stateTimer > 2.5f) {
                    if (enemyHp <= 0) {
                        dialogLabel.setText("CHARIZARD fainted! You defeated BATMAN!");
                        currentState = BattleState.WON;
                        stateTimer = 0;
                    } else {
                        currentState = BattleState.ENEMY_TURN;
                        stateTimer = 0;
                    }
                }
                break;

            case ENEMY_TURN:
                if (stateTimer > 1.0f) {
                    enemyAttack();
                }
                break;

            case ANIMATING_ENEMY:
                if (stateTimer > 2.5f) {
                    if (playerHp <= 0) {
                        dialogLabel.setText("PIKACHU fainted! You blacked out!");
                        currentState = BattleState.LOST;
                        stateTimer = 0;
                    } else {
                        dialogLabel.setText("What will PIKACHU do?");
                        actionMenu.setVisible(true);
                        currentState = BattleState.PLAYER_TURN;
                    }
                }
                break;

            case WON:
                if (stateTimer > 3.0f) {
                    game.setScreen(new PlayScreen(game, 3, true)); // Triggers Avengers Phase!
                    dispose();
                    return false; // <--- STOPS CRASH
                }
                break;

            case LOST:
                if (stateTimer > 3.0f) {
                    Gdx.app.exit();
                    return false; // <--- STOPS CRASH
                }
                break;
        }
        return true;
    }

    @Override
    public void render(float delta) {
        // --- FIX: IF UPDATE LOGIC RETURNS FALSE, STOP IMMEDIATELY ---
        if (!updateLogic(delta)) {
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();
        game.batch.draw(bgTexture, 0, 140, 800, 340);
        game.batch.draw(charizardSprite, 550, 260, 160, 160);
        game.batch.draw(pikachuSprite, 80, 150, 160, 160);
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
        // We still need a generated background for now, so keep this part
        Pixmap bgPix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPix.setColor(new Color(0.8f, 0.9f, 0.8f, 1));
        bgPix.fill();
        bgTexture = new Texture(bgPix);
        bgPix.dispose(); // Always dispose Pixmaps after making a Texture!

        // FETCH REAL IMAGES FROM ASSET MANAGER
        pikachuSprite = game.assets.manager.get(GameAssetManager.PIKACHU_SPRITE, Texture.class);
        charizardSprite = game.assets.manager.get(GameAssetManager.CHARIZARD_SPRITE, Texture.class);
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void show() { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        // --- NEW: Stop battle music and sounds ---
        AudioManager.pokemonFightMusic.stop();
        AudioManager.pokemonPlayerAttack.stop();
        AudioManager.pokemonPlayerDamage.stop();

        stage.dispose();
        shapeRenderer.dispose();
        font.dispose();
        bgTexture.dispose();
        if (skin != null) skin.dispose();
    }
}
