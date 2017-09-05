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
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import com.unibo.s3.Main;

import java.util.HashMap;

public class MenuModule extends BasicModuleWithGui {
    private SpriteBatch textBatch;
    private World world;
    private BitmapFont font;
    private boolean enabled = true;

    private int guardsNum = 5;
    private int thiefsNum = 1;
    private boolean pause = false;
    private boolean simulation = false;
    private boolean mazeMap = true;

    @Override
    public void init(Main owner) {
        super.init(owner);
        this.world = new World(new Vector2(0,0), true);
        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.getData().setScale(1.5f);
        VisUI.load();
        //initMenuGUI();
        initSettingsGUI();
    }

    private void initSettingsGUI() {
        VisWindow table = new VisWindow("Settings");
        //table.setDebug(true);
        table.setMovable(false);
        Label title = table.getTitleLabel();
        title.setColor(Color.GREEN);
        System.out.println("align = " + title.getLabelAlign());
        title.setAlignment(Align.center);

        table.row();
        VisSlider guardsNumS = new VisSlider(2f, 20f, 1f, false);
        guardsNumS.setValue(this.guardsNum);
        VisLabel labGuardsNum = new VisLabel(" " + String.format("%02d", (int) guardsNumS.getValue()));
        table.add(new VisLabel("Guards number: "));
        table.row();
        VisTable tableGuardum = new VisTable();
        tableGuardum.add(guardsNumS).padLeft(10);
        tableGuardum.add(labGuardsNum).padRight(10).padLeft(5);
        table.add(tableGuardum);
        guardsNumS.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                guardsNum = (int) guardsNumS.getValue();
                labGuardsNum.setText(" "+String.format("%02d", guardsNum));
            }
        });

        table.row();
        VisSlider thiefsNumS = new VisSlider(1f, 20f, 1f, false);
        thiefsNumS.setValue(this.thiefsNum);
        VisLabel labThiefsNum = new VisLabel(" " + String.format("%02d", (int) thiefsNumS.getValue()));
        table.add(new VisLabel("Thiefs number: ")).padTop(10);
        table.row();
        VisTable tableThiefsNum = new VisTable();
        tableThiefsNum.add(thiefsNumS);
        tableThiefsNum.add(labThiefsNum);
        table.add(tableThiefsNum);
        thiefsNumS.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                thiefsNum = (int) thiefsNumS.getValue();
                labThiefsNum.setText(" "+String.format("%02d", thiefsNum));
            }
        });

        table.row();
        /*table.add(new VisLabel("Map tipe: "));
        table.row();*/
        VisTable tableThiefType = new VisTable();
        ButtonGroup<VisRadioButton> group = new ButtonGroup<>();
        VisRadioButton simThief = new VisRadioButton("Simulated");
        group.add(simThief);
        VisRadioButton pilThief = new VisRadioButton("Piloted");
        group.add(pilThief);
        tableThiefType.add(new VisLabel("Thief type: "));
        tableThiefType.add(simThief).padRight(5);
        tableThiefType.add(pilThief).padLeft(5);
        simThief.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(simThief.isChecked())
                    simulation = true;
                else
                    simulation = false;
            }
        });
        table.add(tableThiefType).padTop(10).padLeft(5).padRight(5);

        //60x60, 80x60
        table.row();
        VisTable tableMapDimension = new VisTable();

        int numDimension = 2;
        String[] dimensionS = new String[numDimension];
        Integer[][] dimensionI = new Integer[numDimension][2];
        dimensionS[0] = "60x60";
        dimensionI[0][0] = 60;
        dimensionI[0][1] = 60;

        dimensionS[1] = "80x60";
        dimensionI[1][0] = 80;
        dimensionI[1][1] = 60;

        VisSelectBox<String> mapDimension = new VisSelectBox<>();
        mapDimension.setItems(dimensionS);
        tableMapDimension.add(new VisLabel("Map dimensions: "));
        tableMapDimension.add(mapDimension);
        table.add(tableMapDimension).padTop(10);
        mapDimension.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int indexSelected = mapDimension.getSelectedIndex();
                System.out.println(dimensionI[indexSelected][0] + "x" + dimensionI[indexSelected][1]);
            }
        });

        //labirinto: maze - stanze: rooms
        table.row();
        /*table.add(new VisLabel("Map tipe: "));
        table.row();*/
        VisTable tableMapType = new VisTable();
        ButtonGroup<VisRadioButton> groupMaps = new ButtonGroup<>();
        VisRadioButton mazeCheck = new VisRadioButton("Maze");
        groupMaps.add(mazeCheck);
        VisRadioButton roomCheck = new VisRadioButton("Rooms");
        groupMaps.add(roomCheck);
        tableMapType.add(new VisLabel("Map type: "));
        tableMapType.add(mazeCheck).padRight(5);
        tableMapType.add(roomCheck).padLeft(5);
        mazeCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(mazeCheck.isChecked())
                    mazeMap = true;
                else
                    mazeMap = false;
            }
        });
        table.add(tableMapType).padTop(10);

        table.row();
        VisTextButton okButton = new VisTextButton("Start");
        table.add(okButton).padTop(10).padBottom(10);
        okButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
            }
        });
        table.pack();

        GUI.addActor(table);

        table.centerWindow();
        //table.setPosition((Gdx.graphics.getWidth()/2)-(table.getWidth()/2), (Gdx.graphics.getHeight()/2)-(table.getHeight()/2));
    }

    private void initMenuGUI() {
        VisWindow table = new VisWindow("Menu");
        //table.setDebug(true);
        table.setMovable(false);
        Label title = table.getTitleLabel();
        title.setColor(Color.GREEN);
        title.setAlignment(Align.center);

        table.row();
        String pauseString = "Pause";
        String startString = "Start";
        VisTextButton buttonPause = new VisTextButton(pauseString);
        table.add(buttonPause).padTop(10);
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
        VisTextButton buttonStop = new VisTextButton("STOP");
        table.add(buttonStop).padTop(10);

        table.row();
        VisCheckBox debugView = new VisCheckBox(" View debug");
        table.add(debugView).padTop(10).padBottom(10).padLeft(10).padRight(10);

        GUI.addActor(table);

        table.pack();
        table.setPosition(50, Gdx.graphics.getHeight() - 200);
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
