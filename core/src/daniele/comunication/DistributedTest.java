package daniele.comunication;

import akka.actor.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Before;
import org.junit.Test;

import java.net.Inet4Address;

public class DistributedTest {

    private UndirectedGraph<String, DefaultEdge> graph;

    @Before
    public void initialization() {
        this.graph = new SimpleGraph<>(DefaultEdge.class);

        String v1 = "v1";
        String v2 = "v2";
        String v3 = "v3";
        String v4 = "v4";

        this.graph.addVertex(v1);
        this.graph.addVertex(v2);
        this.graph.addVertex(v3);
        this.graph.addVertex(v4);

        this.graph.addEdge(v1, v2);
        this.graph.addEdge(v2, v3);
        this.graph.addEdge(v3, v4);
        this.graph.addEdge(v4, v1);

        System.out.println("initial graph: " + this.graph.toString());
    }

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
            ActorRef remote = SystemManager.getInstance().createActor(GraphActor.props("Remote"), "remote");
            customConf = ConfigFactory.parseString(confText.replace("2727", "5050"));
            ActorSystem LocalSystem = ActorSystem.create("LocalSystem", customConf);
            ActorRef local = LocalSystem.actorOf
                    (GraphActor.props("Local"), "local");
            remote.tell(this.graph, local);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCommunicationAndSelectionOnLocalSystem() {
        SystemManager.getInstance().createSystem("System", null);
        SystemManager.getInstance().createActor(GraphActor.props("First Local"), "firstLocal");
        SystemManager.getInstance().createActor(GraphActor.props("Second Local"), "secondLocal");
        ActorRef first = SystemManager.getInstance().getLocalActor("firstLocal");
        ActorRef second = SystemManager.getInstance().getLocalActor("secondLocal");
        second.tell(this.graph, first);
    }
}
