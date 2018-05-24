package com.vakuor.knightsandgoldmines.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

import static com.vakuor.knightsandgoldmines.Main.aspectRatio;

public class TouchCreator {
    private Touchpad touchpad;
    private Touchpad.TouchpadStyle touchpadStyle;
    private Skin touchpadSkin;
    private Drawable touchBackground;
    private Drawable touchKnob;
    private Array<Drawable> drawableArray;
    private float touchScale = 1f;

    public TouchCreator(String... strings){
        touchpadSkin = new Skin();
        touchpadSkin.add("touchBackground",new Texture(Gdx.files.internal("visual/input/flatDark/flatDark_10.png")));
        touchpadSkin.add("touchKnob",new Texture(Gdx.files.internal("visual/input/flatDark/flatDark_00.png")));
        touchpadStyle = new Touchpad.TouchpadStyle();
        touchBackground = touchpadSkin.getDrawable("touchBackground");
        touchKnob = touchpadSkin.getDrawable("touchKnob");
        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;
        touchpad = new Touchpad(10,touchpadStyle);
        touchScale=0.2f;
        touchpad.setBounds(15,15,Gdx.graphics.getWidth()*touchScale,Gdx.graphics.getHeight()*aspectRatio*touchScale);

    }
}
