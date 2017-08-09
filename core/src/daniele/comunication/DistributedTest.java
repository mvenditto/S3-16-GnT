package daniele.comunication;

import akka.actor.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import daniele.comunication.Messages.IntMsg;
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
            ActorRef remote = SystemManager.getInstance().createActor(IntActor.props("Remote"), "remote");
            customConf = ConfigFactory.parseString(confText.replace("2727", "5050"));
            ActorSystem RemoteSystem = ActorSystem.create("LocalSystem", customConf);
            ActorRef local = RemoteSystem.actorOf
                    (IntActor.props("Local"), "local");
            remote.tell(new IntMsg(0), local);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCommunicationBetweenSameSystem() {
        SystemManager.getInstance().createSystem("LocalSystem", null);
        ActorRef firstLocal = SystemManager.getInstance().createActor(IntActor.props("First Local"), "firstLocal");
        ActorRef secondLocal =SystemManager.getInstance().createActor(IntActor.props("Second Local"), "secondLocal");
        firstLocal.tell(new IntMsg(0), secondLocal);
    }

    @Test
    public void testSelectionOnLocalSystem() {
        SystemManager.getInstance().createSystem("System", null);
        SystemManager.getInstance().createActor(IntActor.props("First Local"), "firstLocal");
        SystemManager.getInstance().createActor(IntActor.props("Second Local"), "secondLocal");
        ActorRef first = SystemManager.getInstance().getLocalActor("firstLocal");
        ActorRef second = SystemManager.getInstance().getLocalActor("secondLocal");
        second.tell(new IntMsg(0), first);
    }


}
