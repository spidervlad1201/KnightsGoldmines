package com.vakuor.knightsandgoldmines.models;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import static com.vakuor.knightsandgoldmines.models.Player.Headstate.Calm;
import static com.vakuor.knightsandgoldmines.models.Player.Headstate.Damn;
import static com.vakuor.knightsandgoldmines.models.Player.Headstate.Worried;

public class Player extends Actor {
    public static float WIDTH;
    public static float HEIGHT;
    private static float HeadWIDTH;
    private static float HeadHEIGHT;
    private static float MAX_VELOCITY = 15f;
    public static float MAX_MOVEVELOCITY = 10f;
    private static float VELOCITYCNST = 2f;
    private static float JUMP_VELOCITY = 50f;//40f
    public static float DAMPING = 0.87f;

    enum State {
        Standing, Walking, Jumping
    }
    enum Headstate {
        Calm, Worried, Damn
    }

    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    private State state = State.Walking;
    private Headstate headstate = Headstate.Calm;
    private float stateTime = 0;
    private float dlt = 0;
    private float headX=0;
    private float bowtime=0;
    private float attacktime=0;
    private float climbconst = 0.2f;
    private float climbingTime = 0;
    private float attackingtime = 0.15f;
    private float attackingtimemax = attackingtime*3;

    private float damntime=2;
    private float damntimemax=1.5f;

    public static float hitconst = 20f;

    private boolean hd = false;
    private boolean facesRight = true;
    public boolean shooting = false;
    public boolean attacking = false;
    public boolean attackType = false;
    private boolean grounded = false;
    private boolean climb = true;
    private boolean isclimbing = false;
    public boolean ismoving = false;

    public static boolean attacked = false;

    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };
    private Array<Rectangle> tiles = new Array<Rectangle>();
    private int startX, startY, endX, endY;


    private BitmapFont font;
    private int health=0;

    private Animation<TextureRegion> stand;
    private Animation<TextureRegion> walk;
    private Animation<TextureRegion> jump;
    private Animation<TextureRegion> head;
    private Animation<TextureRegion> shotwalk;
    private Animation<TextureRegion> shotstand;
    private Animation<TextureRegion> attackstand;
    private Animation<TextureRegion> shotjump;
    private Animation<TextureRegion> bowshot;
    private TextureAtlas playerTextureAtlas;
    private TextureAtlas headsTextureAtlas;
    private Array<TextureAtlas.AtlasRegion> bodyframes;
    private Array<TextureAtlas.AtlasRegion> standframes;
    private Array<TextureAtlas.AtlasRegion> jumpframes;
    private Array<TextureAtlas.AtlasRegion> headframes;

    private Array<TextureAtlas.AtlasRegion> shotbodyrunframes;
    private Array<TextureAtlas.AtlasRegion> shotframe;
    private Array<TextureAtlas.AtlasRegion> shotjumpframes;
    private Array<TextureAtlas.AtlasRegion> bowframes;
    private Array<TextureAtlas.AtlasRegion> attackframes;

    public Player(int x, int y){
        setPosition(x,y);
        font = new BitmapFont();
        playerTextureAtlas = new TextureAtlas("visual/output/Archer/Archers2.atlas");
        headsTextureAtlas = new TextureAtlas("visual/output/Head/Heads.atlas");
        bodyframes =  playerTextureAtlas.findRegions("body");
        standframes =  playerTextureAtlas.findRegions("idle");
        jumpframes =  playerTextureAtlas.findRegions("jump");
        headframes = headsTextureAtlas.findRegions("2");
        shotbodyrunframes = playerTextureAtlas.findRegions("shotBodyRun");
        shotframe = playerTextureAtlas.findRegions("shot");
        shotjumpframes = playerTextureAtlas.findRegions("shotBody");
        bowframes = playerTextureAtlas.findRegions("arm");
        attackframes = playerTextureAtlas.findRegions("attackBody");

        stand = new Animation<TextureRegion>(0, standframes);
        jump = new Animation<TextureRegion>(1, jumpframes);
        walk = new Animation<TextureRegion>(0.1f,bodyframes);
        head = new Animation<TextureRegion>(1,headframes);
        shotwalk = new Animation<TextureRegion>(0.1f,shotbodyrunframes);
        shotjump = new Animation<TextureRegion>(0,shotjumpframes);
        shotstand = new Animation<TextureRegion>(0,shotjumpframes);
        attackstand = new Animation<TextureRegion>(attackingtime,attackframes);
        bowshot = new Animation<TextureRegion>(0.5f,bowframes);




        walk.setPlayMode(Animation.PlayMode.LOOP);
        shotwalk.setPlayMode(Animation.PlayMode.LOOP);
        attackstand.setPlayMode(Animation.PlayMode.NORMAL);

        Player.WIDTH = 1 / 16f * bodyframes.get(0).getRegionWidth();
        Player.HEIGHT = 1 / 16f * bodyframes.get(0).getRegionHeight();
        Player.HeadWIDTH = 0.05f;
        Player.HeadHEIGHT = 0.05f;
    }
    public void setPosition(float x,float y){
        position.set(x,y);
    }
    public Vector2 getPosition(){
        return position;
    }
    public void setHealth(int health){this.health=health;}
    public int getHealth(){return health;}

    public void update(){
        //System.out.println(touchpad.getKnobPercentX()+ " " + touchpad.getKnobPercentY());
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
                    //addVelocity(0,climbconst);
                    isclimbing=true;
                    velocity.y=climbconst;
                    setGrounded(false);
                }
                //else isclimbing = false;
                //System.out.println(isclimbing);
                break;
            }
            //else System.out.println("NO");
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
    public void attack(){

    }
    public void hit(boolean mydir){
        System.out.println("GO");
        if(mydir){addVelocity(2*hitconst,hitconst);}
        else {addVelocity(-2*hitconst,hitconst);}
        health-=1;
        GameLogic.healthLabel.setText(String.valueOf(health));
        headstate=Damn;
        damntime=0f;
    }
    @Override
    public void draw(Batch batch, float parentAlpha) {
        Player.HeadHEIGHT = 0.05f+position.y+Player.HEIGHT/3+headX;

        TextureRegion frame = null;
        TextureRegion headframe = null;
        TextureRegion bowframe = null;
        if(velocity.y<-5 || velocity.y > 10) state = State.Jumping;
        if(shooting) {
            headstate = Worried;
            if(attackType){bowtime += GameLogic.deltaTime; bowframe = bowshot.getKeyFrame(bowtime);attacktime=0;}
            else {attacktime += GameLogic.deltaTime;bowtime=0; }
            //System.out.println(bowtime);
            facesRight = GameLogic.touchpadR.getKnobPercentX() >= 0;
        }
        else {if(damntime>damntimemax){headstate = Calm;}
        bowtime=0;attacktime=0;}

        switch (state) {
            case Standing:{
                if(shooting && attackType){frame = shotstand.getKeyFrame(stateTime);
                }
                else if(shooting && !attackType){frame = attackstand.getKeyFrame(attacktime);
                    //System.out.println("1");
                }
                else
                {frame = stand.getKeyFrame(stateTime);}
                headX=0;
                break;}
            case Walking: {
                if(shooting && attackType){ frame = shotwalk.getKeyFrame(stateTime);//System.out.println("2");
                     }
                else if(shooting && !attackType && attacktime<=attackingtimemax){ frame = attackstand.getKeyFrame(attacktime);
                    //System.out.println("3");
                }
                else {frame = walk.getKeyFrame(stateTime);//System.out.println("4");
                }
                if ((stateTime-dlt)>0.5f){
                    dlt = stateTime;
                    if (hd) {
                        headX=0.05f;
                        hd = false;
                    }
                    else {headX=0.1f;
                        hd=true;}
                }
                break;
            }
            case Jumping:{
                if(shooting){
                    if(velocity.y>=0){
                        if(attackType){frame = shotjump.getKeyFrame(0);//System.out.println("5");
                        }
                        else {frame = attackstand.getKeyFrame(attacktime);//System.out.println("6");
                        }
                        headX=-0.05f;
                    }
                    else {
                        if(attackType){frame = shotjump.getKeyFrame(0);//System.out.println("7");
                        }
                        else {frame = attackstand.getKeyFrame(attacktime);//System.out.println("8");
                        }
                        headX=0f;
                    }
                }
                else {
                    if(velocity.y>=0){
                        frame = jump.getKeyFrame(0);
                        //System.out.println("9");
                        headX=-0.05f;
                    }
                    else if(velocity.y<-30){
                        frame = jump.getKeyFrame(2);
                        //System.out.println("10");

                        headX=0.1f;
                    }
                    else if(velocity.y<0){
                        frame = jump.getKeyFrame(1);
                        //System.out.println("11");

                        headX=0.05f;
                    }
                }
                break;
            }
        }
        switch (headstate) {
            case Calm:
                headframe = head.getKeyFrame(0);
                break;
            case Worried:
                headframe = head.getKeyFrame(1);
                break;
            case Damn:{
                damntime+=GameLogic.deltaTime;
                headframe = head.getKeyFrame(2);
                break;
            }
        }
        if (facesRight) {
            batch.draw(frame,  position.x-Player.WIDTH/2,  position.y-Player.HEIGHT/2-0.5f/Player.HEIGHT+0.2f, Player.WIDTH*2, Player.HEIGHT*2-0.5f);
            batch.draw(headframe,  position.x+HeadWIDTH,  HeadHEIGHT-0.15f, Player.WIDTH, Player.HEIGHT-0.05f);
            if(bowframe!=null)batch.draw(bowframe,  position.x-Player.WIDTH/2+0.2f,  position.y-Player.HEIGHT/2-0.5f/Player.HEIGHT+0.5f, Player.WIDTH*2, Player.HEIGHT*2-0.5f);

        } else {
            batch.draw(frame,  position.x + 1.5f*Player.WIDTH,  position.y-Player.HEIGHT/2-0.5f/Player.HEIGHT+0.2f, -Player.WIDTH*2, Player.HEIGHT*2-0.5f);
            batch.draw(headframe,  position.x+Player.WIDTH-HeadWIDTH,  HeadHEIGHT-0.15f, -Player.WIDTH, Player.HEIGHT-0.05f);
            if(bowframe!=null)batch.draw(bowframe,  position.x + 1.5f*Player.WIDTH-0.2f,  position.y-Player.HEIGHT/2-0.5f/Player.HEIGHT+0.5f, -Player.WIDTH*2, Player.HEIGHT*2-0.5f);
        }

    }

    public void jump(){
        velocity.y += Player.JUMP_VELOCITY;
        state = Player.State.Jumping;
        grounded = false;
    }
    public void move(boolean dir) {
        if (Math.abs(velocity.x)<=Player.MAX_VELOCITY-Player.VELOCITYCNST) {
            if (dir) {
                //velocity.x += Player.VELOCITYCNST;
                addVelocity(Player.VELOCITYCNST,0);
                if (grounded) state = Player.State.Walking;
                facesRight = true;
            } else {
                velocity.x = -Player.MAX_VELOCITY;
                if (grounded) state = Player.State.Walking;
                facesRight = false;
            }

        }
        ismoving=true;
    }
    public void move (float x){
        ismoving = true;
        velocity.x = Player.MAX_VELOCITY*x;
        if (grounded) state = Player.State.Walking;
        facesRight = x >= 0;
    }
    public boolean isGrounded(){
        return grounded;
    }
    public void setGrounded(boolean a){
        grounded = a;
    }
    @Override
    public void act(float deltaTime) {

        update();

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
            if (grounded) state = Player.State.Standing;
        }
        super.act(deltaTime);

    }
    public void addVelocity(float x, float y){
        velocity.add(x, y);
        velocity.x = MathUtils.clamp(velocity.x, -MAX_VELOCITY, MAX_VELOCITY);
    }

}
