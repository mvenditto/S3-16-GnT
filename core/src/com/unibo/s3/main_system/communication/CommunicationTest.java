package com.unibo.s3.main_system.communication;

import akka.actor.ActorRef;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.unibo.s3.main_system.world.actors.WorldActor;

import com.unibo.s3.main_system.communication.Messages.CreateCharacterMsg;
import com.unibo.s3.main_system.communication.Messages.ActMsg;
import com.unibo.s3.main_system.communication.Messages.GenerateMapMsg;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import scala.Option;
import scala.tools.cmd.Opt;

public class CommunicationTest extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture img;

    @Override
    public void create() {
        UndirectedGraph<Vector2, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        Vector2 v1 = new Vector2(3f, 3f);
        Vector2 v2 = new Vector2(7f, 3f);
        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addEdge(v1, v2);
        this.batch = new SpriteBatch();
        this.img = new Texture("badlogic.jpg");

        SystemManager.createSystem("System", Option.empty(), Option.empty());
        SystemManager.createActor(MasterActor.props(), GeneralActors.MASTER_ACTOR());
        SystemManager.createActor(WorldActor.props(new World(new Vector2(0, 0), true)), GeneralActors.WORLD_ACTOR());
        SystemManager.createActor(QuadTreeActor.props(), GeneralActors.QUAD_TREE_ACTOR());
        SystemManager.createActor(MapActor.props(), GeneralActors.MAP_ACTOR());
        SystemManager.createActor(GraphActor.props(), GeneralActors.GRAPH_ACTOR());

        ActorRef masterActor = SystemManager.getLocalActor(GeneralActors.MASTER_ACTOR());
        ActorRef mapActor = SystemManager.getLocalActor(GeneralActors.MAP_ACTOR());
        ActorRef graphActor = SystemManager.getLocalActor(GeneralActors.GRAPH_ACTOR());
        ActorRef quadTreeActor = SystemManager.getLocalActor(GeneralActors.QUAD_TREE_ACTOR());

        mapActor.tell(new Messages.MapSettingsMsg(60, 60), ActorRef.noSender());
        graphActor.tell(new Messages.MapSettingsMsg(60, 60), ActorRef.noSender());
        quadTreeActor.tell(new Messages.MapSettingsMsg(60, 60), ActorRef.noSender());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mapActor.tell(new GenerateMapMsg(), ActorRef.noSender());

        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        masterActor.tell(new CreateCharacterMsg(new Vector2(3,3), CharacterActors.GUARD()), ActorRef.noSender());
        masterActor.tell(new CreateCharacterMsg(new Vector2(2,2), CharacterActors.GUARD()), ActorRef.noSender());
        masterActor.tell(new CreateCharacterMsg(new Vector2(6.5f,3), CharacterActors.GUARD()), ActorRef.noSender());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ActorRef cop1 = SystemManager.getLocalActor(CharacterActors.GUARD(), 1);
        ActorRef cop2 = SystemManager.getLocalActor(CharacterActors.GUARD(), 2);
        ActorRef cop3 = SystemManager.getLocalActor(CharacterActors.GUARD(), 3);

        cop1.tell(new Messages.SendGraphMsg(graph), ActorRef.noSender());
        cop2.tell(new Messages.SendGraphMsg(graph), ActorRef.noSender());
        cop3.tell(new Messages.SendGraphMsg(graph), ActorRef.noSender());

        masterActor.tell(new ActMsg(0.016f), ActorRef.noSender());
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.batch.begin();
        this.batch.draw(img, 0, 0);
        this.batch.end();
    }

    @Override
    public void dispose () {
        this.batch.dispose();
        this.img.dispose();
    }
}
