package com.vakuor.knightsandgoldmines.models;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.vakuor.knightsandgoldmines.GameLogic;

import static com.vakuor.knightsandgoldmines.models.Player.Headstate.Calm;
import static com.vakuor.knightsandgoldmines.models.Player.Headstate.Worried;

public class Player extends Actor {
    public static float WIDTH;
    public static float HEIGHT;
    private static float HeadWIDTH;
    private static float HeadHEIGHT;
    private static float MAX_VELOCITY = 10f;
    public static float MAX_MOVEVELOCITY = 10f;
    private static float VELOCITYCNST = 2f;
    private static float JUMP_VELOCITY = 40f;
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

    private boolean hd = false;
    private boolean facesRight = true;
    public boolean shooting = false;
    private boolean grounded = false;

    private Animation<TextureRegion> stand;
    private Animation<TextureRegion> walk;
    private Animation<TextureRegion> jump;
    private Animation<TextureRegion> head;
    private Animation<TextureRegion> shotwalk;
    private Animation<TextureRegion> shotstand;
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

    public Player(){
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

        stand = new Animation<TextureRegion>(0, standframes);
        jump = new Animation<TextureRegion>(1, jumpframes);
        walk = new Animation<TextureRegion>(0.1f,bodyframes);
        head = new Animation<TextureRegion>(1,headframes);
        shotwalk = new Animation<TextureRegion>(0.1f,shotbodyrunframes);
        shotjump = new Animation<TextureRegion>(0,shotjumpframes);
        shotstand = new Animation<TextureRegion>(0,shotjumpframes);
        bowshot = new Animation<TextureRegion>(0.5f,bowframes);




        walk.setPlayMode(Animation.PlayMode.LOOP);
        shotwalk.setPlayMode(Animation.PlayMode.LOOP);

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
        Player.HeadHEIGHT = 0.05f+position.y+Player.HEIGHT/3+headX;

        TextureRegion frame = null;
        TextureRegion headframe = null;
        TextureRegion bowframe = null;
        if(velocity.y<-5 || velocity.y > 10) state = State.Jumping;
        if(shooting) {
            headstate = Worried;
            bowtime += GameLogic.deltaTime; bowframe = bowshot.getKeyFrame(bowtime);
            System.out.println(bowtime);
            facesRight = GameLogic.touchpadR.getKnobPercentX() >= 0;
        }
        else {headstate = Calm;bowtime=0;}
        switch (state) {
            case Standing:{
                if(shooting){frame = shotstand.getKeyFrame(stateTime);
                }
                else frame = stand.getKeyFrame(stateTime);
                headX=0;
                break;}
            case Walking: {
                if(shooting){ frame = shotwalk.getKeyFrame(stateTime);}
                else {frame = walk.getKeyFrame(stateTime);}
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
                        frame = shotjump.getKeyFrame(0);
                        headX=-0.05f;
                    }
                    else {
                        frame = shotjump.getKeyFrame(0);
                        headX=0f;
                    }
                }
                else {
                    if(velocity.y>=0){
                        frame = jump.getKeyFrame(0);
                        headX=-0.05f;
                    }
                    else if(velocity.y<-30){
                        frame = jump.getKeyFrame(2);

                        headX=0.1f;
                    }
                    else if(velocity.y<0){
                        frame = jump.getKeyFrame(1);

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
    }
    public void move (float x){
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
