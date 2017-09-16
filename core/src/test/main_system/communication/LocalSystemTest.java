package main_system.communication;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.unibo.s3.main_system.communication.Messages.GenerateMapMsg;
import com.unibo.s3.main_system.communication.Messages.MapElementMsg;
import com.unibo.s3.main_system.communication.SystemManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.Option;

public class LocalSystemTest {

    private static class TestActor extends AbstractActor {
        ActorRef target = null;
        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(ActorRef.class, actorRef -> {
                        target = actorRef;
                        getSender().tell("done", getSelf());
                    })
                    .matchEquals(startMsg, message -> getSender().tell(mapMsg, getSelf()))
                    .matchEquals(mapMsg, message -> {
                        if (target != null) target.forward(message, getContext());
                    })
                    .build();
        }
    }

    private static GenerateMapMsg startMsg = new GenerateMapMsg();
    private static MapElementMsg mapMsg = new MapElementMsg("0.0:0.0:0.0:0.0");

    private static ActorSystem testSystem;

    @BeforeClass
    public static void setup() {
        SystemManager.createSystem("MySystem", Option.empty());
        SystemManager.createActor(Props.create(TestActor.class), "firstActor");
        SystemManager.createActor(Props.create(TestActor.class), "secondActor");
        testSystem = ActorSystem.create();

    }

    @AfterClass
    public static void teardown() {
        SystemManager.shutdownSystem();
        TestKit.shutdownActorSystem(testSystem);
        testSystem = null;
    }

    @Test
    public void localSystemTest() {
        new TestKit(testSystem) {{

            ActorRef firstActor = SystemManager.getLocalActor("firstActor");
            ActorRef secondActor = SystemManager.getLocalActor("secondActor");

            TestKit probe = new TestKit(testSystem);

            firstActor.tell(probe.getRef(), getRef());

            expectMsg("done");

            within(duration("3 seconds"), () -> {
                secondActor.tell(startMsg, firstActor);

                probe.expectMsg(mapMsg);
                return null;
            });
        }};
    }
}
