package com.unibo.s3.main_system.characters.steer.collisions;

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.unibo.s3.main_system.characters.steer.collisions.gdx.Box2dSquareAABBProximity;

public class Box2dDetectorsFactory implements DetectorsFactory<Vector2> {

    private World world;

    public Box2dDetectorsFactory(World world) {
        this.world = world;
    }

    @Override
    public RaycastCollisionDetector<Vector2> newRaycastCollisionDetector() {
        return new Box2dRaycastCollisionDetector(world);
    }

    @Override
    public Proximity<Vector2> newProximityDetector(float detectionRadius) {
        return new Box2dSquareAABBProximity(null, world, detectionRadius);
    }
}
