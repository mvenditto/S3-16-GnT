package communication;

import akka.actor.*;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.unibo.s3.main_system.communication.Messages;
import com.unibo.s3.main_system.communication.SystemManager;
import com.unibo.s3.main_system.game.AkkaSettings;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class SimulatedRemoteSystemTest {
    private static class TestActor extends AbstractActor {
        ActorRef target = null;
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(ActorRef.class, actorRef -> {
                        target = actorRef;
                        getSender().tell("done", getSelf());
                    })
                    .matchEquals(startMsg, message ->  {
                        getSender().tell(mapMsg, getSelf());
                    })
                    .matchEquals(mapMsg, message -> {
                        if (target != null) target.forward(message, getContext());
                    })
                    .build();
        }
    }

    private static Messages.GenerateMapMsg startMsg = new Messages.GenerateMapMsg();
    private static Messages.MapElementMsg mapMsg = new Messages.MapElementMsg("0.0:0.0:0.0:0.0");

    private static ActorSystem testSystem;

    @BeforeClass
    public static void setup() {
        testSystem = ActorSystem.create();

    }

    @AfterClass
    public static void teardown() {
        SystemManager.shutdownSystem();
        TestKit.shutdownActorSystem(testSystem);
        testSystem = null;
    }

    @Test
    public void simulatedRemoteSystemTest() {
        new TestKit(testSystem) {{
            try {
                String confText = "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
                        "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
                        ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
                        ",\"netty\":{\"tcp\":{\"hostname\":\""+ Inet4Address.getLocalHost().getHostAddress() +"\",\"port\":"+
                        AkkaSettings.GUISystemPort()+"}}}}}";
                Config customConf = ConfigFactory.parseString(confText);
                SystemManager.createSystem("LocalSystem", customConf);
                ActorRef localActor = SystemManager.createActor(Props.create(TestActor.class), "localActor");
                customConf = ConfigFactory.parseString(confText.replace(AkkaSettings.GUISystemPort(),
                        AkkaSettings.ComputeSystemPort()));
                ActorSystem remoteSystem = ActorSystem.create("RemoteSystem", customConf);
                remoteSystem.actorOf(Props.create(TestActor.class), "remoteActor");

                SystemManager.setIPForRemoting(Inet4Address.getLocalHost().getHostAddress());
                ActorSelection remoteActor = SystemManager.getRemoteActor
                        ("RemoteSystem","/user/", "remoteActor");

                TestKit probe = new TestKit(testSystem);

                localActor.tell(probe.getRef(), getRef());

                expectMsg("done");

                within(duration("3 seconds"), () -> {
                    remoteActor.tell(startMsg, localActor);

                    probe.expectMsg(mapMsg);
                    return null;
                });
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }};
    }
}
