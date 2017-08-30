package com.unibo.s3.main_system.modules;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.unibo.s3.InputProcessorAdapter;
import com.unibo.s3.Main;

public abstract class BasicModuleWithGui implements BasicModule, InputProcessorAdapter {
    protected Main owner;
    protected Stage gui;
    private boolean enabled = true;

    @Override
    public void init(Main owner) {
        this.owner = owner;
        this.gui = new Stage(new ScreenViewport());
    }

    @Override
    public void renderGui() {
        gui.draw();
    }

    @Override
    public void update(float dt) {
        gui.act(dt);
    }

    @Override
    public void resize(int newWidth, int newHeight) {
        gui.getViewport().update(newWidth, newHeight, true);
    }

    @Override
    public void attachInputProcessors(InputMultiplexer inputMultiplexer) {
        inputMultiplexer.addProcessor(this.gui);
    }

    @Override
    public void enable(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {

    }

    @Override
    public void cleanup() {
        gui.dispose();
    }
}
