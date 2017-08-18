package com.unibo.s3.testbed.testbed_modules
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.main_system.characters.steer.collisions.Box2dSquareAABBProximity.AABB
import com.unibo.s3.main_system.characters.steer.{BaseMovableEntity, MovableEntity}
import com.unibo.s3.main_system.rendering.ScaleUtils
import com.unibo.s3.main_system.rendering.ScaleUtils.getMetersPerPixel
import com.unibo.s3.main_system.world.spatial.{Bounds, QuadTreeNode}
import com.unibo.s3.testbed.Testbed

class QuadTreeTest extends EntitiesSystemModule {

  private[this] var root: QuadTreeNode[MovableEntity[Vector2]] = _

  override def init(owner: Testbed): Unit = {
    super.init(owner)
    root = QuadTreeNode[MovableEntity[Vector2]](Bounds(0, 0, 50, 50))
  }


  override def render(shapeRenderer: ShapeRenderer): Unit = {
    super.render(shapeRenderer)
    val bkColor = shapeRenderer.getColor
    val s = ScaleUtils.getPixelsPerMeter

    shapeRenderer.setColor(Color.GREEN)
    root.traverse(n => {
      shapeRenderer.rect(n.bounds.x * s, n.bounds.y * s, n.bounds.w * s, n.bounds.h * s)
    })

    shapeRenderer.setColor(Color.RED)
    val p = owner.screenToWorld(new Vector2(Gdx.input.getX, Gdx.input.getY))
    p.scl(getMetersPerPixel)
    val b = Bounds(p.x - 5, p.y - 5, 10, 10)

    root.rangeQuery(b)
      .toStream.filter(n => !n.equals(selectedAgent))
      .foreach(n => shapeRenderer.circle(n.getPosition.x * s, n.getPosition.y * s, 0.45f * s))

    shapeRenderer.rect((p.x - 5) * s, (p.y-5) * s, 10f * s, 10f * s)
    shapeRenderer.setColor(bkColor)
  }


  override def update(dt: Float): Unit = {
    super.update(dt)
    root = QuadTreeNode[MovableEntity[Vector2]](Bounds(0, 0, 50, 50))
    if (!entities.isEmpty) {
      for (i <- 0 until entities.size()) {
        root.insert(entities.get(i))
      }
    }
  }
}
