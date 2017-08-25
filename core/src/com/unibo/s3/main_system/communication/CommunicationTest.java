package com.unibo.s3.main_system.communication;

import akka.actor.ActorRef;
import akka.actor.Props;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.unibo.s3.main_system.graph.GraphImpl;
import com.unibo.s3.main_system.world.actors.WorldActor;

public class CommunicationTest extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture img;

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");

        SystemManager.getInstance().createSystem("System", null);
        SystemManager.getInstance().createActor(WorldActor.props(new World(new Vector2(0, 0), true)), "worldActor");
        SystemManager.getInstance().createActor(GraphActor.props(), "graphActor");
        SystemManager.getInstance().createActor(MapActor.props(), "mapActor").tell(new Messages.StartMsg(), ActorRef.noSender());

        ActorRef quadTree = SystemManager.getInstance().createActor(QuadTreeActor.props(), "quadTree");

        ActorRef copOne = SystemManager.getInstance().createActor(CharacterActor.props(), "copOne");
        ActorRef copTwo = SystemManager.getInstance().createActor(CharacterActor.props(), "copTwo");
        ActorRef copThree = SystemManager.getInstance().createActor(CharacterActor.props(), "copThree");

        quadTree.tell(new Messages.askNeighbourMsg(), copOne);
        quadTree.tell(new Messages.askNeighbourMsg(), copTwo);
        quadTree.tell(new Messages.askNeighbourMsg(), copThree);


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
}
