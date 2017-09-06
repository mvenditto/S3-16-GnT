package com.unibo.s3.testbed.model

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.kotcrab.vis.ui.widget.VisWindow

trait TestbedModule {

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

  def getSubmodules: Iterable[TestbedModule]

  def getKeyShortcuts: Option[Map[String, String]]

}
