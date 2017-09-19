package com.unibo.s3.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.unibo.s3.Main;
import com.unibo.s3.main_system.communication.SystemManager;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
		String ip = null;
		try {
			ip = arg.length == 0 ? InetAddress.getLocalHost().getHostAddress() : arg[0];
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		SystemManager.setIPForRemoting(ip);
		new LwjglApplication(new Main(), config);
	}
}
