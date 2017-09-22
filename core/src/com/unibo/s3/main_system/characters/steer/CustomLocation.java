package com.unibo.s3.main_system.characters.steer;

import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

/**
 *
 * @author mvenditto
 * */
public class CustomLocation implements Location<Vector2> {

    private Vector2 position;
    private float orientation;

    public CustomLocation() {
        this(new Vector2());
    }

    public CustomLocation(Vector2 position) {
        this.position = position.cpy();
        this.orientation = 0f;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public float getOrientation() {
        return orientation;
    }

    @Override
    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return vector.angleRad();
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = -(float)Math.sin(angle);
        outVector.y = (float)Math.cos(angle);
        return outVector;
    }

    @Override
    public Location<Vector2> newLocation() {
        return new CustomLocation();
    }
}
