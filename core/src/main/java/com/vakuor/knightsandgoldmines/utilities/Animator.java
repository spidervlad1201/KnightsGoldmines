package com.vakuor.knightsandgoldmines.utilities;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Animator implements ApplicationListener {

    private static final int FRAME_COLS = 6;
    private static final int FRAME_ROWS = 5;

    public Animation walkAnimation;
    public Texture walkSheet;
    public TextureRegion[] walkFrames;
    public SpriteBatch spriteBatch;
    public TextureRegion currentFrame;

    private Array<TextureAtlas.AtlasRegion> frames;

    private int indexes;
    private int index = 0;

    private Animation animation;
    private TextureAtlas textureAtlas;
    private Array<TextureAtlas.AtlasRegion> atlasRegions;
    private float stateTime;

    private boolean alive = false;

    @Override
    public void create() {

        walkSheet = new Texture(Gdx.files.internal("animation_sheet.png"));
        TextureRegion[][] tmp = TextureRegion.split(walkSheet, walkSheet.getWidth()/FRAME_COLS, walkSheet.getHeight()/FRAME_ROWS);
        walkFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
        int index = 0;
        for(int i = 0; i < FRAME_ROWS; i++){
            for(int j = 0; j< FRAME_COLS; j++){
                walkFrames[index++] = tmp[i][j];
            }
        }
        walkAnimation = new Animation(0.025f, walkFrames);
        spriteBatch = new SpriteBatch();
        stateTime = 0f;
    }

//    public void create(String action, float frameTime){
//        System.out.println("Animator.create "+action+" "+frameTime+"\n");
//        //textureAtlas = new TextureAtlas(internalPackFile);
//        frames =  textureAtlas.findRegions(action);
//        indexes = frames.size;
//        animation = new Animation(frameTime,frames);
//        stateTime = 0f;
//    }

    public Animation create(String action, float frameTime){

        //textureAtlas = new TextureAtlas("visual/output/Archer/Archers.atlas");
        frames =  textureAtlas.findRegions(action);
        return new Animation(frameTime, frames);
    }

    public Animation create(TextureAtlas textureAtlas, String action, float frameTime){//Vector a
        this.textureAtlas = textureAtlas;
        textureAtlas.dispose();
        create(action, frameTime);
        animation = create(action, frameTime);
        return animation;
    }

    public Animation create(String internalPackFile, String action, float frameTime){
        textureAtlas = new TextureAtlas(internalPackFile);
        animation = create(action, frameTime);
        return animation;
    }

    private void iterat(){///////////////////////////////////////////////
       /* Iterator<TextureAtlas.AtlasRegion> reg = atlasRegions.iterator();
        while (reg.hasNext()) {
            TextureAtlas.AtlasRegion currentReg = reg.next();
            //raindrop.y -= 200 * Gdx.graphics.getDeltaTime();

            //reg.remove();
        }*/
        /*while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
            if (raindrop.y + 64 < 0)
                iter.remove();
            if (raindrop.overlaps(bucket)) {
                dropsGathered++;
                dropSound.play();
                iter.remove();
            }
        }*/
    }


    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        System.out.println("Animator.render");
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        stateTime += Gdx.graphics.getDeltaTime();

        //currentFrame = (TextureRegion) walkAnimation.getKeyFrame(stateTime, true);

        //frames.get();
        if((frames.get(index))==null) {
            System.out.println("not null");
        }
        else System.out.println("null");
        spriteBatch.begin();
        //spriteBatch.draw(currentFrame, 50, 50);
        spriteBatch.draw(frames.get(index),5,5);
        spriteBatch.end();
        if(index < indexes)index++;
        else index = 0;
    }

    public void render(boolean a) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        stateTime += Gdx.graphics.getDeltaTime();

        //currentFrame = (TextureRegion) animation.getKeyFrame(stateTime, true);


        spriteBatch.begin();
        spriteBatch.draw(currentFrame, 50, 50);
        spriteBatch.end();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    public boolean isAlive(){return alive;}
    public void setAlive(boolean alive){this.alive = alive;}

    @Override
    public void dispose() {

    }
}
