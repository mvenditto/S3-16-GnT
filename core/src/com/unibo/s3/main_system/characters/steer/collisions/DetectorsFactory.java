package com.unibo.s3.main_system.characters.steer.collisions;

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector;

/**
 * Describes an interface for a factory of 'detectors'.
 *
 * @param <T> Type of vector, either 2D or 3D, implementing the Vector interface
 */
public interface DetectorsFactory<T extends Vector<T>> {

    RaycastCollisionDetector<T> newRaycastCollisionDetector();

    Proximity<T> newProximityDetector(float detectionRadius);

}
