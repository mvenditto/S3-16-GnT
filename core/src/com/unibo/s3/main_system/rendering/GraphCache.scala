package com.unibo.s3.main_system.rendering

import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.graph.GraphAdapter

import scala.collection.mutable

/**
  * This trait enables caching of the waypoints graph in a GeometryRenderer.
  * Used when the graph doesn't changes over time and can be stored and
  * then accessed more efficiently later.
  *
  * @author mvenditto
  */
trait GraphCache extends GeometryRenderer[Vector2] {

  private[this] lazy val graphCache = mutable.AnyRefMap[Vector2, Iterable[Vector2]]()
  private[this] var graphCacheAdapter: GraphAdapter[Vector2] = _
  private[this] var alreadyCached = false

  abstract override def renderGraph(
    shapeRenderer: ShapeRenderer, graph: GraphAdapter[Vector2], config: GraphRenderingConfig): Unit = {

    if(!alreadyCached) {
      graph.getVertices.foreach(v => {
        graphCache += (v -> graph.getNeighbors(v).toIterable)
      })
      graphCacheAdapter = new GraphAdapter[Vector2] {
        override def getNeighbors(vertex: Vector2): Iterator[Vector2] = graphCache(vertex).iterator
        override def getVertices: Iterator[Vector2] = graphCache.keysIterator
      }
      alreadyCached = true
    }
    super.renderGraph(shapeRenderer, graphCacheAdapter, config)
  }
}