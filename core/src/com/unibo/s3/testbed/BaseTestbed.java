package com.unibo.s3.testbed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.AbstractMainApplication;
import com.unibo.s3.main_system.world.actors.Box2dRayCastCollisionDetectorProxy;
import com.unibo.s3.testbed.testbed_modules.Box2dActorBasedModule;
import com.unibo.s3.testbed.testbed_modules.EntitiesSystemModule;
import com.unibo.s3.testbed.testbed_modules.TestbedModule;

import java.util.ArrayList;
import java.util.List;

public class BaseTestbed extends AbstractMainApplication implements Testbed {

    private List<TestbedModule> modules = new ArrayList<>();
    private InputMultiplexer inputMultiplexer;

    /*core modules*/
    private EntitiesSystemModule msm;
    private Box2dActorBasedModule b2dm;

    private static final String PAUSED = "paused";
    private static final String FPS = "FPS";
    private static final String B2D_BODY_EDITOR_ENABLED = "body editor enabled";
    private static final String SEPARATOR = ": ";

    @Override
    public void create() {
        super.create();
        inputMultiplexer = new InputMultiplexer();
        preInitSetup();
        modules.forEach(m -> m.init(this));
        modules.forEach(m -> m.attachInputProcessors(inputMultiplexer));
        postInitSetup();
        inputMultiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    protected void doRender() {
        modules.stream()
                .filter(TestbedModule::isEnabled)
                .forEach(m -> m.render(shapeRenderer));
        modules.stream()
                .filter(TestbedModule::isEnabled)
                .forEach(TestbedModule::renderGui);
        renderInfo();
    }

    @Override
    protected void doUpdate(float delta) {
        modules.stream()
                .filter(TestbedModule::isEnabled)
                .forEach(m -> m.update(delta));
    }

    @Override
    public void resize(int newWidth, int newHeight) {
        super.resize(newWidth, newHeight);
        modules.forEach(m->m.resize(newWidth, newHeight));
    }

    private void postInitSetup() {
        RaycastCollisionDetector<Vector2> collisionDetector = new Box2dRayCastCollisionDetectorProxy(b2dm.worldActor);
        msm.setCollisionDetector(collisionDetector);
    }

    private void preInitSetup() {
        msm = new EntitiesSystemModule();
        msm.enable(false);
        modules.add(msm);

        b2dm = new Box2dActorBasedModule();
        b2dm.enable(true);
        modules.add(b2dm);
        b2dm.setTextRenderer(textBatch);
    }

    private void renderInfo() {
        textBatch.begin();
        font.draw(textBatch, PAUSED + SEPARATOR + pause, 10, 30);
        font.draw(textBatch, B2D_BODY_EDITOR_ENABLED + SEPARATOR + b2dm.isBodyEditorEnabled(), 10, Gdx.graphics.getHeight() - 35);
        font.draw(textBatch, FPS + SEPARATOR + Gdx.graphics.getFramesPerSecond(), 10, 15);
        textBatch.end();
    }
}
