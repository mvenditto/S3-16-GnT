package com.unibo.s3.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.unibo.s3.main_system.communication.AgentsTest;
import com.unibo.s3.main_system.communication.CommunicationTest;
import com.unibo.s3.main_system.graph.GraphTest;


public class CommunicationLauncher {
    public static void main (String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1024;
        config.height = 768;
        config.samples = 4;
        config.vSyncEnabled = true;
        new LwjglApplication(new CommunicationTest(), config);
        //new LwjglApplication(new AgentsTest(), config); //test sugli agenti
        //new LwjglApplication(new GraphTest(), config); //test sugli agenti
    }
}
