package com.unibo.s3.main_system.world.spatial

import com.badlogic.gdx.ai.steer.Steerable
import com.badlogic.gdx.math.Vector2

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/*
* A 2d space rectangular partition.
* */
case class Bounds(x: Float, y: Float, w: Float, h: Float) {

  def contains(p: Vector2): Boolean = x <= p.x && x + w >= p.x && y <= p.y && y + h >= p.y

  def intersect(o: Bounds): Boolean = x < o.x + o.w && x + w > o.x && y < o.y + o.h && y + h > o.y

}

/**
  * An implementation of a QuadTree structure for 2d proximity searches.
  *
  * @param bounds the structure's root space (rectangle)
  * @tparam T any [[Steerable]] of type [[Vector2]]
  *
  * @author mvenditto
  */
class QuadTreeNode[T <: Steerable[Vector2]](bounds: Bounds){

  private var entities = ArrayBuffer.empty[T]
  private val max_entities: Int = 4

  private var ne: QuadTreeNode[T] = _
  private var nw: QuadTreeNode[T] = _
  private var se: QuadTreeNode[T] = _
  private var sw: QuadTreeNode[T] = _

  def getBounds: Bounds = bounds

  private def split(): Unit = {

    val hw = bounds.w / 2
    val hh = bounds.h / 2

    sw = QuadTreeNode(Bounds(bounds.x, bounds.y, hw, hh))
    se = QuadTreeNode(Bounds(bounds.x + hw, bounds.y, hw, hh))
    nw = QuadTreeNode(Bounds(bounds.x, bounds.y + hh, hw, hh))
    ne = QuadTreeNode(Bounds(bounds.x + hw, bounds.y + hh, hw, hh))

  }

  /**
    * Inserts an item into the structure.
    * @param p the item to insert into the QuadTree
    * @return true if there were room left for p to be inserted in.
    */
  def insert(p: T): Boolean = {

    if (bounds.contains(p.getPosition)) {

      if (entities.size < max_entities) {
        entities += p
        return true
      }

      if (nw == null) split()

      if (nw.insert(p)) return true
      if (ne.insert(p)) return true
      if (se.insert(p)) return true
      if (sw.insert(p)) return true
    }
    false
  }

  /**
    * Query for entities in the given area.
    * @param queryArea the [[Bounds]] to search for entities in.
    * @return an [[Iterable]] of [[T]] entities contained in queryArea.
    */
  def rangeQuery(queryArea: Bounds): Iterable[T] = {
    var neighbors = ArrayBuffer[T]()

    if (!bounds.intersect(queryArea)) return neighbors

    neighbors ++= entities.toStream.filter(e => queryArea.contains(e.getPosition))

    if (nw == null) return neighbors

    neighbors ++= nw.rangeQuery(queryArea)
    neighbors ++= ne.rangeQuery(queryArea)
    neighbors ++= sw.rangeQuery(queryArea)
    neighbors ++= se.rangeQuery(queryArea)

    neighbors
  }

  /**
    * Traverse the structure and apply the given function to all nodes.
    * @param consumer a function to be applied to each node of the structure.
    */
  def traverse(consumer: (QuadTreeNode[T]) => Unit) : Unit = {

    consumer(this)

    if (nw != null) {
      nw.traverse(consumer)
    }

    if (ne != null) {
      ne.traverse(consumer)
    }

    if (se != null) {
      se.traverse(consumer)
    }

    if (sw != null) {
      sw.traverse(consumer)
    }
  }
}

object QuadTreeNode {
  def apply[T <: Steerable[Vector2]](bounds: Bounds): QuadTreeNode[T] = new QuadTreeNode[T](bounds)
}

