package com.unibo.s3.main_system.rendering

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{Rectangle, Vector}
import com.unibo.s3.main_system.characters.steer.MovableEntity
import com.unibo.s3.main_system.graph.GraphAdapter

/**
  *
  * This trait describes a renderer specialized in geometry/debug
  * rendering of main game entities.
  *
  * @author mvenditto
  * */
trait GeometryRenderer[T <: Vector[T]] {
  /**
    * Render the given character using the given renderer.
    * @param shapeRenderer a [[ShapeRenderer]]
    * @param character the character to be rendered.
    */
  def renderCharacter(shapeRenderer: ShapeRenderer, character: MovableEntity[T])
  /**
    * Render the given character rays, cone of view(if exists), using the given renderer.
    * @param shapeRenderer a [[ShapeRenderer]]
    * @param character the character to render the debug info of.
    */
  def renderCharacterDebugInfo(shapeRenderer: ShapeRenderer, character: MovableEntity[T])
  /**
    * Render the given waypoints graph using the given renderer.
    * @param shapeRenderer a [[ShapeRenderer]]
    * @param graph the graph to render the debug info of.
    */
  def renderGraph(shapeRenderer: ShapeRenderer, graph: GraphAdapter[T], config: GraphRenderingConfig )
  /**
    * Render the given map(tiles) using the given renderer.
    * @param shapeRenderer a [[ShapeRenderer]]
    * @param map the map tiles to render the debug info of.
    */
  def renderMap(shapeRenderer: ShapeRenderer , map: Iterable[Rectangle])

}
