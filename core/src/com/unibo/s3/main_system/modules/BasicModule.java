package com.unibo.s3.main_system.modules;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unibo.s3.InputProcessorAdapter;
import com.unibo.s3.Main;

public interface BasicModule extends InputProcessorAdapter {
    void init(Main owner);

    void attachInputProcessors(InputMultiplexer inputMultiplexer);

    void enable(boolean enabled);

    boolean isEnabled();

    void render(ShapeRenderer shapeRenderer);

    void customRender();

    void renderGui();

    void update(float dt);

    void resize(int newWidth, int newHeight);

    void cleanup();
}
