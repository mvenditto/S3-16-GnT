package com.unibo.s3.main_system.graph;

import com.badlogic.gdx.math.Vector;

import java.util.Iterator;

public interface GraphAdapter<T extends Vector<T>> {

    Iterator<T> getVertices();

    Iterator<T> getNeighbors(T vertex);

}
