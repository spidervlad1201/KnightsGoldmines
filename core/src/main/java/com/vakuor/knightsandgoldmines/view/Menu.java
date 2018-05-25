package com.vakuor.knightsandgoldmines.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.vakuor.knightsandgoldmines.GameLogic;
import com.vakuor.knightsandgoldmines.Main;

public class Menu implements Screen {

    private final Main game;

    Music menuMusic;
    public static int screenWidth;
    public static int screenHeight;
    //float aspectRatio = 1;
    Texture wallpaper;
    float elapsed;

    Stage stage;
    private Viewport viewport;
    private OrthographicCamera camera;


    Animation<TextureRegion> animation;

    public Menu(final Main game){
        System.out.println("Menu.constructor\n");
        this.game = game;
        camera = new OrthographicCamera();
        //viewport = new ScreenViewport(camera);
        game.manager.load("visual/images/wallpaper.jpg",Texture.class);
        game.manager.load("visual/music/mainmenutheme.wav",Music.class);
        game.manager.finishLoading();

        wallpaper = game.manager.get("visual/images/wallpaper.jpg");

        camera.setToOrtho(false, 800, 480);

        menuMusic = game.manager.get("visual/music/mainmenutheme.wav");
        //menuMusic = Gdx.audio.newMusic(Gdx.files.internal("visual/music/mainmenutheme.wav"));
        menuMusic.setLooping(true);

    }

    @Override
    public void show() {
        System.out.println("Menu.show\n");
        screenWidth  = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        System.out.println(screenWidth+"x"+screenHeight);
        game.aspectRatio = (float) screenWidth/screenHeight;
    }

    @Override
    public void render(float delta) {
        //System.out.println("Menu.render\n");
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        elapsed += delta;

        menuMusic.play();
        //animator.render();
        game.batch.begin();


        game.batch.draw(wallpaper,0,0,screenWidth,screenHeight);
        game.font.draw(game.batch, "Welcome to Knights and Goldmines", 100, 150);
        game.font.draw(game.batch, "Tap anywhere to begin", 100, 100);

        //game.batch.draw(animation.getKeyFrame(elapsed),0,0);
        game.batch.end();

        if (Gdx.input.isTouched() || Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            new GameLogic();
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        //aspectRatio = (float) width/height;
        screenHeight = height;
        screenWidth = width;
        game.aspectRatio = (float) screenWidth/screenHeight;
        //viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        //game.dispose();

        menuMusic.dispose();
    }

}
