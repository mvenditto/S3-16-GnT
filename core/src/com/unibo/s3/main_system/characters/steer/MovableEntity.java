package com.unibo.s3.main_system.characters.steer;

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.math.Vector;

public interface MovableEntity<T extends Vector<T>> extends SteeringEntity<T>{

    void setCollisionDetector(RaycastCollisionDetector<T> collisionDetector);

    void setProximityDetector(Proximity<T> proximityDetector);

    ComplexSteeringBehaviorBuilder<T> setComplexSteeringBehavior();

    Ray[] getRays();

}
