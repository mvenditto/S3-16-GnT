package com.unibo.s3.main_system.tests;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Tests implements ApplicationListener {
    private Stage gui;
    private List<TestClass> testClasses;
    private VisWindow window;
    private boolean ended = false;

    @Override
    public void create() {
        gui = new Stage(new ScreenViewport());

        testClasses = new ArrayList<>();
        testClasses.add(new SystemTest());
        //testClasses.add(new MapTest());
        testClasses.add(new GraphTest());

        initGui();
        ended = true;
    }

    private void initGui() {
        VisUI.load();
        window = new VisWindow("Tests");
        window.getTitleLabel().setAlignment(Align.center);

        doTest();
        window.setFillParent(false);
        window.pack();
        window.centerWindow();
        gui.addActor(window);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {
        if(ended) gui.draw();
    }

    private void doTest() {
        testClasses.forEach(test -> {
            Map<String, Boolean> results = test.doTests();
            results.forEach((text, res) -> {
                VisTable row = new VisTable();
                row.add(new VisLabel(text+ ": "));
                row.add(new VisLabel(res.toString()));
                window.add(row).row();
            });
        });
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}
