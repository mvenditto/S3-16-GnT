package com.unibo.s3.main_system.characters.steer;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector;

public interface ComplexSteeringBehaviorBuilder<T extends Vector<T>> {

    ComplexSteeringBehaviorBuilder<T> seek(Location<T> target);

    ComplexSteeringBehaviorBuilder<T> fleeFrom(Location<T> target);

    ComplexSteeringBehaviorBuilder<T> arriveTo(Location<T> target);

    ComplexSteeringBehaviorBuilder<T> pursue(Steerable<T> target);

    ComplexSteeringBehaviorBuilder<T> evadeFrom(Steerable<T> target);

    ComplexSteeringBehaviorBuilder<T> hideFrom(Steerable<T> target);

    ComplexSteeringBehaviorBuilder<T> add(SteeringBehavior<T> steeringBehavior);

    ComplexSteeringBehaviorBuilder<T> avoidCollisionsWithWorld();

    ComplexSteeringBehaviorBuilder<T> wander();

    SteeringBehavior<T> buildPriority();

    SteeringBehavior<T> buildBlended(float... weights);

}
