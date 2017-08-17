package com.unibo.s3.testbed.testbed_modules;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.unibo.s3.InputProcessorAdapter;
import com.unibo.s3.testbed.Testbed;

public class BasicTestbedModuleWithGui implements TestbedModule, InputProcessorAdapter {

    protected Testbed owner;
    protected Stage gui;
    protected boolean guiEnabled = true;
    protected boolean enabled = true;

    @Override
    public void init(Testbed owner) {
        this.owner = owner;
        this.gui = new Stage(new ScreenViewport());
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {

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
    public void cleanup() {
        gui.dispose();
    }

    @Override
    public void enable(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void resize(int newWidth, int newHeight) {
        gui.getViewport().update(newWidth, newHeight, true);
    }

    @Override
    public void attachInputProcessors(InputMultiplexer inputMultiplexer) {
        inputMultiplexer.addProcessor(gui);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    protected void enableGui(boolean enableGui) {
        guiEnabled = enableGui;
        gui.getActors().forEach(a->a.setVisible(enableGui));
    }
}
