package com.unibo.s3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input._
import com.badlogic.gdx.graphics.OrthographicCamera
import Math.{max, min}

case class CameraInputControllerKeymap(
  up: Int = Keys.UP,
  down: Int = Keys.DOWN,
  left: Int = Keys.LEFT,
  right: Int = Keys.RIGHT,
  zoomOut: Int = Keys.PLUS,
  zoomIn: Int = Keys.MINUS
)

class BasicCameraInputController(
  cam: OrthographicCamera,
  translationSpeed: Float,
  zoomSpeed: Float,
  minZoom: Float = 1.5f,
  maxZoom: Float = 50f,
  keymap: CameraInputControllerKeymap) {

  private def isPressed(key: Int): Boolean =
    Gdx.input.isKeyPressed(key)

  def handleInput(): Unit = {
    if (isPressed(keymap.zoomIn)) cam.zoom = max(cam.zoom - zoomSpeed, minZoom)
    if (isPressed(keymap.zoomOut)) cam.zoom = min(cam.zoom + zoomSpeed, maxZoom)
    if (isPressed(keymap.left)) cam.translate(-translationSpeed, 0, 0)
    if (isPressed(keymap.right)) cam.translate(translationSpeed, 0, 0)
    if (isPressed(keymap.down)) cam.translate(0, -translationSpeed, 0)
    if (isPressed(keymap.up)) cam.translate(0, translationSpeed, 0)
  }
}
