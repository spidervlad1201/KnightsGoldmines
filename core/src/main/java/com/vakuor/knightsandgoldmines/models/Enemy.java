package com.vakuor.knightsandgoldmines.models;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.vakuor.knightsandgoldmines.GameLogic;

import static com.vakuor.knightsandgoldmines.GameLogic.GRAVITY;
import static com.vakuor.knightsandgoldmines.GameLogic.myTile;
import static com.vakuor.knightsandgoldmines.GameLogic.tileDebug;

public class Enemy extends Actor {
    public static float WIDTH;
    public static float HEIGHT;
    public static float MAX_VELOCITY = 5f;
    private static float VELOCITYCNST = 2f;
    private static float JUMP_VELOCITY = 40f;
    public static float DAMPING = 0.87f;

    enum State {
        Standing, Walking, Shooting
    }

    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    private State state = State.Walking;
    private float stateTime = 0;

    private float attacktime=0;
    private float climbconst = 0.2f;
    private float climbingTime = 0;
    private float attackingtime = 0.15f;
    private float timeout = attackingtime*3;
    private float jumptime = 0;
    private float timesincejumped = 1f;
    private float jumptimeconst = 3f;
    private float timesinceattacked=0;
    private float timeforattack=2f;

    private boolean facesRight = true;
    public boolean attacking = false;
    private boolean grounded = false;
    private boolean climb = true;
    private boolean isclimbing = false;
    public boolean ismoving = false;

    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };
    private Array<Rectangle> tiles = new Array<Rectangle>();
    private int startX, startY, endX, endY;

    private int health=0;

    private TextureAtlas enemyTextureAtlas;
    private Animation<TextureRegion> stand;
    private Animation<TextureRegion> walk;
    private Animation<TextureRegion> shot;
    private Array<TextureAtlas.AtlasRegion> walkframes;
    private Array<TextureAtlas.AtlasRegion> standframes;
    private Array<TextureAtlas.AtlasRegion> shotframes;

    public Enemy(int x, int y){
        setPosition(x,y);
        enemyTextureAtlas = new TextureAtlas("visual/output/Enemy/Enemy.atlas");
        walkframes =  enemyTextureAtlas.findRegions("walk");
        standframes =  enemyTextureAtlas.findRegions("stand");
        shotframes =  enemyTextureAtlas.findRegions("attack");

        stand = new Animation<TextureRegion>(0, standframes);
        walk = new Animation<TextureRegion>(0.1f,walkframes,Animation.PlayMode.LOOP);
        shot = new Animation<TextureRegion>(0.5f,shotframes);

        Enemy.WIDTH = 1 / 16f * walkframes.get(0).getRegionWidth();
        Enemy.HEIGHT = 1 / 16f * walkframes.get(0).getRegionHeight();


    }

    public void update(){

        addVelocity(0, GRAVITY);
        // multiply by delta time so we know how far we go in this frame
        velocity.scl(GameLogic.deltaTime);
        position.x+=0.25f; position.y+=0.2f;
        WIDTH-=0.5f; HEIGHT-=0.4f;
        // perform collision detection & response, on each axis, separately//todo:вынести эту хрень отсюда
        // if the player is moving right, check the tiles to the right of it's
        // right bounding box edge, otherwise check the ones to the left
        Rectangle playerRect = rectPool.obtain();
        playerRect.set( position.x,  position.y,  WIDTH,  HEIGHT);
        if ( velocity.x > 0) {
            startX = endX = (int) ( position.x +  WIDTH +  velocity.x); }
        else {
            startX = endX = (int) ( position.x +  velocity.x);
        }

        startY = (int) (position.y);
        endY = (int) (position.y + HEIGHT);
        getTiles(startX, startY, endX, endY, tiles, tileDebug, myTile);
        playerRect.x += velocity.x;//good without deltaTime
        for (Rectangle tile : tiles) {
            if (playerRect.overlaps(tile)) {
                if(isGrounded()){
                    if(velocity.x<0)
                        position.x = tile.x + tile.width;
                    else position.x=tile.x-WIDTH;
                }
                velocity.x = 0;
                if(climb){
                    isclimbing=true;
                    velocity.y=climbconst;
                    setGrounded(false);
                }
                break;
            }
        }
        playerRect.x = position.x;
        // if the player is moving upwards, check the tiles to the top of its
        // top bounding box edge, otherwise check the ones to the bottom
        if (velocity.y > 0) {
            startY = endY = (int) (position.y + HEIGHT + velocity.y); }
        else {
            startY = endY = (int) (position.y + velocity.y);
        }
        startX = (int) (position.x);
        endX = (int) (position.x + WIDTH);
        getTiles(startX, startY, endX, endY, tiles, tileDebug, myTile);
        playerRect.y += velocity.y;
        for (Rectangle tile : tiles) {
            if (playerRect.overlaps(tile)) {
                // we actually reset the player y-position here
                // so it is just below/above the tile we collided with
                // this removes bouncing :)
                if (velocity.y > 0) {
                    position.y = tile.y - HEIGHT;
                    // we hit a block jumping upwards, let's destroy it!
                    TiledMapTileLayer layer = (TiledMapTileLayer) GameLogic.map.getLayers().get("walls");
                    layer.setCell((int) tile.x, (int) tile.y, null);
                } else {
                    position.y = tile.y + tile.height;
                    setGrounded(true);// if we hit the ground, mark us as grounded so we can jump
                }
                velocity.y = 0; break;
            }
        }
        rectPool.free(playerRect);

        // unscale the velocity by the inverse delta time and set
        // the latest position
        position.x-=0.25f; position.y-=0.2f;
        WIDTH+=0.5f; HEIGHT+=0.4f;
        position.add(velocity);
        velocity.scl(1 / GameLogic.deltaTime);
        // Apply damping to the velocity on the x-axis so we don't
        // walk infinitely once a key was pressed
        velocity.x *= DAMPING;
    }



    @Override
    public void act(float deltaTime) {

        jumptimeconst+=GameLogic.deltaTime;
        timesinceattacked+=GameLogic.deltaTime;
        if(GameLogic.player.position.x>position.x+WIDTH || GameLogic.player.position.x<position.x-WIDTH){move(position.x<GameLogic.player.position.x); }
        else if(timesinceattacked>timeforattack&&((GameLogic.player.position.y<=position.y+HEIGHT)&&(GameLogic.player.position.y+GameLogic.player.HEIGHT>=position.y))) {//attack(GameLogic.player);

            GameLogic.player.hit(GameLogic.player.position.x>=position.x);
            timesinceattacked=0;
        }
        if(GameLogic.player.position.y>position.y && timesincejumped>jumptimeconst) jump();


        update();

        if(attacking && attackingtime<timeout) {
            attackingtime += GameLogic.deltaTime;
        }
        else {attacktime =0;}


        if (deltaTime == 0) return;

        if (deltaTime > 0.1f)
            deltaTime = 0.1f;

        stateTime += deltaTime;

        if(climbingTime>climbconst){climb=false;isclimbing=false; climbingTime=0;}
        else if(isclimbing && climb && ismoving)climbingTime+=deltaTime;
        else if(isGrounded()){climb=true;climbingTime=0;}

        //if(climbingTime!=0)System.out.println(climbingTime);


        // If the velocity is < 1, set it to 0 and set state to Standing
        if (Math.abs(velocity.x) < 1) {
            velocity.x = 0;
            if (grounded) state = Enemy.State.Standing;
        }
        super.act(deltaTime);



    }
    @Override
    public void draw(Batch batch, float parentAlpha) {

        TextureRegion frame = null;

        facesRight = position.x < GameLogic.player.position.x;
        switch (state) {
            case Standing:{
                frame = stand.getKeyFrame(0);
                break;}
            case Walking: {
                frame = walk.getKeyFrame(stateTime);
                break;
            }
            case Shooting:{
                frame = shot.getKeyFrame(0);//System.out.println("5");
                break;
            }
        }

        if (facesRight) {
            batch.draw(frame,  position.x-Enemy.WIDTH/2,  position.y-Enemy.HEIGHT/2+0.2f, Enemy.WIDTH*2, Enemy.HEIGHT*2-0.5f);
        } else {
            batch.draw(frame,  position.x + 1.5f*Enemy.WIDTH,  position.y-Enemy.HEIGHT/2+0.2f, -Enemy.WIDTH*2, Enemy.HEIGHT*2-0.5f);
        }

    }
    public void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles, boolean tileDebug, TiledMapTile myTile) {
        TiledMapTileLayer layer = (TiledMapTileLayer) GameLogic.map.getLayers().get("walls");
        rectPool.freeAll(tiles);
        tiles.clear();
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x, y, 1, 1);
                    tiles.add(rect);
                    if(tileDebug)cell.setTile(myTile);
                }
            }
        }
    }

    public void hit(boolean mydir){
        System.out.println("GOEnemy");
        if(mydir){addVelocity(2*Player.hitconst,Player.hitconst);}
        else {addVelocity(-2*Player.hitconst,Player.hitconst);}
        health-=1;
        GameLogic.healthLabel.setText(String.valueOf(health));
    }

    public void jump(){
        velocity.y += Enemy.JUMP_VELOCITY;
        grounded = false;
    }
    public void move(boolean dir) {
        if (Math.abs(velocity.x)<=Enemy.MAX_VELOCITY-Enemy.VELOCITYCNST) {
            if (dir) {
                //velocity.x += Enemy.VELOCITYCNST;
                addVelocity(Enemy.VELOCITYCNST,0);
                if (grounded) state = Enemy.State.Walking;
                facesRight = true;
            } else {
                velocity.x = -Enemy.MAX_VELOCITY;
                if (grounded) state = Enemy.State.Walking;
                facesRight = false;
            }

        }
        ismoving=true;
    }

    public void move (float x){
        ismoving = true;
        velocity.x = Enemy.MAX_VELOCITY*x;
        if (grounded) state = Enemy.State.Walking;
        facesRight = x >= 0;
    }
    public void addVelocity(float x, float y){
        velocity.add(x, y);
        velocity.x = MathUtils.clamp(velocity.x, -MAX_VELOCITY, MAX_VELOCITY);
    }
    public boolean isGrounded(){
        return grounded;
    }
    public void setGrounded(boolean a){
        grounded = a;
    }
    public void setPosition(float x,float y){
        position.set(x,y);
    }
    public Vector2 getPosition(){
        return position;
    }
    public void setHealth(int health){this.health=health;}
    public int getHealth(){return health;}

}
