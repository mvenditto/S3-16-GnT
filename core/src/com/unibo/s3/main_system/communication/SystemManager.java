package com.unibo.s3.main_system.communication;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SystemManager {
    private static SystemManager ourInstance = new SystemManager();

    public static SystemManager getInstance() {
        return ourInstance;
    }

    private ActorSystem system;
    private Map<String, ActorRef> actorList;

    private SystemManager() { }

    public void createSystem(String systemName, Config config) {
        this.system = ActorSystem.create(systemName, config);
    }

    public ActorRef createActor(Props props, String actorName) {
        if(this.actorList == null) {
            this.actorList = new HashMap<>();
        }
        ActorRef ref = this.system.actorOf(props, actorName);
        this.actorList.put(actorName, ref);
        return ref;
    }

    public ActorRef getLocalActor(String actorName) {
        return  this.actorList.entrySet().stream().filter(x -> x.getKey().equals(actorName)).findFirst().get().getValue();
    }

    public ActorSelection getRemoteActor(String systemName, String ip, String portNumber, String path) {
        StringBuilder tmp = new StringBuilder(60);
        tmp.append("akka.tcp://");
        tmp.append(systemName);
        tmp.append("@");
        tmp.append(ip);
        tmp.append(":");
        tmp.append(portNumber);
        tmp.append(path);
        return this.system.actorSelection(tmp.toString());
    }
}
