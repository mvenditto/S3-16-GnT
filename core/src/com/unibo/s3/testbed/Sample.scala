package com.unibo.s3.testbed

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.kotcrab.vis.ui.widget.VisWindow

trait Sample {

  var enabled: Boolean = true

  def init(owner: Testbed): Unit

  def initGui(pane: VisWindow): Unit

  def setup(log: String => Unit): Unit

  def render(shapeRenderer: ShapeRenderer): Unit

  def update(dt: Float): Unit

  def cleanup(): Unit

  def enable(flag: Boolean): Unit = enabled = flag

  def resize(newWidth: Int, newHeight: Int): Unit

  def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit

  def getSubmodules: Iterable[Sample]

  def getKeyShortcuts: Option[Map[String, String]]

}

class ModuleMetadata(
  val name: String,
  val desc: Option[String],
  val clazz: Option[String],
  val version: Option[String],
  val category: String
)

object ModuleMetadata {
  def apply(
    name: String,
    desc: Option[String],
    clazz: Option[String],
    version: Option[String],
    category: String): ModuleMetadata =
      new ModuleMetadata(name, desc, clazz, version, category)
}


abstract class BaseSample extends Sample {

  protected var submodules: Seq[Sample] = List[Sample]()
  protected var owner: Testbed = _

  override def init(owner: Testbed): Unit = {
    this.owner = owner
    submodules.foreach(sm => sm.init(owner))
  }

  override def render(shapeRenderer: ShapeRenderer): Unit =
    submodules.foreach(sm => sm.render(shapeRenderer))

  override def update(dt: Float): Unit =
    submodules.foreach(sm => sm.update(dt))

  override def cleanup(): Unit =
    submodules.foreach(sm => sm.cleanup())

  override def resize(newWidth: Int, newHeight: Int): Unit =
    submodules.foreach(sm => sm.resize(newWidth, newHeight))

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit =
    submodules.foreach(sm => sm.attachInputProcessors(inputMultiplexer))

  override def setup(f: String => Unit): Unit =
    submodules.foreach(sm => sm.setup(f))

  override def initGui(window: VisWindow): Unit = {}

  override def getKeyShortcuts: Option[Map[String, String]] = None

  override def getSubmodules: Iterable[Sample] = submodules
}

