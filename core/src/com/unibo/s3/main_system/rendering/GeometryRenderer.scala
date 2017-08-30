package com.unibo.s3.main_system.rendering

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{Rectangle, Vector}
import com.unibo.s3.main_system.characters.steer.MovableEntity
import com.unibo.s3.main_system.graph.GraphAdapter


case class GraphRenderingConfig(
  edgeColor: Color,
  vertexColor: Color,
  vertexRadius: Float)

/**
 *
 * @author mvenditto
 * */
trait GeometryRenderer[T <: Vector[T]] {

    def renderCharacter(shapeRenderer: ShapeRenderer, character: MovableEntity[T])

    def renderCharacterDebugInfo(shapeRenderer: ShapeRenderer, character: MovableEntity[T])

    def renderGraph(shapeRenderer: ShapeRenderer, graph: GraphAdapter[T], config: GraphRenderingConfig )

    def renderMap(shapeRenderer: ShapeRenderer , map: Iterable[Rectangle])

}
