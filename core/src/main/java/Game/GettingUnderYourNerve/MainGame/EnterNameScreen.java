package Game.GettingUnderYourNerve.MainGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.FileHandler;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;

public class EnterNameScreen implements Screen, InputProcessor {

    private Main game;
    private TitleScreen titleScreen;
    private Viewport viewport;
    private Vector3 touchVec;

    private Texture boardTL, boardTC, boardTR, boardCL, boardCC, boardCR, boardBL, boardBC, boardBR;
    private Texture btnL, btnC, btnR;

    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout layout;

    private Rectangle[] slotRects = new Rectangle[4];
    private Rectangle confirmRect;
    private Rectangle cancelRect;
    private Rectangle backRect;

    private String[] slotNames = new String[4];
    private boolean isTyping = false;
    private int selectedSlot = -1;
    private StringBuilder typedName = new StringBuilder();

    public EnterNameScreen(Main game, TitleScreen titleScreen) {
        this.game = game;
        this.titleScreen = titleScreen;
        this.viewport = new ExtendViewport(800, 480);
        this.viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        this.touchVec = new Vector3();
        this.layout = new GlyphLayout();

        for (int i = 0; i < 4; i++) {
            slotRects[i] = new Rectangle();
        }
        confirmRect = new Rectangle();
        cancelRect = new Rectangle();
        backRect = new Rectangle();

        loadAssets();
        refreshSlots();
        recalcLayout();
    }

    private void recalcLayout() {
        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();

        float btnWidth = 300f;
        float btnHeight = 45f;
        float startX = (worldW - btnWidth) / 2f;
        float startY = 320f;
        float gap = 60f;

        for (int i = 0; i < 4; i++) {
            slotRects[i].set(startX, startY - (i * gap), btnWidth, btnHeight);
        }

        float popupCenterX = worldW / 2f;
        cancelRect.set(popupCenterX - 140f, 150f, 130f, 40f);
        confirmRect.set(popupCenterX + 10f, 150f, 130f, 40f);

        backRect.set(20f, worldH - 60f, 140f, 40f);
    }

    private void loadAssets() {
        boardTL = game.assets.manager.get(GameAssetManager.BOARD_TL, Texture.class);
        boardTC = game.assets.manager.get(GameAssetManager.BOARD_TC, Texture.class);
        boardTR = game.assets.manager.get(GameAssetManager.BOARD_TR, Texture.class);
        boardCL = game.assets.manager.get(GameAssetManager.BOARD_CL, Texture.class);
        boardCC = game.assets.manager.get(GameAssetManager.BOARD_CC, Texture.class);
        boardCR = game.assets.manager.get(GameAssetManager.BOARD_CR, Texture.class);
        boardBL = game.assets.manager.get(GameAssetManager.BOARD_BL, Texture.class);
        boardBC = game.assets.manager.get(GameAssetManager.BOARD_BC, Texture.class);
        boardBR = game.assets.manager.get(GameAssetManager.BOARD_BR, Texture.class);

        btnL = game.assets.manager.get(GameAssetManager.BUTTON_L, Texture.class);
        btnC = game.assets.manager.get(GameAssetManager.BUTTON_C, Texture.class);
        btnR = game.assets.manager.get(GameAssetManager.BUTTON_R, Texture.class);

        font = loadFont("ui/runescape_uf.ttf", 24);
        titleFont = loadFont("ui/runescape_uf.ttf", 36);
    }

    private void refreshSlots() {
        for (int i = 0; i < 4; i++) {
            String[] data = FileHandler.getSlotInfo(i);
            slotNames[i] = (data != null) ? data[0] : null;
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        if (titleScreen != null) {
            titleScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        recalcLayout();
    }

    @Override
    public void render(float delta) {
        handleMouseInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (titleScreen != null && titleScreen.getMenuBg() != null) {
            titleScreen.getMenuBg().updateAndRender(delta, game.batch);
        }

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);

        float worldW = viewport.getWorldWidth();

        game.batch.begin();

        layout.setText(titleFont, "SELECT A SAVE SLOT");
        titleFont.draw(game.batch, "SELECT A SAVE SLOT", (worldW - layout.width) / 2f, 420);

        font.setColor(Color.LIGHT_GRAY);
        layout.setText(font, "Left Click: Play  |  Right Click: Delete");
        font.draw(game.batch, "Left Click: Play  |  Right Click: Delete", (worldW - layout.width) / 2f, 390);
        font.setColor(Color.WHITE);

        for (int i = 0; i < 4; i++) {
            String label = (slotNames[i] == null) ? "Slot " + (i + 1) + " - Empty" : slotNames[i];
            drawButton(slotRects[i], label);
        }

        drawButton(backRect, "BACK");

        if (isTyping) {
            drawTypingPopup();
        }

        game.batch.end();
    }

    private void drawTypingPopup() {
        float worldW = viewport.getWorldWidth();
        float bW = 400f;
        float bH = 200f;
        float corner = 32f;
        float bx = (worldW - bW) / 2f;
        float by = 130f;
        float inW = bW - corner * 2;
        float inH = bH - corner * 2;

        game.batch.draw(boardTL, bx, by + bH - corner, corner, corner);
        game.batch.draw(boardTC, bx + corner, by + bH - corner, inW, corner);
        game.batch.draw(boardTR, bx + bW - corner, by + bH - corner, corner, corner);
        game.batch.draw(boardCL, bx, by + corner, corner, inH);
        game.batch.draw(boardCC, bx + corner, by + corner, inW, inH);
        game.batch.draw(boardCR, bx + bW - corner, by + corner, corner, inH);
        game.batch.draw(boardBL, bx, by, corner, corner);
        game.batch.draw(boardBC, bx + corner, by, inW, corner);
        game.batch.draw(boardBR, bx + bW - corner, by, corner, corner);

        layout.setText(font, "ENTER CHARACTER NAME:");
        font.draw(game.batch, "ENTER CHARACTER NAME:", (worldW - layout.width) / 2f, 290);

        layout.setText(font, typedName.toString());
        float nameWidth = layout.width;
        float textStartX = (worldW - nameWidth) / 2f;

        font.setColor(Color.YELLOW);
        font.draw(game.batch, typedName.toString(), textStartX, 240);

        if (System.currentTimeMillis() / 500 % 2 == 0) {
            font.draw(game.batch, "_", textStartX + nameWidth, 240);
        }
        font.setColor(Color.WHITE);

        drawButton(cancelRect, "CANCEL");
        drawButton(confirmRect, "START");
    }

    private void handleMouseInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchVec);

            if (backRect.contains(touchVec.x, touchVec.y) && !isTyping) {
                AudioManager.playSFX(AudioManager.buttonSound);
                game.setScreen(titleScreen);
                dispose();
                return;
            }

            if (!isTyping) {
                for (int i = 0; i < 4; i++) {
                    if (slotRects[i].contains(touchVec.x, touchVec.y)) {
                        AudioManager.playSFX(AudioManager.buttonSound);
                        if (slotNames[i] == null) {
                            isTyping = true;
                            selectedSlot = i;
                            typedName.setLength(0);
                        } else {
                            String[] currentData = FileHandler.getSlotInfo(i);
                            float savedX = Float.parseFloat(currentData[1]);
                            float savedY = Float.parseFloat(currentData[2]);
                            int savedLevel = (currentData.length > 3) ? Integer.parseInt(currentData[3]) : 0;

                            game.setScreen(new DifficultyScreen(game, titleScreen, currentData[0], i, savedX, savedY, savedLevel));
                        }
                    }
                }
            } else {
                if (confirmRect.contains(touchVec.x, touchVec.y)) {
                    AudioManager.playSFX(AudioManager.buttonSound);
                    confirmName();
                } else if (cancelRect.contains(touchVec.x, touchVec.y)) {
                    AudioManager.playSFX(AudioManager.buttonSound);
                    isTyping = false;
                }
            }
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT) && !isTyping) {
            touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchVec);

            for (int i = 0; i < 4; i++) {
                if (slotRects[i].contains(touchVec.x, touchVec.y) && slotNames[i] != null) {
                    AudioManager.playSFX(AudioManager.buttonSound);
                    FileHandler.deleteSlot(i);
                    refreshSlots();
                }
            }
        }
    }

    private void confirmName() {
        if (typedName.length() > 0) {
            String name = typedName.toString().trim();
            FileHandler.saveSlot(selectedSlot, name, 0f, 0f, 0);
            game.setScreen(new DifficultyScreen(game, titleScreen, name, selectedSlot, 0f, 0f, 0));
        }
    }

    @Override
    public boolean keyTyped(char character) {
        if (!isTyping) return false;

        if (character == '\b' && typedName.length() > 0) {
            typedName.setLength(typedName.length() - 1);
        } else if (character == '\r' || character == '\n') {
            AudioManager.playSFX(AudioManager.buttonSound);
            confirmName();
        } else if (Character.isLetterOrDigit(character) || character == ' ') {
            if (typedName.length() < 12) {
                typedName.append(character);
            }
        }
        return true;
    }

    private void drawButton(Rectangle rect, String label) {
        game.batch.draw(btnL, rect.x, rect.y, 20, rect.height);
        game.batch.draw(btnC, rect.x + 20, rect.y, rect.width - 40, rect.height);
        game.batch.draw(btnR, rect.x + rect.width - 20, rect.y, 20, rect.height);

        layout.setText(font, label);
        font.draw(game.batch, label, rect.x + (rect.width - layout.width) / 2f, rect.y + (rect.height + layout.height) / 2f);
    }

    private BitmapFont loadFont(String filename, int size) {
        try {
            FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal(filename));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = size; p.color = Color.WHITE; p.borderWidth = 1.5f; p.borderColor = new Color(0f, 0f, 0f, 0.6f);
            BitmapFont f = gen.generateFont(p); gen.dispose(); return f;
        } catch (Exception e) { return new BitmapFont(); }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        if (titleScreen != null) {
            titleScreen.resize(width, height);
        }
        recalcLayout();
    }

    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() {
        if(font != null) font.dispose();
        if(titleFont != null) titleFont.dispose();
    }

    @Override public boolean keyDown(int keycode) { return false; }
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}
