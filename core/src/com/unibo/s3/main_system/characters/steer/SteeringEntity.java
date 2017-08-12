package com.unibo.s3.main_system.characters.steer;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector;

/**
* A SteeringEntity is a {@link Steerable} entity that as a {@link SteeringBehavior} linked to it
* and that updates itself accordingly each time {@link #act(float)} is called.
* */
public interface SteeringEntity<T extends Vector<T>> extends Steerable<T> {

    /**
     * Set a {@link SteeringBehavior} for this entity.
     * @param steeringBehavior the {@link SteeringBehavior} to set.
     */
    void setSteeringBehavior(SteeringBehavior<T> steeringBehavior);

    /**
     * @return Get the actual {@link SteeringBehavior} linked to this entity.
     */
    SteeringBehavior<T> getSteeringBehavior();

    /**
     * Apply this entity {@link SteeringBehavior} based on time. tipically called each frame.
     * @param delta time in seconds since the last frame.
     */
    void act(float delta);

}
