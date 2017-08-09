package com.unibo.s3.main_system.characters.steer;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector;

public interface SteeringEntity<T extends Vector<T>> extends Steerable<T> {

    void setSteeringBehavior(SteeringBehavior<T> steeringBehavior);

    SteeringBehavior<T> getSteeringBehavior();

    void act(float delta);

}
