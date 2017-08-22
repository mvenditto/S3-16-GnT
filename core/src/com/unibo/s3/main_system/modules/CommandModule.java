package com.unibo.s3.main_system.modules;

import akka.actor.ActorRef;
import akka.util.Timeout;
import akka.pattern.Patterns;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.unibo.s3.Main;
import com.unibo.s3.main_system.rendering.ScaleUtils;
import com.unibo.s3.main_system.world.actors.GetAllBodies;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.xml.soap.Text;

import static com.unibo.s3.main_system.rendering.ScaleUtils.getPixelsPerMeter;
import static com.unibo.s3.main_system.rendering.ScaleUtils.metersToPixels;
import static com.unibo.s3.main_system.rendering.ScaleUtils.pixelsToMeters;

public class CommandModule extends BasicModuleWithGui {
    private SpriteBatch textBatch;
    private World world;
    private BitmapFont font;
    private boolean enabled = true;
    private boolean enableGrid = false;
    private boolean bodyEditorEnabled = false;
    private Vector2 topLeft = null;
    private Vector2 delta = null;

    private int guardsNum = 5;
    private int thiefsNum = 1;
    private boolean pause = false;
    private boolean simulation = false;

    @Override
    public void init(Main owner) {
        super.init(owner);
        this.world = new World(new Vector2(0,0), true);
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(1.5f);
        initGUI();

        //ATTORI
    }

    private void initGUI() {
        Table table = new Table();

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        Label l = new Label("Menu",skin);
        l.setColor(Color.GREEN);
        table.add(l);

        table.row();
        Slider guardsNumS = new Slider(2f, 20f, 1f, false, skin);
        guardsNumS.setValue(this.guardsNum);
        Label labGuardsNum = new Label(" " + String.format("%02d", (int) guardsNumS.getValue()), skin);
        table.add(new Label("Guards number: ", skin));
        table.row();
        table.add(guardsNumS);
        table.add(labGuardsNum);
        guardsNumS.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                guardsNum = (int) guardsNumS.getValue();
                labGuardsNum.setText(" "+String.format("%02d", guardsNum));
            }
        });

        table.row();
        Slider thiefsNumS = new Slider(1f, 20f, 1f, false, skin);
        thiefsNumS.setValue(this.thiefsNum);
        Label labThiefsNum = new Label(" " + String.format("%02d", (int) thiefsNumS.getValue()), skin);
        table.add(new Label("Thiefs number: ", skin));
        table.row();
        table.add(thiefsNumS);
        table.add(labThiefsNum);
        thiefsNumS.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                thiefsNum = (int) thiefsNumS.getValue();
                labThiefsNum.setText(" "+String.format("%02d", thiefsNum));
            }
        });

        table.row();
        String pauseString = "Pause";
        String startString = "Start";
        TextButton buttonPause = new TextButton(pauseString, skin);
        table.add(buttonPause);
        buttonPause.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(pause)
                    buttonPause.setText(pauseString);
                else
                    buttonPause.setText(startString);
                pause = !pause;
            }
        });

        table.row();
        Button buttonStop = new TextButton("STOP", skin);
        table.add(buttonStop);

        table.row();
        String sim = "Simulated";
        String pil = "Piloted";
        TextButton buttonSimulation = new TextButton(sim, skin);
        table.add(buttonSimulation);
        buttonSimulation.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(simulation)
                    buttonSimulation.setText(sim);
                else
                    buttonSimulation.setText(pil);
                simulation = !simulation;
            }
        });

        table.row();
        CheckBox debugView = new CheckBox(" View debug", skin);
        table.add(debugView);

        gui.addActor(table);

        table.setFillParent(false);
        table.setPosition(100, Gdx.graphics.getHeight() - 120);

        //TODO add right buttons and widget
    }

    @Override
    public void attachInputProcessors(InputMultiplexer inputMultiplexer) {
        super.attachInputProcessors(inputMultiplexer);
        inputMultiplexer.addProcessor(this);
    }

    @Override
    public void enable(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        if(enableGrid)
            renderGrid(shapeRenderer, 200, 200);

        /*Timeout timeout = new Timeout(Duration.create(5, "seconds"));
        Future<Object> future = Patterns.ask(worldActor, new GetAllBodies(), timeout);

        Array<Body> bodies = null;
        try {
            bodies = (Array<Body>) Await.result(future, timeout.duration());
            bodies.forEach(b->renderBox(shapeRenderer, b, false));
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        if(bodyEditorEnabled && topLeft != null && delta != null) {
            shapeRenderer.setColor(Color.GREEN);
            Vector2 tmp = topLeft.cpy().scl(getPixelsPerMeter());
            Vector2 tmp2 = delta.cpy().scl(getPixelsPerMeter());
            shapeRenderer.rect(tmp.x,tmp.y,tmp2.x-tmp.x, tmp2.y-tmp.y);

            float cx = tmp.x + ((tmp2.x-tmp.x) / 2);
            float cy = tmp.y + ((tmp2.y - tmp.y) / 2);
            shapeRenderer.circle(cx, cy, 10);

            Vector2 wTextPos = owner.worldToScreen(new Vector2(cx, (cy + (tmp2.y - tmp.y) / 2) - 30));
            Vector2 hTextPos = owner.worldToScreen(new Vector2((cx + (tmp2.x-tmp.x)/2) + 30, cy));

            if (textBatch != null) {
                textBatch.begin();

                font.draw(textBatch, ""+Math.round(pixelsToMeters((int)(tmp2.x - tmp.x))), wTextPos.x, wTextPos.y);
                font.draw(textBatch, ""+Math.abs(Math.round(pixelsToMeters((int)(tmp2.y - tmp.y)))), hTextPos.x, hTextPos.y);

                textBatch.end();
            }
        }

    }

    private void renderBox(ShapeRenderer renderer, Body b, boolean underMouse) {
        Vector2 position = b.getWorldCenter();
        float angle = b.getAngle();
        PolygonShape poly = (PolygonShape) b.getFixtureList().get(0).getShape();

        float[] vertices = new float[poly.getVertexCount() * 2];

        int j = 0;
        for (int i = 0; i < poly.getVertexCount(); i++) {
            Vector2 v = new Vector2();
            poly.getVertex(i, v);
            Vector2 worldVertex = b.getWorldPoint(v);
            vertices[j] = metersToPixels(worldVertex.x);
            vertices[j + 1] = metersToPixels(worldVertex.y);
            j += 2;
        }

        Color c = renderer.getColor();

        if (underMouse) {
            renderer.setColor(Color.CYAN);
        } else {
            renderer.setColor(Color.GRAY);
        }

        Vector2 v0 = new Vector2(vertices[0],vertices[1]);
        String[] t = ((String) b.getUserData()).split(":");
        float s = getPixelsPerMeter();

        renderer.setAutoShapeType(true);
        renderer.set(ShapeRenderer.ShapeType.Filled);
        renderer.rect(v0.x, v0.y, Float.parseFloat(t[0]) * s, Float.parseFloat(t[1]) * s);
        renderer.set(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Color.BLACK);
        renderer.rect(v0.x, v0.y, Float.parseFloat(t[0]) * s, Float.parseFloat(t[1]) * s);
        renderer.setAutoShapeType(false);
        renderer.setColor(c);
    }

    private void renderGrid(ShapeRenderer renderer, int width, int height) {
        float s = ScaleUtils.getPixelsPerMeter();
        int halfWidth = width / 2;
        int halhHeight = height / 2;

        Color oldColor = renderer.getColor();
        Color greyColor = Color.LIGHT_GRAY;
        renderer.setColor(greyColor.r, greyColor.g, greyColor.b, 0.10f);

        for(int y = -halhHeight; y <= halhHeight; y += 5)
            renderer.line((- halfWidth*s), (-halhHeight * s), (halfWidth * s), (y * s));

        for(int x = -halfWidth; x <= halfWidth; x += 5)
            renderer.line((x*s), (-halhHeight * s), (x * s), (halhHeight * s));

        renderer.setColor(oldColor);
    }


    @Override
    public void update(float dt) {
        //Fa qualcosa con l'attore
    }


    public void setTextRenderer(SpriteBatch b) {
        textBatch = b;
    }
}
