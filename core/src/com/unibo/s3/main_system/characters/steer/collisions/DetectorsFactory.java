package com.unibo.s3.main_system.characters.steer.collisions;

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector;

public interface DetectorsFactory<T extends Vector<T>> {

    RaycastCollisionDetector<T> newRaycastCollisionDetector();

    Proximity<T> newProximityDetector(float detectionRadius);

}
