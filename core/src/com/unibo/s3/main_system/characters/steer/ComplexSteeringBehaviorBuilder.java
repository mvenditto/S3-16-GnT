package com.unibo.s3.main_system.characters.steer;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector;

public interface ComplexSteeringBehaviorBuilder<T extends Vector<T>> {

    ComplexSteeringBehaviorBuilder seek(Location<T> target);

    ComplexSteeringBehaviorBuilder fleeFrom(Location<T> target);

    ComplexSteeringBehaviorBuilder arriveTo(Location<T> target);

    ComplexSteeringBehaviorBuilder pursue(Steerable<T> target);

    ComplexSteeringBehaviorBuilder evadeFrom(Steerable<T> target);

    ComplexSteeringBehaviorBuilder hideFrom(Steerable<T> target);

    ComplexSteeringBehaviorBuilder add(SteeringBehavior<T> steeringBehavior);

    ComplexSteeringBehaviorBuilder avoidCollisionsWithWorld();

    ComplexSteeringBehaviorBuilder wander();

    SteeringBehavior<T> buildPriority();

    SteeringBehavior<T> buildBlended(float... weights);

}
