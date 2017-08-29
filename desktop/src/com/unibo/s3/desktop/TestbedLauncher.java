package com.unibo.s3.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.unibo.s3.testbed.FutureTestbed;

public class TestbedLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1024;
		config.height = 700;
		config.samples = 4;
		config.vSyncEnabled = true;
		new LwjglApplication(new FutureTestbed(), config);
	}
}
