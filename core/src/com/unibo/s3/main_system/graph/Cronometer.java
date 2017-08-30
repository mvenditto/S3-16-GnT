package com.unibo.s3.main_system.graph;

public class Cronometer {
    private boolean running;
    private long startTime;

    public Cronometer(){
        running = false;
    }

    public void start(){
        running = true;
        startTime = System.currentTimeMillis();
    }

    public void stop(){
        startTime = getTime();
        running = false;
    }

    public long getTime(){
        if (running){
            return 	System.currentTimeMillis() - startTime;
        } else {
            return startTime;
        }
    }
}
