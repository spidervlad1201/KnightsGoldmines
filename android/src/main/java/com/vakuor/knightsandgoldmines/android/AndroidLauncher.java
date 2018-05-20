package com.vakuor.knightsandgoldmines.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.vakuor.knightsandgoldmines.GameLogic;
import com.vakuor.knightsandgoldmines.Main;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        config.useCompass = false; // компас и аксель рекомендуется вырубать
        config.useAccelerometer = false;
        config.hideStatusBar = true;
        //config.useRotationVectorSensor = true; // разобраться с ориентацией экрана

        initialize(new GameLogic(), config);
    }
}