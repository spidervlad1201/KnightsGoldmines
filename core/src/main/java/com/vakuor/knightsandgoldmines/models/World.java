package com.vakuor.knightsandgoldmines.models;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class World extends Stage {


    public float ppuX;
    public float ppuY;
    //выбранный актёр
    public Actor selectedActor = null;
    //регионы
    public static float CAMERA_WIDTH = 12f;
    public static  float CAMERA_HEIGHT = 8f;
    //обновление положения объектов
    public void update(float delta){
        for(Actor actor: this.getActors())
            if(actor instanceof Player)
                ((Player)actor).update(delta);
    }

    public World(int x, int y, boolean b, SpriteBatch spriteBatch){
        super(x,y, b, spriteBatch);
        ppuX = getWidth() / CAMERA_WIDTH;
        ppuY = getHeight() / CAMERA_HEIGHT;
        //добавим двух персонажей
        addActor(new Player(new Vector2(4,2), this));
        addActor(new Player(new Vector2(4,4), this));


        //контрол как актёр
        addActor(new WalkingControl(new Vector2(0F,0F),this));
    }

    public void setPP(float x, float y){
        ppuX = x;
        ppuY = y;
    }


    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        super.touchDown(x, y, pointer, button);

        // передвигаем выбранного актёра
        //moveSelected(x, y);

        return true;
    }

	/*
	private void moveSelected(float x, float y){
		if(selectedActor != null && selectedActor instanceof Player)
		{
			((Player)selectedActor).ChangeNavigation(x,this.getHeight() -y);
		}
	}*/

    /**
     * Сбрасываем текущий вектор и направление движения
     */
    public void resetSelected(){
        if(selectedActor != null && selectedActor instanceof Player)
        {
            ((Player)selectedActor).resetWay();
        }
    }

    @Override
    public boolean touchUp (int x, int y, int pointer, int button) {
        super.touchUp(x, y, pointer, button);
        resetSelected();
        return true;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if(selectedActor != null)
            super.touchDragged(x, y, pointer);

        return true;
    }

    public Actor hit(float x, float y, boolean touchable) {

        Actor  actor  = super.hit(x,y,touchable);
        //если выбрали актёра
        if(actor != null &&  actor instanceof Player)
            //запоминаем
            selectedActor = actor;
        return actor;

    }

}
