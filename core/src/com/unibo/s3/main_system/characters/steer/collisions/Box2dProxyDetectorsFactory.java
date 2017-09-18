package com.unibo.s3.main_system.characters.steer.collisions;

import akka.actor.ActorRef;
import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.unibo.s3.main_system.world.actors.Box2dRayCastCollisionDetectorProxy;
import com.unibo.s3.main_system.world.actors.Box2dSquareAABBProximityProxy;

/**
 * A factory of 'proxy' detectors, based on WorldActor instead of a World instance directly.
 *
 * @author mvenditto
 */
public class Box2dProxyDetectorsFactory implements DetectorsFactory<Vector2> {

    private ActorRef worldActor;

    public Box2dProxyDetectorsFactory(ActorRef worldActor) {
        this.worldActor = worldActor;
    }

    @Override
    public RaycastCollisionDetector<Vector2> newRaycastCollisionDetector() {
        return new Box2dRayCastCollisionDetectorProxy(worldActor);
    }

    @Override
    public Proximity<Vector2> newProximityDetector(float detectionRadius) {
        return new Box2dSquareAABBProximityProxy(null, worldActor, detectionRadius);
    }
}
