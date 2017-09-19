package com.unibo.s3.main_system.graph

import com.badlogic.gdx.math.Vector

/**
  * `RenderableGraphAdapter` is an interface needed for rendering graphs independently from the graph class.
  * @tparam T Type of vector, either 2D or 3D, implementing the `Vector`interface
  *
  * @author mvenditto
  * */
trait GraphAdapter[T <: Vector[T]] {

    def getVertices: Iterator[T]

    def getNeighbors(vertex: T): Iterator[T]

}
