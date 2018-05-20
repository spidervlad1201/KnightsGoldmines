package com.vakuor.knightsandgoldmines.controls;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.vakuor.knightsandgoldmines.GameLogic;

public class WalkingControl extends Actor {

    //размер джоя
    public static  float SIZE = 4f;
    //размер движущейся части (khob)
    public static  float CSIZE = 3f;

    public static  float CIRCLERADIUS = 1.5f;
    public static float  CONTRLRADIUS = 3F;
    //public static float Coefficient = 1F;

    //угол для определения направления
    float angle;
    //public static int Opacity = 1;

    //координаты отклонения khob
    protected Vector2 offsetPosition = new Vector2();

    protected Vector2 position = new Vector2();
    protected Rectangle bounds = new Rectangle();

    public WalkingControl(Vector2 pos){
        this.position = pos;
        this.bounds.width = SIZE;
        this.bounds.height = SIZE;

        getOffsetPosition().x = 0;
        getOffsetPosition().y = 0;

        setHeight(SIZE);
        setWidth(SIZE);
        setX(position.x);
        setY(position.y);

        addListener(new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            //при перетаскивании
            public void touchDragged(InputEvent event, float x, float y, int pointer){

                withControl(x,y);
            }

            //убираем палец с экрана
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {

                getOffsetPosition().x = 0;
                getOffsetPosition().y = 0;
            }

        });
    }



    //отрисовка
    public void draw(SpriteBatch batch) {
        System.out.println("DrawControl");
        batch.draw(GameLogic.controlsframes.get(10), getX(), getY(),getWidth(), getHeight());
        batch.draw(GameLogic.controlsframes.get(0),
                (float)(position.x+WalkingControl.SIZE/2-WalkingControl.CSIZE/2+getOffsetPosition().x),
                (float)(position.y+WalkingControl.SIZE/2-WalkingControl.CSIZE/2+getOffsetPosition().y),
                WalkingControl.CSIZE , WalkingControl.CSIZE );


    }



    public Actor hit(float x, float y, boolean touchable) {
        //Процедура проверки. Если точка в прямоугольнике актёра, возвращаем актёра.
        return x > 0 && x < getWidth() && y> 0 && y < getHeight()?this:null;
    }


    public Vector2 getPosition() {
        return position;
    }
    public Vector2 getOffsetPosition() {
        return offsetPosition;
    }

    public Rectangle getBounds() {
        return bounds;
    }




    public void withControl(float x, float y){

        //точка касания относительно центра джойстика
        float calcX = x -SIZE/2;
        float calcY = y -SIZE/2;

        //определяем лежит ли точка касания в окружности джойстика
        if(((calcX*calcX + calcY* calcY)<=WalkingControl.CONTRLRADIUS*WalkingControl.CONTRLRADIUS)
                ){

            //world.resetSelected();

            //пределяем угол касания
            double angle = Math.atan(calcY/calcX)*180/Math.PI;

            //угол будет в диапозоне [-90;90]. Удобнее работать, если он в диапозоне [0;360]
            //поэтому пошаманим немного
            if(angle>0 &&calcY<0)
                angle+=180;
            if(angle <0)
                if(calcX<0)
                    angle=180+angle;
                else
                    angle+=360;

            //в зависимости от угла указываем направление, куда двигать игрока
            if(angle>40 && angle<140 && GameLogic.player.isGrounded()) {
                GameLogic.player.jump();
            }
            /*if(angle>220 && angle<320)
                ((GameLogic.Player)world.selectedActor).downPressed();*/


            if(angle>130 && angle<230)
            {
                GameLogic.player.move(false);
            }

            if(angle<50 || angle>310)
            {
                GameLogic.player.move(true);
            }


            //двигаем игрока


            angle = (float)(angle*Math.PI/180);
            //getOffsetPosition().x = (float)((calcX*calcX + calcY* calcY>1F)? Math.cos(angle)*0.75F: calcX);
            //getOffsetPosition().y = (float)((calcX*calcX + calcY* calcY>1F)? Math.sin(angle)*0.75F: calcY);

        }
        else{

            getOffsetPosition().x = 0;
            getOffsetPosition().y = 0;
        }

    }
}