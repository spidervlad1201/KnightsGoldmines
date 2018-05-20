package com.vakuor.knightsandgoldmines.models;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.vakuor.knightsandgoldmines.GameLogic;

public class Player extends Actor {
    public static float WIDTH;
    public static float HEIGHT;
    public static float HeadWIDTH;
    public static float HeadHEIGHT;
    public static float MAX_VELOCITY = 10f;
    public static float JUMP_VELOCITY = 40f;
    public static float DAMPING = 0.87f;

    enum State {
        Standing, Walking, Jumping
    }
    enum Headstate {
        Calm, Worried, Damn
    }

    public final Vector2 position = new Vector2();
    public final Vector2 velocity = new Vector2();
    State state = State.Walking;
    Headstate headstate = Headstate.Calm;
    float stateTime = 0;
    boolean facesRight = true;
    boolean grounded = false;

    private Animation<TextureRegion> stand;
    private Animation<TextureRegion> walk;
    private Animation<TextureRegion> jump;
    private Animation<TextureRegion> head;
    private TextureAtlas playerTextureAtlas;
    private TextureAtlas headsTextureAtlas;
    private Array<TextureAtlas.AtlasRegion> bodyframes;
    private Array<TextureAtlas.AtlasRegion> standframes;
    private Array<TextureAtlas.AtlasRegion> jumpframes;
    private Array<TextureAtlas.AtlasRegion> headframes;

    public Player(){
        playerTextureAtlas = new TextureAtlas("visual/output/Archer/Archers.atlas");
        headsTextureAtlas = new TextureAtlas("visual/output/Head/Heads.atlas");
        bodyframes =  playerTextureAtlas.findRegions("body");
        standframes =  playerTextureAtlas.findRegions("idle");
        jumpframes =  playerTextureAtlas.findRegions("jump");
        headframes = headsTextureAtlas.findRegions("2");


        stand = new Animation<TextureRegion>(0, standframes);
        jump = new Animation<TextureRegion>(1, jumpframes);
        walk = new Animation<TextureRegion>(0.1f,bodyframes);
        head = new Animation<TextureRegion>(0,headframes);

        walk.setPlayMode(Animation.PlayMode.LOOP);

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



    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion frame = null;
        TextureRegion headframe = null;
        if(velocity.y<-10 || velocity.y > 10) state = State.Jumping;
        switch (state) {
            case Standing:
                frame = stand.getKeyFrame(stateTime);
                break;
            case Walking: {
                frame = walk.getKeyFrame(stateTime);

                break;
            }
            case Jumping:{
                if(velocity.y>=0){
                    frame = jump.getKeyFrame(0);
                }
                else if(velocity.y<-30){
                    frame = jump.getKeyFrame(2);
                }
                else if(velocity.y<0){
                    frame = jump.getKeyFrame(1);
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
                headframe = head.getKeyFrame(2);
                break;
            }
        }
        if (facesRight) {
            batch.draw(frame,  position.x-Player.WIDTH/2,  position.y-Player.HEIGHT/2-0.5f/Player.HEIGHT, Player.WIDTH*2, Player.HEIGHT*2);
            batch.draw(headframe,  position.x+HeadWIDTH,  position.y+Player.HEIGHT/3+HeadHEIGHT, Player.WIDTH, Player.HEIGHT);
        } else {
            batch.draw(frame,  position.x + 1.5f*Player.WIDTH,  position.y-Player.HEIGHT/2-0.5f/Player.HEIGHT, -Player.WIDTH*2, Player.HEIGHT*2);
            batch.draw(headframe,  position.x + 1.5f*Player.WIDTH,  position.y-Player.HEIGHT/2-0.5f/Player.HEIGHT, -Player.WIDTH*2, Player.HEIGHT*2);
        }
    }

    public void jump(){
        velocity.y += Player.JUMP_VELOCITY;
        state = Player.State.Jumping;
        grounded = false;
    }
    public void move(boolean dir){
        if(dir){
            velocity.x = Player.MAX_VELOCITY;
            if (grounded) state = Player.State.Walking;
            facesRight = true;
        }
        else {
            velocity.x = -Player.MAX_VELOCITY;
            if (grounded) state = Player.State.Walking;
            facesRight = false;
        }
    }
    public boolean isGrounded(){
        return grounded;
    }
    public void setGrounded(boolean a){
        grounded = a;
    }
    @Override
    public void act(float deltaTime) {

        if (deltaTime == 0) return;

        if (deltaTime > 0.1f)
            deltaTime = 0.1f;

        stateTime += deltaTime;

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
