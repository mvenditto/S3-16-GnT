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

public class CommunicationTest extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture img;

    @Override
    public void create() {
        this.batch = new SpriteBatch();
        this.img = new Texture("badlogic.jpg");

        SystemManager.getInstance().createSystem("System", null);
        SystemManager.getInstance().createActor(MasterActor.props(), "masterActor");
        SystemManager.getInstance().createActor(WorldActor.props(new World(new Vector2(0, 0), true)), "worldActor");
        SystemManager.getInstance().createActor(QuadTreeActor.props(), "quadTreeActor");
        SystemManager.getInstance().createActor(MapActor.props(), "mapActor");
        SystemManager.getInstance().createActor(GraphActor.props(), "graphActor");

        ActorRef masterActor = SystemManager.getInstance().getLocalActor("masterActor");
        ActorRef mapActor = SystemManager.getInstance().getLocalActor("mapActor");
        ActorRef graphActor = SystemManager.getInstance().getLocalActor("graphActor");
        ActorRef quadTreeActor = SystemManager.getInstance().getLocalActor("quadTreeActor");

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

        masterActor.tell(new CreateCharacterMsg(new Vector2(1,1)), ActorRef.noSender());
        masterActor.tell(new CreateCharacterMsg(new Vector2(2,2)), ActorRef.noSender());
        masterActor.tell(new CreateCharacterMsg(new Vector2(3,3)), ActorRef.noSender());
        masterActor.tell(new CreateCharacterMsg(new Vector2(24,34)), ActorRef.noSender());

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
