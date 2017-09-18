package graph;

import akka.actor.ActorRef;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.unibo.s3.main_system.communication.GeneralActors;
import com.unibo.s3.main_system.communication.Messages;
import com.unibo.s3.main_system.communication.SystemManager;
import com.unibo.s3.main_system.graph.GraphGenerator;
import com.unibo.s3.main_system.world.actors.WorldActor;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;
import org.junit.runner.RunWith;
import main_system.GdxDependencies;
import scala.Option;

import static org.junit.Assert.assertEquals;

@RunWith(GdxDependencies.class)
public class GraphTest {
    private String mapFilename = "maps/test.txt";

    @Test
    public void checkNewGraph() {
        SystemManager.createSystem("TestSystem",Option.empty(), Option.empty());
        SystemManager.createActor(WorldActor.props(new World(new Vector2(0, 0), true)), "worldActor");

        sendMapToWord();

        UndirectedGraph<Vector2, DefaultEdge> objectGraph = GraphGenerator.createGraph(15,9,mapFilename);
        UndirectedGraph<Vector2, DefaultEdge> expected = createExpectedGraph();
        assert(objectGraph.vertexSet().containsAll(expected.vertexSet()));
        assert(expected.vertexSet().containsAll(objectGraph.vertexSet()));
        NeighborIndex<Vector2, DefaultEdge> neighborGraph = new NeighborIndex<>(objectGraph);
        NeighborIndex<Vector2, DefaultEdge> neighborExpected = new NeighborIndex<>(expected);
        objectGraph.vertexSet().forEach(v -> {
            assert(neighborGraph.neighborsOf(v).equals(neighborExpected.neighborsOf(v)));
        });
    }

    private void sendMapToWord() {
        FileHandle file = Gdx.files.local(mapFilename);
        String text = file.readString();
        String[] lines = text.split("\\n");
        for(String l : lines) {
            SystemManager.getLocalActor(GeneralActors.WORLD_ACTOR()).tell(new Messages.MapElementMsg(l), ActorRef.noSender());
        }
    }

    private UndirectedGraph<Vector2, DefaultEdge> createExpectedGraph() {
        UndirectedGraph<Vector2, DefaultEdge> expectedGraph = new SimpleGraph<>(DefaultEdge.class);
        Vector2 v1 = new Vector2(3f,3f);
        expectedGraph.addVertex(v1);
        Vector2 v2 = new Vector2(6f,3f);
        expectedGraph.addVertex(v2);
        Vector2 v3 = new Vector2(9f,5f);
        expectedGraph.addVertex(v3);
        Vector2 v4 = new Vector2(13f,5f);
        expectedGraph.addVertex(v4);
        Vector2 v5 = new Vector2(12f,9f);
        expectedGraph.addVertex(v5);
        Vector2 v6 = new Vector2(9f,9f);
        expectedGraph.addVertex(v6);

        expectedGraph.addEdge(v1,v2);
        expectedGraph.addEdge(v2,v3);
        expectedGraph.addEdge(v3,v4);
        expectedGraph.addEdge(v4,v5);
        expectedGraph.addEdge(v6,v5);
        return expectedGraph;
    }
}
