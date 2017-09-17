package com.unibo.s3.main_system.rendering

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.unibo.s3.Main
import com.unibo.s3.main_system.modules.BasicModuleWithGui

trait Overlay extends BasicModuleWithGui {

  protected var overlay: Stage = _

  def getOverlay: Stage = overlay

  abstract override def init(owner: Main): Unit = {
    super.init(owner)
    overlay = new Stage(new ScreenViewport())
  }

  abstract override def resize(newWidth: Int, newHeight: Int): Unit = {
    super.resize(newWidth, newHeight)
    overlay.getViewport.update(newWidth, newHeight, true)
  }

  abstract override def update(dt: Float): Unit = {
    super.update(dt)
    overlay.act(dt)
  }

  abstract override def renderGui(): Unit = {
    super.renderGui()
    overlay.draw()
  }

  abstract override def cleanup(): Unit = {
    super.cleanup()
    overlay.dispose()
  }
}
