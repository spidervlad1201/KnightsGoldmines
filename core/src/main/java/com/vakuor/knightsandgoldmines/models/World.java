package com.vakuor.knightsandgoldmines.models;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class World extends Stage {


    //выбранный актёр
    public Actor selectedActor = null;
    //регионы
    //обновление положения объектов
    public void update(float delta){
        for(Actor actor: this.getActors()){}
            //actor.update(delta);
    }

    public World(int x, int y, boolean b, SpriteBatch spriteBatch){
        //добавим двух персонажей
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
        return actor;

    }

}
