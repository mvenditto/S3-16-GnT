package com.unibo.s3.main_system.util

import java.util

import org.jgrapht.UndirectedGraph
import org.jgrapht.alg.NeighborIndex
import com.badlogic.gdx.math.Vector
import com.unibo.s3.main_system.graph.GraphAdapter
import scala.collection.JavaConverters._

/**
  * An collection of possibly useful implicit conversions.
  *
  * @author mvenditto
  */
object ImplicitConversions {

  implicit def UndirectedGraphToRenderable[T <: Vector[T], V](g: UndirectedGraph[T, V]):
    GraphAdapter[T] = {

    new GraphAdapter[T] {
      override def getNeighbors(vertex: T): Iterator[T] =
        new NeighborIndex[T, V](g).neighborsOf(vertex).iterator().asScala

      override def getVertices: Iterator[T] = g.vertexSet.iterator().asScala
    }
  }
}
