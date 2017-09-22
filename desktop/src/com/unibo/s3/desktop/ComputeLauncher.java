package com.unibo.s3.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.*;
import com.unibo.s3.main_system.remote.MainRemote;

import java.awt.*;

/**
 * Launcher che avvia tutto tranne rendering e agenti*/
public class ComputeLauncher {
    public static void main (String[] arg) {
        HeadlessNativesLoader.load();
        Gdx.net = new HeadlessNet();
        Gdx.files = new HeadlessFiles();
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new MainRemote(), config);
    }
}
