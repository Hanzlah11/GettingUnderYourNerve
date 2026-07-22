package Game.GettingUnderYourNerve.MainGame;

import Game.GettingUnderYourNerve.Main;
import Game.GettingUnderYourNerve.Utilities.AudioManager;
import Game.GettingUnderYourNerve.Utilities.GameAssetManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class FootballMinigameScreen implements Screen {

    private final Main game;
    private final Screen savedPlayScreen;

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final SpriteBatch batch;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    private float mapPixelWidth;
    private float mapPixelHeight;

    private Array<Rectangle> walls;
    private Rectangle playerGoal;
    private Rectangle cr7Goal;

    private Ball ball;

    private final Vector2 playerPos;
    private final Vector2 cr7Pos;
    private final Rectangle playerBounds;
    private final Rectangle cr7Bounds;

    private final Vector2 playerSpawn;
    private final Vector2 cr7Spawn;
    private final Vector2 ballSpawn;

    private final float MOVE_SPEED = 180f;
    private float stateTime = 0f;

    private float collisionImmunityTimer = 0f;

    private boolean isEnding = false;
    private boolean winOutcome = false;

    // Animations from the Asset Manager
    private final Animation<TextureRegion> playerIdleAnim;
    private final Animation<TextureRegion> playerRunAnim;
    private final Animation<TextureRegion> cr7RunAnim;

    private boolean playerMoving = false;
    private boolean playerFacingRight = true;
    private boolean cr7FacingRight = false;

    public FootballMinigameScreen(Main game, Screen savedPlayScreen) {
        this.game = game;
        this.savedPlayScreen = savedPlayScreen;
        this.batch = game.getBatch();

        map = new TmxMapLoader().load("data/tilemaps/Minigame.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);

        int mapWidth = map.getProperties().get("width", Integer.class);
        int mapHeight = map.getProperties().get("height", Integer.class);
        int tileWidth = map.getProperties().get("tilewidth", Integer.class);
        int tileHeight = map.getProperties().get("tileheight", Integer.class);

        mapPixelWidth = mapWidth * tileWidth;
        mapPixelHeight = mapHeight * tileHeight;

        camera = new OrthographicCamera();
        viewport = new FitViewport(896, 560, camera);

        if (AudioManager.elevatorMusic != null) AudioManager.elevatorMusic.stop();
        if (AudioManager.level1Music != null) AudioManager.level1Music.stop();
        if (AudioManager.level2Music != null) AudioManager.level2Music.stop();
        if (AudioManager.bossArenaMusic != null) AudioManager.bossArenaMusic.stop();
        AudioManager.crabPatrol.stop();

        if (AudioManager.footballCrowd != null) {
            AudioManager.footballCrowd.setLooping(true);
            AudioManager.footballCrowd.play();
        }
        if (AudioManager.footballWhistle != null) {
            AudioManager.footballWhistle.play();
        }

        walls = new Array<>();
        playerPos = new Vector2();
        cr7Pos = new Vector2();

        playerBounds = new Rectangle(0, 0, 64, 64);
        cr7Bounds = new Rectangle(0, 0, 48, 48);

        playerSpawn = new Vector2();
        cr7Spawn = new Vector2();
        ballSpawn = new Vector2();

        playerIdleAnim = GameAssetManager.getAnimation(GameAssetManager.PLAYER_IDLE_PREFIX, 5, 0.15f, Animation.PlayMode.LOOP, "%02d");
        playerRunAnim = GameAssetManager.getAnimation(GameAssetManager.PLAYER_RUN_PREFIX, 6, 0.1f, Animation.PlayMode.LOOP, "%02d");
        cr7RunAnim = GameAssetManager.getAnimation(GameAssetManager.CR7_RUN_PREFIX, 4, 0.1f, Animation.PlayMode.LOOP, "%d");

        parseMapObjects();
        resetPositions();
    }

    private void parseMapObjects() {
        if (map.getLayers().get("GoalObjects") != null) {
            for (MapObject object : map.getLayers().get("GoalObjects").getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    if ("PlayerGoal".equals(object.getName())) playerGoal = rect;
                    if ("CR7Goal".equals(object.getName())) cr7Goal = rect;
                }
            }
        }

        if (map.getLayers().get("Walls") != null) {
            for (MapObject object : map.getLayers().get("Walls").getObjects()) {
                if (object instanceof RectangleMapObject) {
                    walls.add(((RectangleMapObject) object).getRectangle());
                }
            }
        }

        if (map.getLayers().get("Player") != null) {
            for (MapObject object : map.getLayers().get("Player").getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    playerSpawn.set(rect.x, rect.y);
                }
            }
        }

        if (map.getLayers().get("CR7") != null) {
            for (MapObject object : map.getLayers().get("CR7").getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    cr7Spawn.set(rect.x, rect.y);
                }
            }
        }

        if (map.getLayers().get("Ball") != null) {
            for (MapObject object : map.getLayers().get("Ball").getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    ballSpawn.set(rect.x, rect.y);
                }
            }
        }

        Texture ballTexture = GameAssetManager.manager.get(GameAssetManager.FOOTBALL_BALL, Texture.class);
        ball = new Ball(ballSpawn.x, ballSpawn.y, ballTexture);
        ball.getBounds().setSize(20, 20);
    }

    private void resetPositions() {
        playerPos.set(playerSpawn);
        cr7Pos.set(cr7Spawn);
        ball.setPosition(ballSpawn.x, ballSpawn.y);
        ball.getVelocity().set(0, 0);
        playerBounds.setPosition(playerPos);
        cr7Bounds.setPosition(cr7Pos);
        collisionImmunityTimer = 0f;
    }

    @Override
    public void render(float delta) {
        update(delta);

        if (game.getScreen() != this) return;

        viewport.apply();
        Gdx.gl.glClearColor(0.2f, 0.5f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stateTime += delta;

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        TextureRegion playerFrame = playerMoving ? playerRunAnim.getKeyFrame(stateTime, true) : playerIdleAnim.getKeyFrame(stateTime, true);
        if ((playerFacingRight && playerFrame.isFlipX()) || (!playerFacingRight && !playerFrame.isFlipX())) {
            playerFrame.flip(true, false);
        }
        batch.draw(playerFrame, playerPos.x, playerPos.y, 80, 80);

        TextureRegion cr7Frame = cr7RunAnim.getKeyFrame(stateTime, true);
        if ((cr7FacingRight && cr7Frame.isFlipX()) || (!cr7FacingRight && !cr7Frame.isFlipX())) {
            cr7Frame.flip(true, false);
        }
        batch.draw(cr7Frame, cr7Pos.x, cr7Pos.y, 48, 48);

        ball.draw(batch);

        batch.end();
    }

    private void update(float delta) {
        float dt = Math.min(delta, 1f / 60f);

        if (collisionImmunityTimer > 0f) {
            collisionImmunityTimer -= dt;
        }

        if (isEnding) {
            ball.update(dt);
            handleBallCollisions();
            if (AudioManager.footballWhistle != null && !AudioManager.footballWhistle.isPlaying()) {
                // FIXED: Crowd tracks are now stopped right here after the whistle simulation completely finishes[cite: 16]
                if (AudioManager.footballCrowd != null) {
                    AudioManager.footballCrowd.stop();
                }

                PlayScreen platformer = (PlayScreen) savedPlayScreen;
                platformer.getPlayableMap().resolveMinigameOutcome(winOutcome);

                if (!winOutcome) {
                    platformer.getPlayer().hit(100, ball.getPosition().x);
                }

                if (platformer.currentTrack != null) {
                    platformer.currentTrack.setLooping(true);
                    platformer.currentTrack.setVolume(0.15f);
                    platformer.currentTrack.play();
                }

                game.setScreen(savedPlayScreen);
                this.dispose();
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(896, 560);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        handlePlayerInput(dt);
        handleCR7AI(dt);

        ball.update(dt);

        handleEntityWallCollisions();
        handleBallCollisions();
        checkGoalScoring();

        if (game.getScreen() != this) return;

        camera.position.set(mapPixelWidth / 2f, mapPixelHeight / 2f, 0f);
        camera.update();
    }

    private void handlePlayerInput(float dt) {
        Vector2 moveInput = new Vector2(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) moveInput.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveInput.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveInput.x += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveInput.x -= 1;

        if (moveInput.len() > 0) {
            playerMoving = true;
            moveInput.nor().scl(MOVE_SPEED * dt);
            playerPos.add(moveInput);
            playerBounds.setPosition(playerPos);

            if (moveInput.x > 0) playerFacingRight = true;
            if (moveInput.x < 0) playerFacingRight = false;
        } else {
            playerMoving = false;
        }
    }

    private void handleCR7AI(float dt) {
        float ballCenterX = ball.getBounds().x + (ball.getBounds().width / 2f);
        float ballCenterY = ball.getBounds().y + (ball.getBounds().height / 2f);
        Vector2 cr7Center = new Vector2(cr7Pos.x + 24, cr7Pos.y + 24);

        float goalX = 0f;
        float goalY = 280f;
        if (playerGoal != null) {
            goalX = playerGoal.x + playerGoal.width / 2f;
            goalY = playerGoal.y + playerGoal.height / 2f;
        }

        Vector2 target = new Vector2();

        if (cr7Center.x < ballCenterX + 16f) {
            float targetX = ballCenterX + 50f;
            float targetY = ballCenterY;

            if (Math.abs(cr7Center.y - ballCenterY) < 40f) {
                if (cr7Center.y > ballCenterY) {
                    targetY = MathUtils.clamp(ballCenterY + 50f, 64f, mapPixelHeight - 64f);
                } else {
                    targetY = MathUtils.clamp(ballCenterY - 50f, 64f, mapPixelHeight - 64f);
                }
                if (cr7Center.x < ballCenterX - 16f) {
                    targetX = cr7Center.x + 12f;
                }
            }
            target.set(targetX, targetY);
        }
        else {
            Vector2 goalToBall = new Vector2(ballCenterX - goalX, ballCenterY - goalY).nor();
            Vector2 sweetSpot = new Vector2(ballCenterX, ballCenterY).add(goalToBall.scl(36f));

            sweetSpot.x = MathUtils.clamp(sweetSpot.x, 64f, mapPixelWidth - 64f);
            sweetSpot.y = MathUtils.clamp(sweetSpot.y, 64f, mapPixelHeight - 64f);

            if (cr7Center.dst(sweetSpot) > 16f) {
                target.set(sweetSpot);
            } else {
                target.set(ballCenterX, ballCenterY);
            }
        }

        Vector2 direction = target.sub(cr7Center);
        float distance = direction.len();

        if (distance > 0.5f) {
            float movementStep = Math.min(distance, MOVE_SPEED * 0.85f * dt);
            direction.nor().scl(movementStep);
            cr7Pos.add(direction);
            cr7Bounds.setPosition(cr7Pos);

            cr7FacingRight = direction.x > 0;
        }
    }

    private void handleEntityWallCollisions() {
        for (Rectangle wall : walls) {
            if (Intersector.overlaps(playerBounds, wall)) {
                bounceEntityOffWall(playerPos, playerBounds, wall);
            }
            if (Intersector.overlaps(cr7Bounds, wall)) {
                bounceEntityOffWall(cr7Pos, cr7Bounds, wall);
            }
        }

        playerPos.x = MathUtils.clamp(playerPos.x, 16f, mapPixelWidth - 16f - playerBounds.width);
        playerPos.y = MathUtils.clamp(playerPos.y, 16f, mapPixelHeight - 16f - playerBounds.height);
        playerBounds.setPosition(playerPos);

        cr7Pos.x = MathUtils.clamp(cr7Pos.x, 16f, mapPixelWidth - 16f - cr7Bounds.width);
        cr7Pos.y = MathUtils.clamp(cr7Pos.y, 16f, mapPixelHeight - 16f - cr7Bounds.height);
        cr7Bounds.setPosition(cr7Pos);
    }

    private void bounceEntityOffWall(Vector2 pos, Rectangle bounds, Rectangle wall) {
        float overlapX = Math.min(pos.x + bounds.width - wall.x, wall.x + wall.width - pos.x);
        float overlapY = Math.min(pos.y + bounds.height - wall.y, wall.y + wall.height - pos.y);

        if (overlapX < overlapY) {
            if (pos.x + bounds.width / 2f < wall.x + wall.width / 2f) pos.x -= overlapX;
            else pos.x += overlapX;
        } else {
            if (pos.y + bounds.height / 2f < wall.y + wall.height / 2f) pos.y -= overlapY;
            else pos.y += overlapY;
        }
        bounds.setPosition(pos);
    }

    private void handleBallCollisions() {
        Vector2 ballVec = ball.getPosition();
        Rectangle ballBounds = ball.getBounds();
        boolean wallOverlapOccurred = false;

        for (Rectangle wall : walls) {
            if (Intersector.overlaps(ballBounds, wall)) {
                wallOverlapOccurred = true;
                float overlapX = Math.min(ballBounds.x + ballBounds.width - wall.x, wall.x + wall.width - ballBounds.x);
                float overlapY = Math.min(ballBounds.y + ballBounds.height - wall.y, wall.y + wall.height - ballBounds.y);

                if (overlapX < overlapY) {
                    if (ballVec.x + ballBounds.width / 2f < wall.x + wall.width / 2f) {
                        ballVec.x -= (overlapX + 2f);
                    } else {
                        ballVec.x += (overlapX + 2f);
                    }
                    ball.getVelocity().x = -ball.getVelocity().x * 0.75f;
                } else {
                    if (ballVec.y + ballBounds.height / 2f < wall.y + wall.height / 2f) {
                        ballVec.y -= (overlapY + 2f);
                    } else {
                        ballVec.y += (overlapY + 2f);
                    }
                    ball.getVelocity().y = -ball.getVelocity().y * 0.75f;
                }
                ball.setPosition(ballVec.x, ballVec.y);
                AudioManager.playSFX(AudioManager.footballKick, 0.4f);
            }
        }

        ballVec.x = MathUtils.clamp(ballVec.x, 16f, mapPixelWidth - 16f - ballBounds.width);
        ballVec.y = MathUtils.clamp(ballVec.y, 16f, mapPixelHeight - 16f - ballBounds.height);
        ball.setPosition(ballVec.x, ballVec.y);

        float ballCenterX = ballBounds.x + (ballBounds.width / 2f);
        float ballCenterY = ballBounds.y + (ballBounds.height / 2f);

        if (wallOverlapOccurred && (Intersector.overlaps(playerBounds, ballBounds) || Intersector.overlaps(cr7Bounds, ballBounds))) {
            Vector2 fieldCenter = new Vector2(mapPixelWidth / 2f, mapPixelHeight / 2f);
            Vector2 blastDir = fieldCenter.sub(ballVec).nor();

            ballVec.add(blastDir.x * 12f, blastDir.y * 12f);
            ball.setPosition(ballVec.x, ballVec.y);
            ball.getVelocity().set(blastDir.x * MOVE_SPEED * 2.2f, blastDir.y * MOVE_SPEED * 2.2f);

            collisionImmunityTimer = 0.15f;
            AudioManager.playSFX(AudioManager.footballKick, 0.4f);
            return;
        }

        if (collisionImmunityTimer > 0f) return;

        if (Intersector.overlaps(playerBounds, ball.getBounds())) {
            Vector2 playerCenter = new Vector2(playerBounds.x + 32, playerBounds.y + 32);
            Vector2 ballCenter = new Vector2(ball.getBounds().x + (ball.getBounds().width / 2f), ball.getBounds().y + (ball.getBounds().height / 2f));
            Vector2 pushDir = ballCenter.sub(playerCenter).nor();

            if (ballBounds.x < 48f && pushDir.x < 0) pushDir.x = -pushDir.x;
            if (ballBounds.x > mapPixelWidth - 48f && pushDir.x > 0) pushDir.x = -pushDir.x;
            if (ballBounds.y < 48f && pushDir.y < 0) pushDir.y = -pushDir.y;
            if (ballBounds.y > mapPixelHeight - 48f && pushDir.y > 0) pushDir.y = -pushDir.y;
            pushDir.nor();

            ball.setVelocity(pushDir.x * MOVE_SPEED * 1.3f, pushDir.y * MOVE_SPEED * 1.3f);
            ballVec.add(pushDir.x * 6f, pushDir.y * 6f);
            ball.setPosition(ballVec.x, ballVec.y);
            AudioManager.playSFX(AudioManager.footballKick, 0.4f);
        }

        if (Intersector.overlaps(cr7Bounds, ball.getBounds())) {
            Vector2 cr7Center = new Vector2(cr7Bounds.x + 24, cr7Bounds.y + 24);
            Vector2 ballCenter = new Vector2(ball.getBounds().x + (ball.getBounds().width / 2f), ball.getBounds().y + (ball.getBounds().height / 2f));
            Vector2 pushDir = ballCenter.sub(cr7Center).nor();

            if (ballBounds.x < 48f && pushDir.x < 0) pushDir.x = -pushDir.x;
            if (ballBounds.x > mapPixelWidth - 48f && pushDir.x > 0) pushDir.x = -pushDir.x;
            if (ballBounds.y < 48f && pushDir.y < 0) pushDir.y = -pushDir.y;
            if (ballBounds.y > mapPixelHeight - 48f && pushDir.y > 0) pushDir.y = -pushDir.y;
            pushDir.nor();

            ball.setVelocity(pushDir.x * MOVE_SPEED * 1.3f, pushDir.y * MOVE_SPEED * 1.3f);
            ballVec.add(pushDir.x * 6f, pushDir.y * 6f);
            ball.setPosition(ballVec.x, ballVec.y);
            AudioManager.playSFX(AudioManager.footballKick, 0.4f);
        }
    }

    private void checkGoalScoring() {
        if (isEnding) return;
        Rectangle ballBounds = ball.getBounds();

        if (cr7Goal != null && ballBounds.x >= cr7Goal.x
            && ballBounds.y >= cr7Goal.y && (ballBounds.y + ballBounds.height) <= (cr7Goal.y + cr7Goal.height)) {

            isEnding = true;
            winOutcome = true;
            // FIXED: Removed early stop call to keep track playing during whistle loop[cite: 16]
            if (AudioManager.footballWhistle != null) AudioManager.footballWhistle.play();
            return;
        }

        if (playerGoal != null && (ballBounds.x + ballBounds.width) <= (playerGoal.x + playerGoal.width)
            && ballBounds.y >= playerGoal.y && (ballBounds.y + ballBounds.height) <= (playerGoal.y + playerGoal.height)) {

            isEnding = true;
            winOutcome = false;
            // FIXED: Removed early stop call to keep track playing during whistle loop[cite: 16]
            if (AudioManager.footballWhistle != null) AudioManager.footballWhistle.play();
            return;
        }
    }

    @Override public void show() {}

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        // FIXED SAFETY: Ensures clean stops on fast manual exits[cite: 16]
        if (AudioManager.footballCrowd != null) AudioManager.footballCrowd.stop();
    }
}
