package com.unibo.s3.testbed.testbed_modules;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Array;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.unibo.s3.main_system.communication.Messages.ActMsg;
import com.unibo.s3.main_system.communication.SystemManager;
import com.unibo.s3.main_system.world.actors.*;
import com.unibo.s3.testbed.Testbed;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.unibo.s3.main_system.util.ScaleUtils.*;

@SuppressWarnings("Duplicates")
public class Box2dActorBasedModule extends BasicTestbedModuleWithGui {

    private World world;
    public ActorRef worldActor;
    private boolean enabled = true;
    private boolean bodyEditorEnabled = false;
    private Vector2 topLeft = null;
    private Vector2 delta = null;
    private boolean mouseDragged = false;
    private boolean enableGrid = false;
    private SpriteBatch textBatch;
    private BitmapFont font;

    private void initGui() {
        Table table = new Table();

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        Button btnSaveWorld = new TextButton("Save World", skin);
        Button btnLoadWorld = new TextButton("Load World", skin);
        Button btnResetWorld = new TextButton("Reset World", skin);

        Label l = new Label("Box2D World Module",skin);
        l.setColor(Color.GREEN);
        table.add(l);

        table.row();
        table.add(btnSaveWorld);
        table.row();
        table.add(btnLoadWorld);
        table.row();
        table.add(btnResetWorld);

        gui.addActor(table);

        table.setFillParent(true);

        table.addListener((new DragListener() {
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                table.moveBy(x - table.getWidth() / 2, y - table.getHeight() / 2);
            }
        }));

        btnResetWorld.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                resetWorld();
            }
        });

        btnSaveWorld.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input (String text) {
                        System.out.println(text);
                        saveWorld(text);
                    }

                    @Override
                    public void canceled () {}
                }, "Save World...", System.getProperty("user.home"), "");
            }
        });

        btnLoadWorld.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.input.getTextInput(new Input.TextInputListener() {
                    @Override
                    public void input (String text) {
                        loadWorld(text);
                    }

                    @Override
                    public void canceled () {}
                }, "Load World...", System.getProperty("user.home"), "");
            }
        });

        Button b2 = new TextButton("Toggle grid render.", skin);
        b2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                enableGrid = !enableGrid;
            }
        });
        table.row();
        table.add(b2);

    }

    public ActorRef getWorldActorRef() {return worldActor;}

    public World getWorld() {return world;}

    private void resetWorld() {
        worldActor.tell(new ResetWorld(), null);
    }

    private void loadWorld(String fileName) {
        try {
            Files.lines(Paths.get(fileName + ".txt")).forEach(l -> {
                String[] toks = l.split(":");
                float x = Float.parseFloat(toks[0]);
                float y = Float.parseFloat(toks[1]);
                float w = Float.parseFloat(toks[2]);
                float h = Float.parseFloat(toks[3]);
                worldActor.tell(new CreateBox(new Vector2(x, y), new Vector2(w, h)), null);
            });
        } catch (IOException e) {}
    }

    private void saveWorld(String fileName) {
        List<String> lines = new ArrayList<>();
        Path file = Paths.get(fileName + ".txt");

        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        for (Body b: bodies) {
            lines.add(b.getWorldCenter().x+":"+b.getWorldCenter().y+":"+b.getUserData());
        }

        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {System.out.println(e.getMessage());}
    }

    @Override
    public void init(Testbed owner) {
        super.init(owner);
        world = new World(new Vector2(0, 0), true);
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(1.5f);
        initGui();

        String confText = "{\"akka\":{\"actor\":{\"provider\":\"akka.remote.RemoteActorRefProvider\"}," +
                        "\"loglevel\":\"INFO\",\"remote\":{\"enabled-transports\":[\"akka.remote.netty.tcp\"]" +
                        ",\"log-received-messages\":\"on\",\"log-sent-messages\":\"on\"" +
                        ",\"netty\":{\"tcp\":{\"hostname\":\""+ "127.0.0.1" +"\",\"port\":5050}}}}}";

        Config customConf = ConfigFactory.parseString(confText);
        SystemManager.getInstance().createSystem("b2d", customConf);
        SystemManager.getInstance().createActor(Props.create(WorldActor.class, world), "world");
        worldActor = SystemManager.getInstance().getLocalActor("world");
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        if (enableGrid) {
            renderGrid(shapeRenderer, 200, 200);
        }

        Timeout timeout = new Timeout(Duration.create(5, "seconds"));
        Future<Object> future = Patterns.ask(worldActor, new GetAllBodies(), timeout);
        try {
            Array<Body> bodies = (Array<Body>) Await.result(future, timeout.duration());
            bodies.forEach(b->renderBox(shapeRenderer, b, false));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bodyEditorEnabled && topLeft != null && delta != null){
            shapeRenderer.setColor(Color.GREEN);
            //shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Vector2 tmp = topLeft.cpy().scl(getPixelsPerMeter());
            Vector2 tmp2 = delta.cpy().scl(getPixelsPerMeter());
            shapeRenderer.rect(tmp.x, tmp.y, tmp2.x-tmp.x, tmp2.y-tmp.y);

            float cx = tmp.x + ((tmp2.x-tmp.x) / 2);
            float cy = tmp.y + ((tmp2.y-tmp.y) / 2);
            shapeRenderer.circle(cx, cy, 10);

            //shapeRenderer.circle((cx + (tmp2.x-tmp.x)/2) + 30, cy, 10);
            //shapeRenderer.circle(cx, (cy + (tmp2.y-tmp.y)/2) - 30, 10);

            //Vector2 wTextPos = new Vector2(cx, (cy + (tmp2.y - tmp.y) / 2) - 30);
            //Vector2 hTextPos = new Vector2((cx + (tmp2.x-tmp.x)/2) + 30, cy);

            Vector2 wTextPos = owner.worldToScreen(new Vector2(cx, (cy + (tmp2.y - tmp.y) / 2) - 30));
            Vector2 hTextPos = owner.worldToScreen(new Vector2((cx + (tmp2.x-tmp.x)/2) + 30, cy));

            //System.out.println(wTextPos + " -> " + owner.worldToScreen(wTextPos));

            if (textBatch != null) {
                textBatch.begin();
                //font.draw(textBatch, ""+Math.round(owner.pixelsToMeters((int)(tmp2.x - tmp.x))), cx, (cy + (tmp2.y - tmp.y) / 2) - 30);
                //font.draw(textBatch, ""+Math.abs(Math.round(owner.pixelsToMeters((int)(tmp2.y - tmp.y)))), (cx + (tmp2.x-tmp.x)/2) + 30, cy);

                font.draw(textBatch, ""+Math.round(pixelsToMeters((int)(tmp2.x - tmp.x))), wTextPos.x, wTextPos.y);
                font.draw(textBatch, ""+Math.abs(Math.round(pixelsToMeters((int)(tmp2.y - tmp.y)))), hTextPos.x, hTextPos.y);

                textBatch.end();
            }
        }
        //shapeRenderer.end();
        //gui.draw();
    }

    @Override
    public void update(float dt) {
        worldActor.tell(new ActMsg(dt), null);
    }

    @Override
    public void cleanup() {
        world.dispose();
    }

    @Override
    public void enable(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void attachInputProcessors(InputMultiplexer inputMultiplexer) {
        super.attachInputProcessors(inputMultiplexer);
        inputMultiplexer.addProcessor(this);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.G) {
            bodyEditorEnabled = !bodyEditorEnabled;
        }
        if (keycode == Input.Keys.U) {
            enableGui(!super.guiEnabled);
        }
        return false;
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector2 click = new Vector2(screenX, screenY);
        click = owner.screenToWorld(click).scl(getMetersPerPixel());
        topLeft = click;
        return false;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (bodyEditorEnabled) {
            if (mouseDragged) {
                Vector2 center = topLeft.cpy().add(delta).scl(0.5f);
                worldActor.tell(new CreateBox(center, new Vector2(Math.abs(topLeft.x-delta.x),Math.abs(topLeft.y-delta.y))), null);
            }
            if (button == 1) {
                Vector2 click = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                click = owner.screenToWorld(click).scl(getMetersPerPixel());
                worldActor.tell(new DeleteBodyAt(click.x, click.y),null);
            }
        }
        topLeft = null;
        delta = null;
        mouseDragged = false;
        return false;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        Vector2 click = new Vector2(screenX, screenY);
        click = owner.screenToWorld(click);

        if (topLeft != null) {
            delta = click.cpy().scl(getMetersPerPixel());
        }

        mouseDragged = true;
        return false;
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
        //renderer.polygon(vertices);
    }

    private void renderGrid(ShapeRenderer renderer, int w, int h) {
        float s = getPixelsPerMeter();
        int wh = w / 2;
        int hh = h / 2;

        Color c= renderer.getColor();
        Color gc = Color.LIGHT_GRAY;
        renderer.setColor(gc.r, gc.g, gc.b, 0.10f);

        for (int y = -hh; y <= hh; y += 5) {
            renderer.line(-wh * s, y * s, wh * s, y * s);
        }

        for (int x = -wh; x <= wh; x += 5) {
            renderer.line(x * s, -hh * s, x * s, hh * s);
        }
        renderer.setColor(c);
    }

    public boolean isBodyEditorEnabled() {return bodyEditorEnabled;}

    public void setTextRenderer(SpriteBatch b) {
        textBatch = b;
    }

}
