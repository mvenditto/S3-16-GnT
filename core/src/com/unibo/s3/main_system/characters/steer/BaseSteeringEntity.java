package com.unibo.s3.main_system.characters.steer;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * An implementation of a {@link SteeringEntity}. partially taken from gdx-ai tests repository.
 * @author mvenditto
 * */
public class BaseSteeringEntity implements SteeringEntity<Vector2> {

    private Vector2 position;
    private Vector2 linearVelocity;
    private float rotation = 0.0f;
    private float angularVelocity;
    private float boundingRadius = 1.0f; //0.1f;

    private boolean tagged;
    private boolean independentFacing = false;

    private float maxLinearSpeed = 2f;
    private float maxLinearAcceleration = 4f;
    private float maxAngularSpeed = 0.25f;
    private float maxAngularAcceleration = 0.1f;
    private float zeroLinearSpeedThreshold = 0.001f;
    private SteeringBehavior<Vector2> steeringBehavior;

    private static final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<>(new Vector2());

    public BaseSteeringEntity(Vector2 position) {
        this.position = position.cpy();
        this.linearVelocity = new Vector2(0.0f, 0.0f);
    }

    @Override
    public Vector2 getLinearVelocity() {
        return linearVelocity;
    }

    @Override
    public float getAngularVelocity() {
        return angularVelocity;
    }

    @Override
    public float getBoundingRadius() {
        return boundingRadius;
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return zeroLinearSpeedThreshold;

    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        zeroLinearSpeedThreshold = value;
    }

    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public float getOrientation() {
        return (float) Math.toRadians(rotation);
    }

    @Override
    public void setOrientation(float orientation) {
        this.rotation = orientation;
    }

    @Override
    public float vectorToAngle(Vector2 vector) {
        return  (float)Math.atan2(-vector.x, vector.y);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = -(float)Math.sin(angle);
        outVector.y = (float)Math.cos(angle);
        return outVector;
    }

    @Override
    public Location<Vector2> newLocation() {
        return new CustomLocation(this.getPosition().cpy());
    }

    @Override
    public SteeringBehavior<Vector2> getSteeringBehavior () {
        return steeringBehavior;
    }

    @Override
    public void setSteeringBehavior (SteeringBehavior<Vector2> steeringBehavior) {
        this.steeringBehavior = steeringBehavior;
    }

    @Override
    public void act(float delta) {
        if (steeringBehavior != null) {
            steeringBehavior.calculateSteering(steeringOutput);
            applySteering(steeringOutput, delta);
        }
    }

    private void applySteering(SteeringAcceleration<Vector2> steering, float time) {
        position.mulAdd(linearVelocity, time);
        linearVelocity.mulAdd(steering.linear, time).limit(getMaxLinearSpeed());

        if (independentFacing) {
            rotation = rotation + (angularVelocity * time) * MathUtils.radiansToDegrees;
            angularVelocity += steering.angular * time;
        } else {
            if (!linearVelocity.isZero(getZeroLinearSpeedThreshold())) {
                float newOrientation = vectorToAngle(linearVelocity);
                angularVelocity = (newOrientation - rotation * MathUtils.degreesToRadians) * time; // this is superfluous if independentFacing is always true
                rotation = newOrientation * MathUtils.radiansToDegrees;
            }
        }
    }
}
