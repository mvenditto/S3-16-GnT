package com.unibo.s3.main_system.characters.steer;


import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.*;
import com.badlogic.gdx.ai.steer.limiters.LinearAccelerationLimiter;
import com.badlogic.gdx.ai.steer.utils.rays.CentralRayWithWhiskersConfiguration;
import com.badlogic.gdx.ai.steer.utils.rays.RayConfigurationBase;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.characters.steer.collisions.Box2dRaycastCollisionDetector;
import com.unibo.s3.main_system.characters.steer.collisions.Box2dSquareAABBProximity;

import java.util.ArrayList;
import java.util.List;

public class BasicMovableEntity extends BasicSteeringEntity implements MovableEntity<Vector2> {

    private RayConfigurationBase<Vector2> rayConfiguration;

    private Wander<Vector2> wander;
    private Pursue<Vector2> pursue;
    private Evade<Vector2> evade;
    private Hide<Vector2> hide;
    private Flee<Vector2>flee;
    private Arrive<Vector2> arrive;
    private Seek<Vector2> seek;
    private RaycastObstacleAvoidance<Vector2> raycastObstacleAvoidance;

    /*collision detection - ray casting*/
    private final static float mainRayLenght = 10.0f;
    private final static float minMainRayLenght = 1.5f;
    private final static float whiskerLenght = 2.0f;
    private final static float whiskerAngle = 35;
    private final static float rayCastingDistFromBoundary = 100;
    private final static float proximityDetectionRadius = 10f;

    /*wander behavior*/
    private final static boolean wanderFaceEnabled = false;
    private final static Limiter wanderLimiter = new LinearAccelerationLimiter(3);
    private final static float defaultTimeToTarget = 1;
    private final static float wanderOffset = 3;
    private final static float wanderOrientation = 5;
    private final static float wanderRadius = 1;
    private final static float wanderRate = MathUtils.PI2 * 4;

    /*arrive behavior*/
    private final static float arriveArrivalTolerance = 0.001f;
    private final static float arriveDeceleratioRadius = 1;

    /*hide behavior*/
    private final static float hideDistFromBoundary = 2f;
    private final static float hideMaxTimePrediction = 0.3f;

    private Color color = Color.WHITE;
    private Object userData;

    public BasicMovableEntity(Vector2 position) {
        super(position);

        wander = new Wander<>(this)
                .setFaceEnabled(wanderFaceEnabled) //
                .setLimiter(wanderLimiter) //
                .setTimeToTarget(defaultTimeToTarget)
                .setWanderOffset(wanderOffset) //
                .setWanderOrientation(wanderOrientation) //
                .setWanderRadius(wanderRadius) //
                .setWanderRate(wanderRate);

        seek = new Seek<>(this);

        flee = new Flee<>(this);

        pursue = new Pursue<>(this, null);

        evade =  new Evade<>(this, null, hideMaxTimePrediction);

        arrive = new Arrive<>(this, null)
                .setTimeToTarget(defaultTimeToTarget)
                .setArrivalTolerance(arriveArrivalTolerance)
                .setDecelerationRadius(arriveDeceleratioRadius);

        hide = new Hide<>(this, null, null)
                .setDistanceFromBoundary(hideDistFromBoundary) //
                .setTimeToTarget(defaultTimeToTarget) //
                .setArrivalTolerance(arriveArrivalTolerance) //
                .setDecelerationRadius(arriveDeceleratioRadius);
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public Object getUserData() {
        return userData;
    }

    @Override
    public void setUserData(Object userData) {
        this.userData = userData;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void act(float dt) {
        super.act(dt);
        if (rayConfiguration != null) {
            ((CentralRayWithWhiskersConfiguration<Vector2>) rayConfiguration)
                    .setRayLength(Math.max(getLinearVelocity().len(), minMainRayLenght));
        }
    }

    @Override
    public void setCollisionDetector(RaycastCollisionDetector<Vector2> raycastCollisionDetector) {

        rayConfiguration = new CentralRayWithWhiskersConfiguration<>(this,
                mainRayLenght,
                whiskerLenght,
                whiskerAngle * MathUtils.degreesToRadians);

        raycastObstacleAvoidance = new RaycastObstacleAvoidance<>(this,
                rayConfiguration,
                raycastCollisionDetector,
                rayCastingDistFromBoundary);
    }

    @Override
    public void setProximityDetector(Proximity<Vector2> proximityDetector) {
        hide.setProximity(proximityDetector);
    }

    @Override
    public Ray<Vector2>[] getRays() {
        return rayConfiguration.getRays();
    }

    @Override
    public ComplexSteeringBehaviorBuilder<Vector2> setComplexSteeringBehavior() {
        return new BehaviorBuilder(this);
    }

    private class BehaviorBuilder implements ComplexSteeringBehaviorBuilder<Vector2> {

        private List<SteeringBehavior<Vector2>> behaviorQueue;
        private BasicSteeringEntity owner;

        BehaviorBuilder(BasicSteeringEntity owner) {
            behaviorQueue = new ArrayList<>();
            this.owner = owner;
        }

        @Override
        public BehaviorBuilder pursue(Steerable<Vector2> target) {
            behaviorQueue.add(pursue.setTarget(target));
            return this;
        }

        @Override
        public BehaviorBuilder evadeFrom(Steerable<Vector2> target) {
            behaviorQueue.add(evade.setTarget(target));
            return this;
        }

        @Override
        public BehaviorBuilder seek(Location<Vector2> target) {
            behaviorQueue.add(seek.setTarget(target));
            return this;
        }

        @Override
        public BehaviorBuilder wander() {
            behaviorQueue.add(wander);
            return this;
        }

        @Override
        public BehaviorBuilder fleeFrom(Location<Vector2> target) {
            behaviorQueue.add(flee.setTarget(target));
            return this;
        }

        @Override
        public BehaviorBuilder arriveTo(Location<Vector2> target) {
            behaviorQueue.add(arrive.setTarget(target));
            return this;
        }

        @Override
        public BehaviorBuilder hideFrom(Steerable<Vector2> target) {
            if (hide.getProximity() != null) {
                behaviorQueue.add(hide.setTarget(target));
            }
            return this;
        }

        @Override
        public BehaviorBuilder add(SteeringBehavior<Vector2> sb) {
            behaviorQueue.add(sb.setOwner(owner));
            return this;
        }

        @Override
        public BehaviorBuilder avoidCollisionsWithWorld() {
            if (raycastObstacleAvoidance != null) {
                behaviorQueue.add(raycastObstacleAvoidance);
            }
            return this;
        }

        @Override
        public SteeringBehavior<Vector2> buildBlended(float... weights) {

            if (weights.length < behaviorQueue.size()) {
                throw new IllegalArgumentException("Too few weights");
            }

            final BlendedSteering<Vector2> finalBehavior = new BlendedSteering<>(owner);

            for (int i = 0; i < behaviorQueue.size(); i++) {
                finalBehavior.add(behaviorQueue.get(i), weights[i]);
            }

            owner.setSteeringBehavior(finalBehavior);
            behaviorQueue.clear();
            return finalBehavior;
        }
        
        @Override
        public SteeringBehavior<Vector2> buildPriority() {
            final PrioritySteering<Vector2> finalBehavior = new PrioritySteering<>(owner, 0.0001f);
            behaviorQueue.forEach(finalBehavior::add);
            owner.setSteeringBehavior(finalBehavior);
            behaviorQueue.clear();
            return finalBehavior;
        }
    }
}

