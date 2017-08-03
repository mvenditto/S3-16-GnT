package daniele.comunication;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;

import java.util.HashMap;
import java.util.Map;

public class SystemManager {
    private static SystemManager ourInstance = new SystemManager();

    public static SystemManager getInstance() {
        return ourInstance;
    }

    private Map<String, ActorSystem> systemMap;

    private SystemManager() { }

    public void createSystem(String systemName, Config config) {
        if(systemMap == null) {
            systemMap = new HashMap<>();
        }
        this.systemMap.put(systemName, ActorSystem.create(systemName, config));
    }

    public ActorSystem getSystem(String name) {
        return this.systemMap.get(name);
    }
}
