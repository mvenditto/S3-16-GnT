package com.unibo.s3.main_system.communication;

import akka.actor.ActorRef;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.unibo.s3.main_system.characters.BaseCharacter;
import com.unibo.s3.main_system.world.actors.WorldActor;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Before;
import scala.collection.JavaConversions;
import scala.collection.immutable.List;

import java.util.Arrays;


public class ActorBasedCharacterTest extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture img;

    private UndirectedGraph<Vector2, DefaultEdge> testGraph = new SimpleGraph<>(DefaultEdge.class);
    private Vector2 v1 = new Vector2(3f,3f);
    private Vector2 v2 = new Vector2(7f,3f);
    private Vector2 v3 = new Vector2(11f,3f);
    private Vector2 v4 = new Vector2(11f,7f);
    private Vector2 v5 = new Vector2(7f,7f);
    private Vector2 currentVertex = v1;

    private ActorRef copOne;
    private ActorRef copTwo;
    private ActorRef copThree;

    @Override
    public void create() {

        //graph setup
        testGraph.addVertex(v1);
        testGraph.addVertex(v2);
        testGraph.addVertex(v3);
        testGraph.addVertex(v4);
        testGraph.addVertex(v5);

        testGraph.addEdge(v1, v2);
        testGraph.addEdge(v1, v5);
        testGraph.addEdge(v2, v3);
        testGraph.addEdge(v3, v4);
        testGraph.addEdge(v5, v4);
        testGraph.addEdge(v2, v5);

        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");

        SystemManager.getInstance().createSystem("System", null);
        SystemManager.getInstance().createActor(WorldActor.props(new World(new Vector2(0, 0), true)), "worldActor");
        SystemManager.getInstance().createActor(GraphActor.props(), "graphActor");
        ActorRef mapActor = SystemManager.getInstance().createActor(MapActor.props(), "mapActor");

        SystemManager.getInstance().createActor(QuadTreeActor.props(), "quadTreeActor");

        ActorRef masterActor = SystemManager.getInstance().createActor(MasterActor.props(), "masterActor");

        copOne = SystemManager.getInstance().createActor
                (CharacterActor.props(new BaseCharacter(new Vector2(1,1),1)), "cop1");
        copTwo = SystemManager.getInstance()
                .createActor(CharacterActor.props(new BaseCharacter(new Vector2(7,7),2)), "cop2");
        copThree = SystemManager.getInstance()
                .createActor(CharacterActor.props(new BaseCharacter(new Vector2(4,4),3)), "cop3");

        masterActor.tell(new Messages.ActMsg(0.016f), ActorRef.noSender());

        testInteraction();
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(img, 0, 0);
        batch.end();
    }

    @Override
    public void dispose () {
        batch.dispose();
        img.dispose();
    }

    private void testDestination(){
        copOne.tell(new Messages.SendGraphMsg(testGraph), ActorRef.noSender());
        copTwo.tell(new Messages.SendGraphMsg(testGraph), ActorRef.noSender());
        copThree.tell(new Messages.SendGraphMsg(testGraph), ActorRef.noSender());
    }


    private void testInteraction(){
    /*    copOne.tell(new Messages.SetupGraphMsg(testGraph), ActorRef.noSender());
        copTwo.tell(new Messages.SetupGraphMsg(testGraph), ActorRef.noSender());
        copThree.tell(new Messages.SetupGraphMsg(testGraph), ActorRef.noSender());

        java.util.List<ActorRef> list = Arrays.asList(copTwo, copThree);
        java.util.List<ActorRef> list2 = Arrays.asList(copOne);
        java.util.List<ActorRef> list3 = Arrays.asList(copOne);

        copOne.tell(new Messages.SendNeighboursMsg(list), ActorRef.noSender());
        copTwo.tell(new Messages.SendNeighboursMsg(list2), ActorRef.noSender());
        copThree.tell(new Messages.SendNeighboursMsg(list3), ActorRef.noSender());*/
    }
}
