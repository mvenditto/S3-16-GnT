package daniele.comunication;

import akka.actor.ActorRef;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import daniele.utils.IntMsg;
import org.junit.Test;

import java.net.Inet4Address;

public class DistributedTest {

    @Test
    public void testCommunicationBetweenSystems() {
        try {
            String confText =
                    "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
                            "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
                            ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
                            ",\"netty\":{\"tcp\":{\"hostname\":\""+ Inet4Address.getLocalHost().getHostAddress()+"\",\"port\":2727}}}}}";
            Config customConf = ConfigFactory.parseString(confText);
            SystemManager.getInstance().createSystem("RemoteSystem", customConf);
            ActorRef remote = SystemManager.getInstance().getSystem("RemoteSystem").actorOf
                    (LocalActor.props("Local"), "local");
            customConf = ConfigFactory.parseString(confText.replace("2727", "5050"));
            SystemManager.getInstance().createSystem("LocalSystem", customConf);
            ActorRef local = SystemManager.getInstance().getSystem("LocalSystem").actorOf
                    (LocalActor.props("Remote"), "remote");
            remote.tell(new IntMsg(0), local);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCommunicationBetweenSameSystem() {
        try {
            SystemManager.getInstance().createSystem("LocalSystem", null);
            ActorRef firstLocal = SystemManager.getInstance().getSystem("LocalSystem").actorOf
                    (LocalActor.props("First Local"), "firstLocal");
            ActorRef SecondLocal = SystemManager.getInstance().getSystem("LocalSystem").actorOf
                    (LocalActor.props("Second Local"), "secondLocal");
            firstLocal.tell(new IntMsg(0), SecondLocal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
