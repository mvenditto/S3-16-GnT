package com.unibo.s3.main_system.modules;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.unibo.s3.InputProcessorAdapter;
import com.unibo.s3.Main;

public class BasicModuleWithGui implements BasicModule, InputProcessorAdapter {
    protected Main owner;
    protected Stage initialGUI, menuGUI;
    private boolean guiEnabled = true;
    private boolean enabled = true;

    @Override
    public void init(Main owner) {
        this.owner = owner;
        this.initialGUI = new Stage(new ScreenViewport());
        this.menuGUI = new Stage(new ScreenViewport());
    }

    @Override
    public void renderGui() {
        initialGUI.draw();
    }

    @Override
    public void update(float dt) {
        initialGUI.act(dt);
    }

    @Override
    public void resize(int newWidth, int newHeight) {
        initialGUI.getViewport().update(newWidth, newHeight, true);
    }

    @Override
    public void attachInputProcessors(InputMultiplexer inputMultiplexer) {
        inputMultiplexer.addProcessor(this.initialGUI);
    }

    @Override
    public void enable(boolean enabled) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {

    }
}
