package com.unibo.s3.testbed.modules

import com.badlogic.gdx.ai.steer.proximities.FieldOfViewProximity
import com.badlogic.gdx.ai.steer.{Proximity, Steerable}
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.unibo.s3.main_system.world.spatial.{Bounds, QuadTreeNode}
import com.unibo.s3.main_system.util.ScaleUtils
import com.unibo.s3.testbed.model.Testbed
import com.unibo.s3.main_system.util.GdxImplicits._

class QuadTreeTest extends EntitySystemModule {

  private var root: QuadTreeNode[Steerable[Vector2]] = _

  private val fovAngle = 120f //degrees
  private val fovRadius = 5f //meters

  override def init(owner: Testbed): Unit = {
    super.init(owner)
    root = QuadTreeNode[Steerable[Vector2]](Bounds(0, 0, 50, 50))
  }


  override def render(shapeRenderer: ShapeRenderer): Unit = {
    super.render(shapeRenderer)
    val bkColor = shapeRenderer.getColor
    val s = ScaleUtils.getPixelsPerMeter

    // draw quad tree partitions
    shapeRenderer.setColor(Color.GREEN)
    root.traverse(n => {
      shapeRenderer.rect(
        n.getBounds.x * s, n.getBounds.y * s, n.getBounds.w * s, n.getBounds.h * s)
    })

    shapeRenderer.setColor(Color.RED)
    if (selectedAgent != null) {

      val p = selectedAgent.getPosition
      val b = Bounds(p.x - 5, p.y - 5, 10, 10)
      val neighbors = root.rangeQuery(b).toList

      val fov = new FieldOfViewProximity[Vector2](selectedAgent, neighbors.asGdxArray,
        fovRadius, MathUtils.degreesToRadians * fovAngle)

      fov.findNeighbors(new Proximity.ProximityCallback[Vector2] {
        override def reportNeighbor(n: Steerable[Vector2]): Boolean = {
          shapeRenderer.circle(n.getPosition.x * s, n.getPosition.y * s, 0.45f * s)
          true
        }
      })

      // draw cone of view
      shapeRenderer.setColor(Color.CYAN)
      //val angle = fov.getAngle * MathUtils.radiansToDegrees
      shapeRenderer.arc(selectedAgent.getPosition.x * s, selectedAgent.getPosition.y * s, fov.getRadius * s,
        selectedAgent.getOrientation * MathUtils.radiansToDegrees - fovAngle / 2f + 90f, fovAngle)
    }
    shapeRenderer.setColor(bkColor)
  }

  override def update(dt: Float): Unit = {
    super.update(dt)
    root = QuadTreeNode[Steerable[Vector2]](Bounds(0, 0, 50, 50))
    if (entities.nonEmpty) {
      for (i <- entities.indices) {
        root.insert(entities(i))
      }
    }
  }
}
