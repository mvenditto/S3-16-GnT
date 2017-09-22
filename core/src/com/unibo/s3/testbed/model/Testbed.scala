package com.unibo.s3.testbed.model

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector2
import com.unibo.s3.testbed.ui.LogMessage

/**
  * This trait defines a Testbed, an object that manges a
  * predefined graphical environment in which execute
  * visual samples and tests.
  * @author mvenditto
  */
trait Testbed {

  /**
    * Converts coordinates from screen to world space.
    * @param screenPosition a position in screen space
    * @return the screenPosition converted in world space
    */
  def screenToWorld(screenPosition: Vector2): Vector2

  /**
    * Converts coordinates from world to screen space.
    * @param worldPosition a position in world space
    * @return the worldPosition converted in screen space
    */
  def worldToScreen(worldPosition: Vector2): Vector2

  /**
  * @return the [[OrthographicCamera]] of this [[Testbed]]
  * */
  def getCamera: OrthographicCamera

  /**
    * @return a logging function that manages [[LogMessage]]
    */
  def getLogger: (LogMessage => Unit)
}