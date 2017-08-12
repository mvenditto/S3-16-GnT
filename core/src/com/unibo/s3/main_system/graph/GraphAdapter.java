package com.unibo.s3.main_system.graph;

import com.badlogic.gdx.math.Vector;

import java.util.Iterator;

/**
 * {@link GraphAdapter} is an interface needed for rendering graphs independently from the graph class.
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 *
 * @author mvenditto
 * */
public interface GraphAdapter<T extends Vector<T>> {

    Iterator<T> getVertices();

    Iterator<T> getNeighbors(T vertex);

}
