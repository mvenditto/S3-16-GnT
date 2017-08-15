package com.unibo.s3.main_system.characters.steer.collisions;

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class Box2dDetectorsFactory implements DetectorsFactory<Vector2> {
    @Override
    public RaycastCollisionDetector<Vector2> newRaycastCollisionDetector(World world) {
        return new Box2dRaycastCollisionDetector(world);
    }

    @Override
    public Proximity<Vector2> newProximityDetector(World world, float detectRadius) {
        return new Box2dSquareAABBProximity(null, world, detectRadius);
    }
}
