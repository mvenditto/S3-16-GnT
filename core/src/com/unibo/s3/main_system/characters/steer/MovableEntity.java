package com.unibo.s3.main_system.characters.steer;

import com.badlogic.gdx.ai.steer.Proximity;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector;

/**
 * An interface describing an entity that can have {@link com.badlogic.gdx.ai.steer.SteeringBehavior} set to it, and
 * can also manage obstacle avoidance, through a collision detector.
 *
 * @author mvenditto
 * */
public interface MovableEntity<T extends Vector<T>> extends SteeringEntity<T>{

    /**
     * Set a {@link RaycastCollisionDetector} to enable this entity to avoid world objects collisions.
     * @param collisionDetector the collision detector to be used by this.
     */
    void setCollisionDetector(RaycastCollisionDetector<T> collisionDetector);

    /**
     * Set a {@link Proximity} to enable this entity to hide behind world objects.
     * @param proximityDetector the proximity detector to be used by this.
     */
    void setProximityDetector(Proximity<T> proximityDetector);

    /**
     * Returns a {@link ComplexSteeringBehaviorBuilder} linked with this {@link MovableEntity}
     * that enables to build a complex {@link com.badlogic.gdx.ai.steer.SteeringBehavior}.
     * When a build...(*) method is called, the created behavior is setted as this entity
     * {@link com.badlogic.gdx.ai.steer.SteeringBehavior}.
     * @return the builded {@link com.badlogic.gdx.ai.steer.SteeringBehavior}.
     */
    ComplexSteeringBehaviorBuilder<T> setComplexSteeringBehavior();

    /**
     *
     * @return An Array of {@link Ray},
     * used by the {@link RaycastCollisionDetector} used by this entity, if present.
     */

    Boolean hasCollisionDetector();

    Ray<T>[] getRays();

    Color getColor();

    void setColor(Color color);

    Object getUserData();

    void setUserData(Object userData);
}
