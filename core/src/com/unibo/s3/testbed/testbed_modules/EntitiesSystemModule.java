package com.unibo.s3.testbed.testbed_modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.unibo.s3.main_system.characters.steer.BaseMovableEntity;
import com.unibo.s3.main_system.characters.steer.MovableEntity;
import com.unibo.s3.main_system.characters.steer.collisions.Box2dRaycastCollisionDetector;
import com.unibo.s3.main_system.rendering.GeometryRendererImpl;
import com.unibo.s3.main_system.rendering.GeometryRenderer;
import com.unibo.s3.testbed.Testbed;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import static com.unibo.s3.main_system.rendering.ScaleUtils.*;

public class EntitiesSystemModule extends BasicTestbedModuleWithGui implements EntitiesSystem<Vector2> {

    /*simulation*/
    protected List<MovableEntity<Vector2>> entities;
    protected List<MovableEntity<Vector2>> entitiesToAdd;
    protected List<MovableEntity<Vector2>> entitiesToRemove;
    protected MovableEntity<Vector2> selectedAgent = null;

    /*gui*/
    private Slider maxLinearSpeedS;
    private Slider maxLinearAccelerationS;
    private Slider maxAngularSpeedS;
    private Slider maxAngularAccelerationS;

    private Label maxLinearSpeedL;
    private Label maxAngularSpeedL;
    private Label maxLinearAccelerationL;
    private Label maxAngularAccelerationL;
    private Label positionL;
    private Label numAgentsL;
    private TextField numAgentsToSpawn;

    /*rendering*/
    private GeometryRenderer<Vector2> gr = new GeometryRendererImpl();
    private boolean debugRender = false;

    /*input*/
    private boolean isLeftCtrlPressed = false;

    protected RaycastCollisionDetector<Vector2> collisionDetector;

    public void setCollisionDetector(RaycastCollisionDetector<Vector2> collisionDetector) {
        this.collisionDetector = collisionDetector;
    }

    public MovableEntity<Vector2> spawnEntityAt(Vector2 position) {
        final MovableEntity<Vector2> newAgent = new BaseMovableEntity(position);
        entitiesToAdd.add(newAgent);

        newAgent.setColor(new Color(MathUtils.random(),MathUtils.random(),MathUtils.random(),1.0f));

        if (collisionDetector != null) {
            newAgent.setCollisionDetector(collisionDetector);
        }
        return newAgent;
    }

    @Override
    public void spawnEntity(MovableEntity<Vector2> newEntity) {
        entitiesToAdd.add(newEntity);
    }

    public List<MovableEntity<Vector2>> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    private void renderSelectedAgentMarker(ShapeRenderer shapeRenderer) {

        if (selectedAgent != null ) {
            final Vector2 center = selectedAgent.getPosition().cpy().scl(getPixelsPerMeter());
            Color backupColor = shapeRenderer.getColor();
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.circle(center.x, center.y, getPixelsPerMeter());
            shapeRenderer.setColor(backupColor);
        }
    }

    private void updateGui() {
        maxLinearSpeedS.setValue(selectedAgent.getMaxLinearSpeed());
        maxLinearAccelerationS.setValue(selectedAgent.getMaxLinearAcceleration());
        maxAngularSpeedS.setValue(selectedAgent.getMaxAngularSpeed());
        maxAngularAccelerationS.setValue(selectedAgent.getMaxAngularAcceleration());

        maxLinearSpeedL.setText(selectedAgent.getMaxLinearSpeed()+"");
        maxLinearAccelerationL.setText(selectedAgent.getMaxLinearAcceleration()+"");
        maxAngularSpeedL.setText(selectedAgent.getMaxAngularSpeed()+"");
        maxAngularAccelerationL.setText(selectedAgent.getMaxAngularAcceleration()+"");

        Vector2 pos = selectedAgent.getPosition();
        positionL.setText("("+Math.round(pos.x)+","+Math.round(pos.y)+")");
        numAgentsL.setText(entities.size()+"");
    }

    private void initGui() {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        maxLinearSpeedL = new Label("0.0", skin);
        maxAngularSpeedL = new Label("0.0", skin);
        maxLinearAccelerationL = new Label("0.0", skin);
        maxAngularAccelerationL = new Label("0.0", skin);
        positionL = new Label("(?, ?)", skin);
        numAgentsL = new Label("?", skin);

        maxLinearSpeedS = new Slider(0.0f, 10.0f, 0.1f, false, skin);
        maxAngularSpeedS = new Slider(0.0f, 10.0f, 0.1f, false, skin);
        maxLinearAccelerationS = new Slider(0.0f, 10.0f, 0.1f, false, skin);
        maxAngularAccelerationS = new Slider(0.0f, 10.0f, 0.1f, false, skin);
        numAgentsToSpawn = new TextField("1", skin);

        maxLinearSpeedS.addListener(new InputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                float value = maxLinearSpeedS.getValue();
                if (selectedAgent != null) {
                    selectedAgent.setMaxLinearSpeed(value);
                }
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                maxLinearSpeedL.setText(maxLinearSpeedS.getValue()+"");
            }

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        maxLinearAccelerationS.addListener(new InputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                float value = maxLinearAccelerationS.getValue();
                if (selectedAgent != null) {
                    selectedAgent.setMaxLinearAcceleration(value);
                }
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                maxLinearAccelerationL.setText(maxLinearAccelerationS.getValue()+"");
            }

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        maxAngularSpeedS.addListener(new InputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                float value = maxAngularSpeedS.getValue();
                if (selectedAgent != null) {
                    selectedAgent.setMaxAngularSpeed(value);
                }
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                maxAngularSpeedL.setText(maxAngularSpeedS.getValue()+"");
            }

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        maxAngularAccelerationS.addListener(new InputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                float value = maxAngularAccelerationS.getValue();
                if (selectedAgent != null) {
                    selectedAgent.setMaxAngularAcceleration(value);
                }
            }

            @Override
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                maxAngularAccelerationL.setText(maxAngularAccelerationS.getValue()+"");
            }

            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        Table table = new Table();

        Label l = new Label("Simulation Module",skin);
        l.setColor(Color.GREEN);
        table.add(l);

        table.row();
        table.add(new Label("Agents #: ", skin));
        table.add(numAgentsL);
        
        table.row();
        table.add(new Label("position", skin));
        table.add(positionL).width(100);

        table.row();
        table.add(new Label("maxLinearSpeed:", skin));
        table.add(maxLinearSpeedS).width(100);
        table.add(maxLinearSpeedL).width(20);

        table.row();
        table.add(new Label("maxAngularSpeed:", skin));
        table.add(maxAngularSpeedS).width(100);
        table.add(maxAngularSpeedL).width(20);

        table.row();
        table.add(new Label("maxLinearAcceleration:", skin));
        table.add(maxLinearAccelerationS).width(100);
        table.add(maxLinearAccelerationL).width(20);

        table.row();
        table.add(new Label("maxAngularAcceleration:", skin));
        table.add(maxAngularAccelerationS).width(100);
        table.add(maxAngularAccelerationL).width(20);

        table.row();
        Button b = new TextButton("Spawn Agent", skin);
        b.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int numToSpawn = 1;
                try {
                    numToSpawn = Integer.parseInt(numAgentsToSpawn.getText());
                } catch (Exception e) {}

                for (int i = 0; i < numToSpawn; i++) {
                    float ax = MathUtils.random(-2f, 2f);
                    float ay = MathUtils.random(-2f, 2f);
                    MovableEntity<Vector2> newAgent = spawnEntityAt(new Vector2(ax, ay));
                    newAgent.setCollisionDetector(collisionDetector);

                    newAgent.setComplexSteeringBehavior()
                            .avoidCollisionsWithWorld()
                            .wander()
                            .buildPriority(true);

                }
            }
        });
        table.add(b);

        table.add(numAgentsToSpawn).fill(false).width(50f);

        Button b2 = new TextButton("Toggle debug render.", skin);
        b2.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                debugRender = !debugRender;
            }
        });
        table.add(b2);

        //table.setDebug(true);
        table.setFillParent(true);

        table.setY(275);

        gui.addActor(table);

        table.addListener((new DragListener() {
            public void touchDragged (InputEvent event, float x, float y, int pointer) {
                if (isLeftCtrlPressed) {
                    table.moveBy(x - table.getWidth() / 2, y - table.getHeight() / 2);
                }
            }
        }));

        gui.getRoot().addCaptureListener(new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if (!(event.getTarget() instanceof TextField)) gui.setKeyboardFocus(null);
                return false;
            }
        });
    }

    @Override
    public void init(Testbed owner) {
        super.init(owner);
        this.entities = new ArrayList<>();
        this.entitiesToAdd = new ArrayList<>();
        this.entitiesToRemove = new ArrayList<>();
        initGui();
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {

        if (debugRender) {
            entities.forEach(e -> gr.renderCharacterDebugInfo(shapeRenderer, e));
        }
        entities.forEach(e -> gr.renderCharacter(shapeRenderer, e));
        renderSelectedAgentMarker(shapeRenderer);
    }

    @Override
    public void update(float dt) {

        if(entitiesToAdd.size() > 0) {
            entities.addAll(entitiesToAdd);
            entitiesToAdd.clear();
        }

        if(entitiesToRemove.size() > 0) {
            entities.removeAll(entitiesToRemove);
            entitiesToRemove.clear();
        }

        entities.forEach(e->e.act(dt));

        if (selectedAgent != null) {
            updateGui();
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void attachInputProcessors(InputMultiplexer inputMultiplexer) {
        super.attachInputProcessors(inputMultiplexer);
        inputMultiplexer.addProcessor(this);
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.CONTROL_LEFT) {
            isLeftCtrlPressed = true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.CONTROL_LEFT) {
            isLeftCtrlPressed = false;
        }

        if (keycode == Input.Keys.U) {
            enableGui(!super.guiEnabled);
        }

        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        Vector2 click = new Vector2(screenX, screenY);
        click = owner.screenToWorld(click).scl(getMetersPerPixel());

        if (isLeftCtrlPressed) {
            selectedAgent = null;

            if (button == 0) {
                for (MovableEntity<Vector2> a : entities) {
                    if (a.getPosition().dst(click) <= 1.1f) {
                        selectedAgent = a;
                        updateGui();
                        break;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (selectedAgent != null && isLeftCtrlPressed) {
            Vector2 click = new Vector2(screenX, screenY);
            click = owner.screenToWorld(click).scl(getMetersPerPixel());
            selectedAgent.getPosition().set(click);
        }
        return false;
    }
}
