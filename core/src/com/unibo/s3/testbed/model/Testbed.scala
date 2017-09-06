package com.unibo.s3.testbed.model

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.testbed.ui.LogMessage

trait Testbed {

  def screenToWorld(screenPosition: Vector2): Vector2

  def worldToScreen(worldPosition: Vector2): Vector2

  def getCamera: OrthographicCamera

  def getLogger: (LogMessage => Unit)
}