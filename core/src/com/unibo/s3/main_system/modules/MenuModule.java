package com.unibo.s3.main_system.modules;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.unibo.s3.Main;

public class MenuModule extends BasicModuleWithGui {
    private SpriteBatch textBatch;
    private World world;
    private BitmapFont font;
    private boolean enabled = true;

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
        initMenuGUI();
        initSettingsGUI();
    }

    private void initSettingsGUI() {
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
        SelectBox<String> mapDimension = new SelectBox<String>(skin);
        mapDimension.setItems("Ciao", "come", "va");
        mapDimension.setSelected("come");
        table.add(mapDimension);
        mapDimension.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println(mapDimension.getSelected());
            }
        });

        table.row();
        Button okButton = new TextButton("Ok", skin);
        table.add(okButton);
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
            }
        });

        gui.addActor(table);

        table.setFillParent(false);
        table.setPosition(100, Gdx.graphics.getHeight() - 120);
    }

    private void initMenuGUI() {
        Table table = new Table();

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        Label l = new Label("Menu",skin);
        l.setColor(Color.GREEN);
        table.add(l);

        table.row();


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
        CheckBox debugView = new CheckBox(" View debug", skin);
        table.add(debugView);

        menuGUI.addActor(table);

        table.setFillParent(false);
        table.setPosition(100, Gdx.graphics.getHeight() - 120);
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


    public void setTextRenderer(SpriteBatch b) {
        textBatch = b;
    }
}
