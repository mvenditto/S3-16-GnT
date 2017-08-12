package com.unibo.s3.main_system.characters.steer;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector;

/**
 * A {@link ComplexSteeringBehaviorBuilder} is a convenience interface to build a complex {@link SteeringBehavior}
 * that is the result of a set of single {@link SteeringBehavior} summed up with some strategy.
 *
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * @author mvenditto
 */
public interface ComplexSteeringBehaviorBuilder<T extends Vector<T>> {

    /**
     * Seek the specified target.
     *
     * See: {@link com.badlogic.gdx.ai.steer.behaviors.Seek}
     * @param target the {@link com.badlogic.gdx.ai.utils.Location} to seek.
     * @return this for fluent interface.
     */
    ComplexSteeringBehaviorBuilder<T> seek(Location<T> target);

    /**
     * Flee from the specified target.
     *
     * See: {@link com.badlogic.gdx.ai.steer.behaviors.Flee}
     * @param target the {@link com.badlogic.gdx.ai.utils.Location} to flee from.
     * @return this for fluent interface.
     */
    ComplexSteeringBehaviorBuilder<T> fleeFrom(Location<T> target);

    /**
     * Arrive, possibly at zero velocity, at the specified target.
     *
     * See: {@link com.badlogic.gdx.ai.steer.behaviors.Arrive}
     * @param target the {@link com.badlogic.gdx.ai.utils.Location} to move to.
     * @return this for fluent interface.
     */
    ComplexSteeringBehaviorBuilder<T> arriveTo(Location<T> target);

    /**
     * Pursue the specified {@link Steerable} target.
     *
     * See: {@link com.badlogic.gdx.ai.steer.behaviors.Pursue}
     * @param target the {@link com.badlogic.gdx.ai.steer.Steerable} to pursue.
     * @return this for fluent interface.
     */
    ComplexSteeringBehaviorBuilder<T> pursue(Steerable<T> target);

    /**
     * Evade from the specified {@link Steerable} target.
     *
     * See: {@link com.badlogic.gdx.ai.steer.behaviors.Evade}
     * @param target the {@link com.badlogic.gdx.ai.steer.Steerable} to evade from.
     * @return this for fluent interface.
     */
    ComplexSteeringBehaviorBuilder<T> evadeFrom(Steerable<T> target);

    /**
     * Hide from the specified {@link Steerable} target.
     *
     * See: {@link com.badlogic.gdx.ai.steer.behaviors.Hide}
     * @param target the {@link com.badlogic.gdx.ai.steer.Steerable} to hide from.
     * @return this for fluent interface.
     */
    ComplexSteeringBehaviorBuilder<T> hideFrom(Steerable<T> target);

    /**
     * Add a custom {@link SteeringBehavior}.
     *
     * @param steeringBehavior the custom {@link com.badlogic.gdx.ai.steer.SteeringBehavior} to add.
     * @return this for fluent interface.
     */
    ComplexSteeringBehaviorBuilder<T> add(SteeringBehavior<T> steeringBehavior);

    /**
     * Avoid collision with world's objects,
     * based on {@link com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance}.
     *
     * See: {@link com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance}
     * @return this for fluent interface.
     */
    ComplexSteeringBehaviorBuilder<T> avoidCollisionsWithWorld();

    /**
     * Move giving the impression of a random walk.
     *
     * See: {@link com.badlogic.gdx.ai.steer.behaviors.Wander}
     * @return this for fluent interface.
     */
    ComplexSteeringBehaviorBuilder<T> wander();

    /**
     * Build the resulting {@link com.badlogic.gdx.ai.steer.SteeringBehavior},
     * giving priority to the first generating a non-zero acceleration.
     *
     * See: {@link com.badlogic.gdx.ai.steer.behaviors.PrioritySteering}
     * @return The resulting {@link com.badlogic.gdx.ai.steer.SteeringBehavior}.
     */
    SteeringBehavior<T> buildPriority();

    /**
     * Build the resulting {@link com.badlogic.gdx.ai.steer.SteeringBehavior}, summing up all the single behaviors.
     *
     * @param weights the values to weight the single behaviors.
     *
     * See: {@link com.badlogic.gdx.ai.steer.behaviors.BlendedSteering}
     * @return The resulting {@link com.badlogic.gdx.ai.steer.SteeringBehavior}.
     */
    SteeringBehavior<T> buildBlended(float... weights);

}
