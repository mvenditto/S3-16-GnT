package com.unibo.s3.testbed.testbed_modules.future

import com.badlogic.gdx.ai.utils.RaycastCollisionDetector
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.{InputEvent, InputListener}
import com.badlogic.gdx.{Input, InputMultiplexer}
import com.kotcrab.vis.ui.widget._
import com.unibo.s3.InputProcessorAdapter
import com.unibo.s3.main_system.characters.steer.{BaseMovableEntity, MovableEntity}
import com.unibo.s3.main_system.rendering.ScaleUtils.{getMetersPerPixel, getPixelsPerMeter}
import com.unibo.s3.main_system.rendering.{GeometryRenderer, GeometryRendererImpl}
import com.unibo.s3.main_system.world.spatial.{Bounds, QuadTreeNode}
import com.unibo.s3.testbed.Testbed
import com.unibo.s3.testbed.testbed_modules.EntitiesSystem

import scala.collection.JavaConversions

class ScalaEntitySystemModule extends SampleWithGui
  with EntitiesSystem[Vector2]
  with InputProcessorAdapter {

  /*simulation*/
  protected var entities: List[MovableEntity[Vector2]] = _
  protected var entitiesToAdd: List[MovableEntity[Vector2]] = _
  protected var entitiesToRemove: List[MovableEntity[Vector2]] = _
  protected var selectedAgent: MovableEntity[Vector2] = _
  protected var collisionDetector: RaycastCollisionDetector[Vector2] = _
  private var qtree: QuadTreeNode[MovableEntity[Vector2]] = _

  /*gui*/
  private var maxLinearSpeedS: VisSlider = _
  private var maxLinearAccelerationS: VisSlider = _
  private var maxAngularSpeedS: VisSlider = _
  private var maxAngularAccelerationS: VisSlider = _
  private var maxLinearSpeedL: VisLabel = _
  private var maxAngularSpeedL: VisLabel = _
  private var maxLinearAccelerationL: VisLabel = _
  private var maxAngularAccelerationL: VisLabel = _
  private var positionL: VisLabel = _
  private var numAgentsL: VisLabel = _
  private var numAgentsToSpawn: VisTextField = _

  /*rendering*/
  private val gr: GeometryRenderer[Vector2] = new GeometryRendererImpl
  private var debugRender: Boolean = false

  /*input*/
  private var isLeftCtrlPressed: Boolean = false

  override def setup(): Unit = {
  }

  private def renderSelectedAgentMarker(shapeRenderer: ShapeRenderer) = {
    if (selectedAgent != null) {
      val center = selectedAgent.getPosition.cpy.scl(getPixelsPerMeter)
      val backupColor = shapeRenderer.getColor
      shapeRenderer.setColor(Color.GREEN)
      shapeRenderer.circle(center.x, center.y, getPixelsPerMeter)
      shapeRenderer.setColor(backupColor)
    }
  }

  private def updateGui() = {
    maxLinearSpeedS.setValue(selectedAgent.getMaxLinearSpeed)
    maxLinearAccelerationS.setValue(selectedAgent.getMaxLinearAcceleration)
    maxAngularSpeedS.setValue(selectedAgent.getMaxAngularSpeed)
    maxAngularAccelerationS.setValue(selectedAgent.getMaxAngularAcceleration)
    maxLinearSpeedL.setText(selectedAgent.getMaxLinearSpeed + "")
    maxLinearAccelerationL.setText(selectedAgent.getMaxLinearAcceleration + "")
    maxAngularSpeedL.setText(selectedAgent.getMaxAngularSpeed + "")
    maxAngularAccelerationL.setText(selectedAgent.getMaxAngularAcceleration + "")
    val pos = selectedAgent.getPosition
    positionL.setText("(" + pos.x.round + "," + pos.y.round + ")")
    numAgentsL.setText(entities.size + "")
  }

  private def createNode(lbl: VisLabel, slid: VisSlider, label: String): VisTable = {
    val t = new VisTable(true)
    t.add(label).width(200).row()
    t.add(slid).width(100).expandX()
    t.add(lbl).width(25).expandX()
    t
  }

  override def initGui(window: VisWindow): Unit = {
    maxLinearSpeedL = new VisLabel("0.0")
    maxAngularSpeedL = new VisLabel("0.0")
    maxLinearAccelerationL = new VisLabel("0.0")
    maxAngularAccelerationL = new VisLabel("0.0")
    positionL = new VisLabel("(?, ?)")
    numAgentsL = new VisLabel("?")
    maxLinearSpeedS = new VisSlider(0.0f, 10.0f, 0.1f, false)
    maxAngularSpeedS = new VisSlider(0.0f, 10.0f, 0.1f, false)
    maxLinearAccelerationS = new VisSlider(0.0f, 10.0f, 0.1f, false)
    maxAngularAccelerationS = new VisSlider(0.0f, 10.0f, 0.1f, false)
    numAgentsToSpawn = new VisTextField("1")
    maxLinearSpeedS.addListener(new InputListener() {
      override def touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Unit = {
        val value = maxLinearSpeedS.getValue
        if (selectedAgent != null) selectedAgent.setMaxLinearSpeed(value)
      }

      override def touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int): Unit = {
        maxLinearSpeedL.setText(maxLinearSpeedS.getValue + "")
      }

      override def touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) = true
    })
    maxLinearAccelerationS.addListener(new InputListener() {
      override def touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Unit = {
        val value = maxLinearAccelerationS.getValue
        if (selectedAgent != null) selectedAgent.setMaxLinearAcceleration(value)
      }

      override def touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int): Unit = {
        maxLinearAccelerationL.setText(maxLinearAccelerationS.getValue + "")
      }

      override def touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) = true
    })
    maxAngularSpeedS.addListener(new InputListener() {
      override def touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Unit = {
        val value = maxAngularSpeedS.getValue
        if (selectedAgent != null) selectedAgent.setMaxAngularSpeed(value)
      }

      override def touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int): Unit = {
        maxAngularSpeedL.setText(maxAngularSpeedS.getValue + "")
      }

      override def touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) = true
    })
    maxAngularAccelerationS.addListener(new InputListener() {
      override def touchUp(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int): Unit = {
        val value = maxAngularAccelerationS.getValue
        if (selectedAgent != null) selectedAgent.setMaxAngularAcceleration(value)
      }

      override def touchDragged(event: InputEvent, x: Float, y: Float, pointer: Int): Unit = {
        maxAngularAccelerationL.setText(maxAngularAccelerationS.getValue + "")
      }

      override def touchDown(event: InputEvent, x: Float, y: Float, pointer: Int, button: Int) = true
    })

    window.setDebug(false)
    window.getTitleLabel.setText("Entities System")
    window.getTitleLabel.setColor(new Color(2/255f, 179/255f, 255/255f, 1f))

    val t0 = new VisTable(true)
    t0.add(new VisLabel("Entities #:")).width(100).expandX()
    t0.add(numAgentsL).width(100).expandX()
    window.add[VisTable](t0).fillX().expandX()
    window.row

    val b = new VisTextButton("Spawn Agent")
    val b2 = new VisTextButton("Toggle debug rendering", "toggle")
    val t = new VisTable(true)
    t.add(b).width(100).expand().padRight(75)
    t.add(numAgentsToSpawn).width(50).expand().row()
    t.add(b2).expandX()
    window.add[VisTable](t)
    window.row()

    b2.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        debugRender = !debugRender
      }
    })

    b.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        var numToSpawn = 1
        try {
          numToSpawn = numAgentsToSpawn.getText.toInt
        } catch {
          case e: Exception =>
        }
        val p = new Vector2(10, 10)
        for ( i <- 0 until numToSpawn) {
          val ax = MathUtils.random(-2f, 2f)
          val ay = MathUtils.random(-2f, 2f)
          val newAgent = spawnEntityAt(new Vector2(p.x + ax, p.y + ay))
          if (collisionDetector != null) newAgent.setCollisionDetector(collisionDetector)
          newAgent.setComplexSteeringBehavior()/*.avoidCollisionsWithWorld*/.wander.buildPriority(true)
        }
      }
    })

    val l2 = new VisLabel("Selected Entity")
    l2.setColor(Color.LIGHT_GRAY)
    window.add[VisLabel](l2).fillX().padTop(8).padBottom(8)
    window.row

    val t1 = new VisTable()
    t1.add(new VisLabel("Position:")).width(100).expandX()
    t1.add(positionL).width(100).expandX()
    window.add[VisTable](t1).fillX().expandX()
    window.row

    window.add(createNode(maxLinearSpeedL, maxLinearSpeedS, "maxLinearSpeed"))
    window.row
    window.add(createNode(maxLinearAccelerationL, maxLinearAccelerationS, "maxLinearAcceleration"))
    window.row
    window.add(createNode(maxAngularAccelerationL, maxAngularAccelerationS, "maxAngularAcceleration"))
    window.row
    window.add(createNode(maxAngularSpeedL, maxAngularSpeedS, "maxAngularSpeed"))
    window.row

    window.add().expandY()
  }

  override def init(owner: Testbed): Unit = {
    super.init(owner)
    this.entities = List[MovableEntity[Vector2]]()
    this.entitiesToAdd = List[MovableEntity[Vector2]]()
    this.entitiesToRemove = List[MovableEntity[Vector2]]()
    this.qtree = new QuadTreeNode[MovableEntity[Vector2]](Bounds(0, 0, 100, 100))
  }

  override def render(shapeRenderer: ShapeRenderer): Unit = {
    if (debugRender) entities.foreach(e => gr.renderCharacterDebugInfo(shapeRenderer, e))
    entities.foreach(e => gr.renderCharacter(shapeRenderer, e))
    renderSelectedAgentMarker(shapeRenderer)
  }

  override def update(dt: Float): Unit = {
    if (entitiesToAdd.nonEmpty) {
      entities = entities ++ entitiesToAdd
      entitiesToAdd = List[MovableEntity[Vector2]]()
    }
    if (entitiesToRemove.nonEmpty) {
      entities = entities filter(e => !entitiesToRemove.contains(e))
      entitiesToRemove = List[MovableEntity[Vector2]]()
    }
    qtree = new QuadTreeNode[MovableEntity[Vector2]](Bounds(0, 0, 100, 100))
    entities.foreach((e: MovableEntity[Vector2]) => qtree.insert(e))
    entities.foreach((e: MovableEntity[Vector2]) => e.act(dt))
    if (selectedAgent != null) updateGui()
  }

  override def cleanup(): Unit = {
  }

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit = {
    super.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
  }

  override def keyDown(keycode: Int): Boolean = {
    if (keycode == Input.Keys.CONTROL_LEFT) isLeftCtrlPressed = true
    false
  }

  override def keyUp(keycode: Int): Boolean = {
    if (keycode == Input.Keys.CONTROL_LEFT) isLeftCtrlPressed = false
    if (keycode == Input.Keys.U) enableGui(!guiEnabled)
    false
  }

  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = {
    var click = new Vector2(screenX, screenY)
    click = owner.screenToWorld(click).scl(getMetersPerPixel)
    if (isLeftCtrlPressed) {
      selectedAgent = null
      if (button == 0) {
        var found = true
        for (a <- entities) {
          if (a.getPosition.dst(click) <= 1.1f) {
            selectedAgent = a
            updateGui()
          }
        }
      }
    }
    false
  }

  override def touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = {
    if (selectedAgent != null && isLeftCtrlPressed) {
      var click = new Vector2(screenX, screenY)
      click = owner.screenToWorld(click).scl(getMetersPerPixel)
      selectedAgent.getPosition.set(click)
    }
    false
  }

  def spawnEntityAt(position: Vector2): MovableEntity[Vector2] = {
    val newAgent = new BaseMovableEntity(position)
    entitiesToAdd :+= newAgent
    newAgent.setColor(new Color(MathUtils.random, MathUtils.random, MathUtils.random, 1.0f))
    if (collisionDetector != null) newAgent.setCollisionDetector(collisionDetector)
    newAgent
  }

  def spawnEntity(newEntity: MovableEntity[Vector2]): Unit = {
    entitiesToAdd :+= newEntity
  }

  def getEntities: java.util.List[MovableEntity[Vector2]] = JavaConversions.seqAsJavaList(entities)

  def getNeighborsOf(entity: MovableEntity[Vector2], searchRadius: Float): java.lang.Iterable[MovableEntity[Vector2]] = {
    val ePos = entity.getPosition
    val res = qtree.rangeQuery(Bounds(ePos.x - searchRadius / 2, ePos.y - searchRadius / 2, searchRadius, searchRadius))
    JavaConversions.asJavaIterable(res)
  }

  override def description: String = "Entities simulation module."
}
