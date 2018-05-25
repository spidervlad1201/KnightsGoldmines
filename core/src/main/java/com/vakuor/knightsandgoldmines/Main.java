package com.vakuor.knightsandgoldmines;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.vakuor.knightsandgoldmines.view.Menu;

public class Main extends Game {

    public SpriteBatch batch;
    public BitmapFont font;
    public AssetManager manager;
    public static int fps;
    private int frames;
    public static float aspectRatio;

    long javaHeap;//лишнее
    long nativeHeap;//лишнее

    @Override
    public void create () {
        System.out.println("Main.create\n");
        batch = new SpriteBatch();
        font = new BitmapFont();
        fps = 60;
        manager = new AssetManager();
        this.setScreen(new Menu(this));
    }

    @Override
    public void render () {
//		Gdx.gl.glClearColor(0, 0, 0, 1);
//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
        super.render();
        frames = Gdx.graphics.getFramesPerSecond();
        if(frames>0 && frames < Integer.MAX_VALUE)
            fps = frames;
        javaHeap = Gdx.app.getJavaHeap();//потребление памяти Java//лишнее
        nativeHeap = Gdx.app.getNativeHeap();//нативной heap памяти//лишнее
    }
    @Override
    public void dispose () {
        font.dispose();
        batch.dispose();
    }
}
