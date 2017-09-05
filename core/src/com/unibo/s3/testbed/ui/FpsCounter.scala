package com.unibo.s3.testbed.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.kotcrab.vis.ui.widget.{VisLabel, VisTable, VisWindow}

class FpsCounter(title: String = "FPS") extends VisWindow(title) {

  private[this] val fpsLabel = new VisLabel()

  add(fpsLabel).center()

  private def updateFpsLabel() = {
    val fpsNum = Gdx.graphics.getFramesPerSecond
    val col = fpsNum match {
      case fps if fps < 30 => Color.RED
      case fps if fps >= 30 && 50 >= fps => Color.YELLOW
      case fps if fps > 50 => Color.GREEN
    }
    fpsLabel.setText(fpsNum.toString)
    fpsLabel.setColor(col)
  }

  override def act(delta: Float): Unit = {
    super.act(delta)
    updateFpsLabel()
  }
}
