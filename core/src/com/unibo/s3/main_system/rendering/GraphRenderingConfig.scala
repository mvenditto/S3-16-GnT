package com.unibo.s3.main_system.rendering

import com.badlogic.gdx.graphics.Color

/*A container for the information needed to render a waypoints graph.*/
case class GraphRenderingConfig(
  edgeColor: Color,
  vertexColor: Color,
  vertexRadius: Float)
