package com.unibo.s3.testbed.testbed_modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Array;
import com.unibo.s3.testbed.Testbed;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.unibo.s3.main_system.rendering.ScaleUtils.*;

@SuppressWarnings("Duplicates")
public class Box2dModule extends BasicTestbedModuleWithGui {

    private World world;
    private boolean enabled = true;
    private boolean bodyEditorEnabled = false;
    private Vector2 topLeft = null;
    private Vector2 delta = null;
    private boolean mouseDragged = false;
    private boolean enableGrid = false;
    private List<Body> underMouseBodies = new ArrayList<>();
    private List<Body> bodiesToDelete = new ArrayList<>();
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

    private void resetWorld() {
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        bodiesToDelete.addAll(Arrays.asList(bodies.toArray()));
    }

    public void loadWorld(String fileName) {
        try {
            Files.lines(Paths.get(fileName + ".txt")).forEach(l -> {
                String[] toks = l.split(":");
                float x = Float.parseFloat(toks[0]);
                float y = Float.parseFloat(toks[1]);
                float w = Float.parseFloat(toks[2]);
                float h = Float.parseFloat(toks[3]);
                createBox(new Vector2(x, y), new Vector2(w, h));
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
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        if (enableGrid) {
            renderGrid(shapeRenderer, 200, 200);
        }
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        bodies.forEach(b->renderBox(shapeRenderer, b, underMouseBodies.contains(b)));


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
        if (bodiesToDelete.size()>0) {
            for (Body b: bodiesToDelete) {
                world.destroyBody(b);
            }
            bodiesToDelete.clear();
        }
        world.step(dt, 8, 3);
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
    public boolean mouseMoved(int screenX, int screenY) {
        Vector2 click = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        click = owner.screenToWorld(click).scl(getMetersPerPixel());
        underMouseBodies.clear();
        world.QueryAABB(f -> underMouseBodies.add(f.getBody()), click.x-0.1f, click.y-0.1f,
                click.x+0.1f, click.y+0.1f);
        return false;
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
                createBox(center, new Vector2(Math.abs(topLeft.x-delta.x),Math.abs(topLeft.y-delta.y)));
            }
            if (button == 1) {
                bodiesToDelete.addAll(underMouseBodies);
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

    public void createBox(Vector2 position, Vector2 size) {
        System.out.println("box created @" + position + " with size " + size);
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(position);
        Body groundBody = world.createBody(groundBodyDef);
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(Math.abs(size.x / 2), Math.abs(size.y / 2));
        groundBody.createFixture(groundBox, 0.0f);
        groundBody.setUserData(size.x+":"+size.y+":"+"obstacle"); //TEST!
        groundBox.dispose();
    }

    public World getBox2DWorld() {return world;}

    public boolean isBodyEditorEnabled() {return bodyEditorEnabled;}

    public void enableBodyEditor(boolean enableBodyEditor) {
        this.bodyEditorEnabled = enableBodyEditor;
    }

    public void setTextRenderer(SpriteBatch b) {
        textBatch = b;
    }

    public void testRandMap(List<Rectangle> rects) {
        for (Rectangle r: rects) {
            createBox(new Vector2(r.x + (r.width/2), r.y), new Vector2(r.width, 5));
            createBox(new Vector2(r.x + (r.width/2), r.y + r.height), new Vector2(r.width, 5));
            createBox(new Vector2(r.x + r.width, r.y + (r.height/2)), new Vector2(5, r.height));
            createBox(new Vector2(r.x, r.y + (r.height/2)), new Vector2(5, r.height));
        }
    }

}
