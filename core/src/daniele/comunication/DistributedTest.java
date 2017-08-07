package daniele.comunication;

import akka.actor.*;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import daniele.comunication.Messages.IntMsg;
import org.junit.Test;
import scala.concurrent.Future;

import java.net.Inet4Address;
import java.util.concurrent.TimeUnit;

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
                    (IntActor.props("Remote"), "remote");
            customConf = ConfigFactory.parseString(confText.replace("2727", "5050"));
            SystemManager.getInstance().createSystem("LocalSystem", customConf);
            ActorRef local = SystemManager.getInstance().getSystem("LocalSystem").actorOf
                    (IntActor.props("Local"), "local");
            remote.tell(new IntMsg(0), local);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCommunicationBetweenSameSystem() {
        SystemManager.getInstance().createSystem("LocalSystem", null);
        ActorRef firstLocal = SystemManager.getInstance().getSystem("LocalSystem").actorOf
                (IntActor.props("First Local"), "firstLocal");
        ActorRef secondLocal = SystemManager.getInstance().getSystem("LocalSystem").actorOf
                (IntActor.props("Second Local"), "secondLocal");
        firstLocal.tell(new IntMsg(0), secondLocal);
    }

    @Test
    public void testSelectionOnLocalSystem() {
        SystemManager.getInstance().createSystem("System", null);
        SystemManager.getInstance().getSystem("System").actorOf
                (IntActor.props("First Local"), "firstLocal");
        SystemManager.getInstance().getSystem("System").actorOf
                (IntActor.props("Second Local"), "secondLocal");
        ActorSelection second = SystemManager.getInstance().getSystem("System").actorSelection("akka://System/user/secondLocal");
        Future<ActorRef> future = SystemManager.getInstance().getSystem("System")
                .actorSelection("/user/firstLocal").resolveOne(new Timeout(5, TimeUnit.SECONDS).duration());
        second.tell(new IntMsg(0), future.value().get().get());
    }


}
