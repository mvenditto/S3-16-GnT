package com.unibo.s3.testbed.testbed_modules;

import com.badlogic.gdx.math.Vector;
import com.unibo.s3.main_system.characters.steer.MovableEntity;

import java.util.List;

public interface EntitiesSystem<T extends Vector<T>> {

    MovableEntity<T> spawnEntityAt(T position);

    void spawnEntity(MovableEntity<T> newEntity);

    List<MovableEntity<T>> getEntities();

    Iterable<MovableEntity<T>> getNeighborsOf(MovableEntity<T> entity, float searchRadius);
}
