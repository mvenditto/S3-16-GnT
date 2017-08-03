package daniele.comunication;

import akka.actor.ActorRef;
import akka.actor.Props;
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
            ActorRef remote = SystemManager.getInstance().getSystem("RemoteSystem").actorOf(Props.create(RemoteActor.class),"remote");
            customConf = ConfigFactory.parseString(confText.replace("2727", "5050"));
            SystemManager.getInstance().createSystem("LocalSystem", customConf);
            ActorRef local = SystemManager.getInstance().getSystem("LocalSystem").actorOf(Props.create(LocalActor.class), "local");
            remote.tell(new IntMsg(0), local);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCommunicationBetweenSameSystem() {
        try {
            String confText =
                    "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
                            "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
                            ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
                            ",\"netty\":{\"tcp\":{\"hostname\":\"" + Inet4Address.getLocalHost().getHostAddress() + "\",\"port\":2727}}}}}";
            Config customConf = ConfigFactory.parseString(confText);
            SystemManager.getInstance().createSystem("LocalSystem", customConf);
            ActorRef local1 = SystemManager.getInstance().getSystem("LocalSystem").actorOf(Props.create(LocalActor.class), "local1");
            ActorRef local2 = SystemManager.getInstance().getSystem("LocalSystem").actorOf(Props.create(LocalActor.class), "local2");
            local1.tell(new IntMsg(0), local2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
