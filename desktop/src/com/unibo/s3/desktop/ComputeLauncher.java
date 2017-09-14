package com.unibo.s3.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.*;
import com.unibo.s3.main_system.remote.MainRemote;

import java.awt.*;

/**
 * Launcher che avvia tutto tranne rendering e agenti*/
public class ComputeLauncher {
    public static void main (String[] arg) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        /*LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = width;
        config.height = height;
        config.samples = 4;
        config.vSyncEnabled = false;*/
        //config.fullscreen = true;

        HeadlessNativesLoader.load();
        //MockGraphics mockGraphics = new MockGraphics();
        //Gdx.graphics = mockGraphics;
        HeadlessNet headlessNet = new HeadlessNet();
        Gdx.net = headlessNet;
        HeadlessFiles headlessFiles = new HeadlessFiles();
        Gdx.files = headlessFiles;
        //Gdx.gl = mock(GL20.class);
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        //ApplicationListener myGdxGame = EntryPoint.getHeadlessMyGdxGame(config);
        new HeadlessApplication(new MainRemote(), config);
        //new LwjglApplication(new Main(), config);
    }
}
