package com.unibo.s3.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.unibo.s3.Main;

import java.awt.*;

public class DesktopLauncher {
	public static void main (String[] arg) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = width;
		config.height = height;
		config.samples = 4;
		config.vSyncEnabled = false;
		//config.fullscreen = true;
		String ip = arg.length == 0 ? "" : arg[0];
		new LwjglApplication(new Main(ip), config);
	}
}
