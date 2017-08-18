package com.unibo.s3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.AbstractMainApplication;
import com.unibo.s3.main_system.modules.BasicModule;
import com.unibo.s3.main_system.modules.CommandModule;

import java.util.ArrayList;
import java.util.List;

import static com.unibo.s3.main_system.rendering.ScaleUtils.*;


public class Main extends AbstractMainApplication {
    private List<BasicModule> modules = new ArrayList<>();
    private InputMultiplexer inputMultiplexer;

    @Override
    public void create() {
        super.create();
        inputMultiplexer = new InputMultiplexer();
        addModules();

        this.modules.forEach(m -> {
            m.init(this);
            m.attachInputProcessors(inputMultiplexer);
        });

        this.inputMultiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(inputMultiplexer);

    }

    private void addModules() {
        CommandModule cm = new CommandModule();
        cm.enable(true);
        modules.add(cm);
        cm.setTextRenderer(textBatch);
    }

    private void renderAxis(ShapeRenderer shapeRenderer) {
        final Color oldColor = shapeRenderer.getColor();
        final Vector2 worldCenter = new Vector2(0f, 0f);

        final float metersRadius = 1f;
        final float pixelRadius = metersToPixels(metersRadius);

        final float axisMetersLength = 50;
        final float axisPixelLenght = metersToPixels(axisMetersLength);

        /*draw world center*/
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(worldCenter.x, worldCenter.y, pixelRadius);

        /*draw x axis*/
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.line(-axisPixelLenght, 0, axisPixelLenght, 0);


        /*draw y axis*/
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.line(0, -axisPixelLenght, 0, axisPixelLenght);
        shapeRenderer.setColor(oldColor);

        /*draw mouse WORLD vs SCREEN position*/
        final Vector2 mouseScreenPos = new Vector2(Gdx.input.getX(), Gdx.input.getY());

        /*scale by 1.0 / pixelPerMeter --> box2D coords*/
        final Vector2 mouseWorldPos = screenToWorld(mouseScreenPos).cpy().scl( getMetersPerPixel());

        //shapeRenderer.circle(metersToPixels(mouseWorldPos.x),
        //        metersToPixels(mouseWorldPos.y), metersToPixels(3));

        textBatch.begin();

            /*Flip y !!!*/
        font.setColor(Color.ORANGE);
        font.draw(textBatch, "screen: " + mouseScreenPos, Gdx.graphics.getWidth() / 2,
                30f);

        font.setColor(Color.YELLOW);
        font.draw(textBatch, "world: "+mouseWorldPos, Gdx.graphics.getWidth() / 2,
                15f);

        textBatch.end();
    }

    private void renderingTest(ShapeRenderer shapeRenderer) {
        final Color oldColor = shapeRenderer.getColor();
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.circle(0, 0, getPixelsPerMeter() * 2);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(-getPixelsPerMeter(), -getPixelsPerMeter(),
                getPixelsPerMeter() * 2, getPixelsPerMeter() * 2);
        shapeRenderer.setColor(oldColor);

    }

    @Override
    protected void doRender() {
        renderAxis(shapeRenderer);
        renderingTest(shapeRenderer);

        modules.stream()
                .filter(BasicModule::isEnabled)
                .forEach(m -> m.render(shapeRenderer));
        modules.stream()
                .filter(BasicModule::isEnabled)
                .forEach(BasicModule::renderGui);
    }

    @Override
    protected void doUpdate(float delta) {
        modules.stream()
                .filter(BasicModule::isEnabled)
                .forEach(m -> m.update(delta));
    }

    @Override
    public void resize(int newWidth, int newHeight) {
        super.resize(newWidth, newHeight);
        modules.forEach(m -> m.resize(newWidth, newHeight));
    }
}
