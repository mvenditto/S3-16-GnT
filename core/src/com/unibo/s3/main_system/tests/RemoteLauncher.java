package com.unibo.s3.main_system.tests;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.unibo.s3.main_system.AbstractMainApplication;
import com.unibo.s3.main_system.communication.SystemManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RemoteLauncher extends AbstractMainApplication {

    @Override
    public void create() {
        super.create();
        String confText = null;
        try {
            confText = "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
                    "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
                    ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
                    ",\"netty\":{\"tcp\":{\"hostname\":\""+ InetAddress.getLocalHost().getHostAddress()+"\",\"port\":2727}}}}}";
            Config customConf = ConfigFactory.parseString(confText);
            SystemManager.createSystem("RemoteSystem", customConf);
            SystemManager.createActor(TestActor.props(), "remoteActor");
            System.out.println("remote ready, ip: " + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void doRender() {

    }

    @Override
    public void doCustomRender() {

    }

    @Override
    public void doUpdate(float delta) {

    }
}
