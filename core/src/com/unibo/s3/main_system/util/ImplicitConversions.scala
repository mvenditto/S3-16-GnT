package com.unibo.s3.main_system.util

import java.util

import org.jgrapht.UndirectedGraph
import org.jgrapht.alg.NeighborIndex
import com.badlogic.gdx.math.Vector
import com.unibo.s3.main_system.graph.GraphAdapter

object ImplicitConversions {

  implicit def UndirectedGraphToRenderable[T <: Vector[T], V](g: UndirectedGraph[T, V]):
    GraphAdapter[T] = {

    new GraphAdapter[T] {
      override def getNeighbors(vertex: T): util.Iterator[T] =
        new NeighborIndex[T, V](g).neighborsOf(vertex).iterator

      override def getVertices: util.Iterator[T] = g.vertexSet.iterator
    }
  }
}
