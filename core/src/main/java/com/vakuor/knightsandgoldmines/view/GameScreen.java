package com.vakuor.knightsandgoldmines.view;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import ru.test.gdxtest.MainScreen;

public class GameScreen extends InputAdapter implements ApplicationListener {

    static class Player{
        static float WIDTH;
        static float HEIGHT;
        static float MAX_VELOCITY = 10f;
        static float JUMP_VELOCITY = 40f;
        static float DAMPING = 0.87f;

        enum State {
            Standing, Walking, Jumping
        }

        final Vector2 position = new Vector2();
        final Vector2 velocity = new Vector2();
        State state = State.Walking;
        float stateTime = 0;
        boolean facesRight = true;
        boolean grounded = false;
    }


    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Texture playerTexture;
    private Animation<TextureRegion> stand;
    private Animation<TextureRegion> walk;
    private Animation<TextureRegion> jump;
    private Player player;
    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };
    private Array<Rectangle> tiles = new Array<Rectangle>();

    private static final float GRAVITY = -2.5f;

    private boolean debug = true;
    private ShapeRenderer debugRenderer;


    private TextureAtlas playerTextureAtlas,controlsTextureAtlas;
    private Array<TextureAtlas.AtlasRegion> bodyframes;
    private Array<TextureAtlas.AtlasRegion> standframes;
    private Array<TextureAtlas.AtlasRegion> jumpframes;
    public static Array<TextureAtlas.AtlasRegion> controlsframes;


    private Array<Texture> bodyframestexturearray;


    @Override
    public void create() {
        // load the koala frames, split them, and assign them to Animations
        playerTextureAtlas = new TextureAtlas("visual/output/Archer/Archers.atlas");
        controlsTextureAtlas = new TextureAtlas("visual/output/flatDark/Controls.atlas");

        bodyframes =  playerTextureAtlas.findRegions("body");
        standframes =  playerTextureAtlas.findRegions("idle");
        jumpframes =  playerTextureAtlas.findRegions("jump");
        controlsframes = controlsTextureAtlas.findRegions("flatDark");

        stand = new Animation(0, standframes);
        jump = new Animation(1, jumpframes);
        walk = new Animation(0.15f,bodyframes);



        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        // figure out the width and height of the koala for collision
        // detection and rendering by converting a koala frames pixel
        // size into world units (1 unit == 16 pixels)
        Player.WIDTH = 1 / 16f * bodyframes.get(0).getRegionWidth();
        Player.HEIGHT = 1 / 16f * bodyframes.get(0).getRegionHeight();

        // load the map, set the unit scale to 1/12 (1 unit == 12 pixels)
        map = new TmxMapLoader().load("logical/maps/Map/level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / 12f);

        // create an orthographic camera, shows us 30x20 units of the world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 30, 20);
        camera.update();

        // create the Koala we want to move around the world
        player = new Player();
        player.position.set(20, 20);
        //System.out.println(Player.HEIGHT);

        debugRenderer = new ShapeRenderer();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        // clear the screen
        Gdx.gl.glClearColor(0.7f, 0.7f, 1.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // get the delta time
        float deltaTime = Gdx.graphics.getDeltaTime();

        // update the koala (process input, collision detection, position update)
        updatePlayer(deltaTime);

        // let the camera follow the koala, x-axis only
        camera.position.x = player.position.x;
        camera.position.y = player.position.y;
        camera.update();

        // set the TiledMapRenderer view based on what the
        // camera sees, and render the map
        renderer.setView(camera);
        renderer.render();

        // render the koala
        renderPlayer(deltaTime);

        // render debug rectangles
        if (debug) renderDebug();
    }

    private void updatePlayer(float deltaTime) {
        if (deltaTime == 0) return;

        if (deltaTime > 0.1f)
            deltaTime = 0.1f;

        player.stateTime += deltaTime;

        // check input and apply to velocity & state
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE)|| isTouched(0.5f, 1)) && player.grounded) {
            player.velocity.y += Player.JUMP_VELOCITY;
            player.state = Player.State.Jumping;
            player.grounded = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A) || isTouched(0, 0.25f)) {
            player.velocity.x = -Player.MAX_VELOCITY;
            if (player.grounded) player.state = Player.State.Walking;
            player.facesRight = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D) || isTouched(0.25f, 0.5f)) {
            player.velocity.x = Player.MAX_VELOCITY;
            if (player.grounded) player.state = Player.State.Walking;
            player.facesRight = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.B))
            debug = !debug;

        // apply gravity if we are falling
        player.velocity.add(0, GRAVITY);

        // clamp the velocity to the maximum, x-axis only
        player.velocity.x = MathUtils.clamp(player.velocity.x,
                -Player.MAX_VELOCITY, Player.MAX_VELOCITY);

        // If the velocity is < 1, set it to 0 and set state to Standing
        if (Math.abs(player.velocity.x) < 1) {
            player.velocity.x = 0;
            if (player.grounded) player.state = Player.State.Standing;
        }

        // multiply by delta time so we know how far we go
        // in this frame
        player.velocity.scl(deltaTime);

        // perform collision detection & response, on each axis, separately
        // if the player is moving right, check the tiles to the right of it's
        // right bounding box edge, otherwise check the ones to the left
        Rectangle playerRect = rectPool.obtain();
        playerRect.set(player.position.x, player.position.y, Player.WIDTH, Player.HEIGHT);
        int startX, startY, endX, endY;
        if (player.velocity.x > 0) {
            startX = endX = (int) (player.position.x + Player.WIDTH + player.velocity.x);
        } else {
            startX = endX = (int) (player.position.x + player.velocity.x);
        }
        startY = (int) (player.position.y);
        endY = (int) (player.position.y + Player.HEIGHT);
        getTiles(startX, startY, endX, endY, tiles);
        playerRect.x += player.velocity.x;
        for (Rectangle tile : tiles) {
            if (playerRect.overlaps(tile)) {
                player.velocity.x = 0;
                break;
            }
        }
        playerRect.x = player.position.x;

        // if the player is moving upwards, check the tiles to the top of its
        // top bounding box edge, otherwise check the ones to the bottom
        if (player.velocity.y > 0) {
            startY = endY = (int) (player.position.y + Player.HEIGHT + player.velocity.y);
        } else {
            startY = endY = (int) (player.position.y + player.velocity.y);
        }
        startX = (int) (player.position.x);
        endX = (int) (player.position.x + Player.WIDTH);
        getTiles(startX, startY, endX, endY, tiles);
        playerRect.y += player.velocity.y;
        for (Rectangle tile : tiles) {
            if (playerRect.overlaps(tile)) {
                // we actually reset the player y-position here
                // so it is just below/above the tile we collided with
                // this removes bouncing :)
                if (player.velocity.y > 0) {
                    player.position.y = tile.y - Player.HEIGHT;
                    // we hit a block jumping upwards, let's destroy it!
                    TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("walls");
                    layer.setCell((int) tile.x, (int) tile.y, null);
                } else {
                    player.position.y = tile.y + tile.height;
                    // if we hit the ground, mark us as grounded so we can jump
                    player.grounded = true;
                }
                player.velocity.y = 0;
                break;
            }
        }
        rectPool.free(playerRect);

        // unscale the velocity by the inverse delta time and set
        // the latest position
        player.position.add(player.velocity);
        player.velocity.scl(1 / deltaTime);

        // Apply damping to the velocity on the x-axis so we don't
        // walk infinitely once a key was pressed
        player.velocity.x *= Player.DAMPING;
    }

    private boolean isTouched(float startX, float endX) {
        // Check for touch inputs between startX and endX
        // startX/endX are given between 0 (left edge of the screen) and 1 (right edge of the screen)
        for (int i = 0; i < 2; i++) {
            float x = Gdx.input.getX(i) / (float) Gdx.graphics.getWidth();
            if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
                return true;
            }
        }
        return false;
    }

    private void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("walls");
        rectPool.freeAll(tiles);
        tiles.clear();
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x, y, 1, 1);
                    tiles.add(rect);
                }
            }
        }
    }

    private void renderPlayer(float deltaTime) {
        // based on the player state, get the animation frame
        TextureRegion frame = null;
        switch (player.state) {
            case Standing:
                frame = stand.getKeyFrame(player.stateTime);
                break;
            case Walking:
                frame = walk.getKeyFrame(player.stateTime);
                break;
            case Jumping:{
                if(player.velocity.y>=0){
                    frame = jump.getKeyFrame(0);}
                else if(player.velocity.y<-30){
                    frame = jump.getKeyFrame(2);}
                else if(player.velocity.y<0){
                    frame = jump.getKeyFrame(1);}
                break;
            }
        }


        // draw the player, depending on the current velocity
        // on the x-axis, draw the player facing either right
        // or left
        Batch batch = renderer.getBatch();
        batch.begin();
        if (player.facesRight) {
            batch.draw(frame, player.position.x-Player.WIDTH/2, player.position.y-Player.HEIGHT/2-0.5f/Player.HEIGHT, Player.WIDTH*2, Player.HEIGHT*2);
        } else {
            batch.draw(frame, player.position.x + 1.5f*Player.WIDTH, player.position.y-Player.HEIGHT/2-0.5f/Player.HEIGHT, -Player.WIDTH*2, Player.HEIGHT*2);
        }
        batch.end();
    }

    private void renderDebug() {
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);

        debugRenderer.setColor(Color.RED);
        debugRenderer.rect(player.position.x, player.position.y, Player.WIDTH, Player.HEIGHT);

        debugRenderer.setColor(Color.YELLOW);
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("walls");
        for (int y = 0; y <= layer.getHeight(); y++) {
            for (int x = 0; x <= layer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    if (camera.frustum.boundsInFrustum(x + 0.5f, y + 0.5f, 0, 1, 1, 0))
                        debugRenderer.rect(x, y, 1, 1);
                }
            }
        }
        debugRenderer.end();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        playerTextureAtlas.dispose();
    }
}
