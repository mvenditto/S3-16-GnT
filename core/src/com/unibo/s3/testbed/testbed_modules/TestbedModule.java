package com.unibo.s3.testbed.testbed_modules;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unibo.s3.InputProcessorAdapter;
import com.unibo.s3.testbed.Testbed;

public interface TestbedModule extends InputProcessorAdapter {

    void init(Testbed owner);

    void render(ShapeRenderer shapeRenderer);

    void renderGui();

    void update(float dt);

    void cleanup();

    void enable(boolean enabled);

    void resize(int newWidth, int newHeight);

    void attachInputProcessors(InputMultiplexer inputMultiplexer);

    boolean isEnabled();

}
