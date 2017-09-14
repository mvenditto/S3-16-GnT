package com.unibo.s3.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.unibo.s3.main_system.tests.Tests;

public class TestsLauncher {
    public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1024;
        config.height = 600;
        config.samples = 4;
        config.vSyncEnabled = true;
        new LwjglApplication(new Tests(), config);
    }
}
