package com.unibo.s3.main_system.remote;

import com.badlogic.gdx.ApplicationAdapter;

public class MainRemote extends ApplicationAdapter {
    @Override
    public void create() {
        BootstrapRemote module = new BootstrapRemote();
        module.init();
    }
}
