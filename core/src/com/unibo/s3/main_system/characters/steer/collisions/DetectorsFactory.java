package com.unibo.s3.main_system.characters.steer.collisions;

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.physics.box2d.World;

public interface DetectorsFactory<T extends Vector<T>> {

    RaycastCollisionDetector<T> newRaycastCollisionDetector();

    Proximity<T> newProximityDetector(float detectionRadius);

}
