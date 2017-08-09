package daniele.comunication;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import com.typesafe.config.Config;
import javafx.util.Pair;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SystemManager {
    private static SystemManager ourInstance = new SystemManager();

    public static SystemManager getInstance() {
        return ourInstance;
    }

    private ActorSystem system;
    private List<Pair<String, ActorRef>> actorList;

    private SystemManager() { }

    public void createSystem(String systemName, Config config) {
        this.system = ActorSystem.create(systemName, config);
    }

    public ActorRef createActor(Props props, String actorName) {
        if(this.actorList == null) {
            this.actorList = new ArrayList<>();
        }
        ActorRef ref = this.system.actorOf(props, actorName);
        this.actorList.add(new Pair<>(actorName, ref));
        return ref;
    }

    public ActorRef getLocalActor(String actorName) {
        for(Pair<String, ActorRef> pair: this.actorList) {
            if(pair.getKey().equals(actorName)) {
                return pair.getValue();
            }
        }
        return null;
    }

    public ActorSelection getRemoteActor(String systemName, String ip, String portNumber, String path) {
        return this.system.actorSelection("akka.tcp://" + systemName + "@" + ip + ":" + portNumber + path);
    }
}
