package com.unibo.s3.testbed.model

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.kotcrab.vis.ui.widget.VisWindow


abstract class BaseTestbedModule extends TestbedModule {

  protected var submodules: Seq[TestbedModule] = List[TestbedModule]()
  protected var owner: Testbed = _

  override def init(owner: Testbed): Unit = {
    this.owner = owner
    submodules.foreach(sm => sm.init(owner))
  }

  override def render(shapeRenderer: ShapeRenderer): Unit =
    submodules.foreach(sm => sm.render(shapeRenderer))

  override def customRender(): Unit =
    submodules.foreach(sm => sm.customRender())

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

  override def getSubmodules: Iterable[TestbedModule] = submodules
}

