package com.unibo.s3.main_system

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.unibo.s3.BasicCameraInputController
import com.unibo.s3.CameraInputControllerKeymap
import com.unibo.s3.InputProcessorAdapter
import com.unibo.s3.main_system.util.ScaleUtils.getPixelsPerMeter

/**
  * This is a base skeleton application. Only implemented aspects are:
  *{{{
  * - base game-loop logic, update/render cycle + pause.
  * - AI timepiece update
  * - camera management
  * - base support for debug rendering (geometric shapes renderer)
  * - from/to screen space <-> world space projections.
  * - input processing
  *}}}
  * @author mvenditto
  */
abstract class AbstractMainApplication extends ApplicationAdapter with InputProcessorAdapter {
  /*rendering stuff*/
  protected var shapeRenderer: ShapeRenderer = _
  protected var font: BitmapFont = _
  protected var textBatch: SpriteBatch = _

  /*camera*/
  protected var cam: OrthographicCamera = _
  private[this] val camSpeed = 20f
  private[this] val camZoomSpeed = 0.1f
  private[this] val camMinZoom = 1.5f
  private[this] val camMaxZoom = 50f
  private[this] val camVirtualWidth = 30f
  private[this] var camController: BasicCameraInputController = _
  private[this] var camKeymap: CameraInputControllerKeymap = _
  protected var paused = false

  protected def doRender(): Unit

  protected def doCustomRender(): Unit

  protected def doUpdate(delta: Float): Unit

  override def create(): Unit = {
    shapeRenderer = new ShapeRenderer
    textBatch = new SpriteBatch
    font = new BitmapFont
    initCamera()
  }

  override def render(): Unit = { //handleCameraInput();
    camController.handleInput()
    cam.update()
    Gdx.graphics.setTitle("camera@" + cam.position + "(" + cam.zoom + ") - FPS -" + Gdx.graphics.getFramesPerSecond)

    /*clear screen & opengl buffers*/
    Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 0.8f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    /*get time elapsed since previous frame was rendered*/
    val dt = Gdx.graphics.getDeltaTime
    if (!paused) {
      GdxAI.getTimepiece.update(dt)
      doUpdate(dt)
    }
    /*set camera projection matrix to all renderers*/
    shapeRenderer.setProjectionMatrix(cam.combined)
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
    doRender()
    shapeRenderer.end()

    doCustomRender()
  }

  override def dispose(): Unit = {
    shapeRenderer.dispose()
    textBatch.dispose()
  }

  override def resize(newWidth: Int, newHeight: Int): Unit = {
    //cam.setToOrtho(false, camVirtualWidth,camVirtualWidth * newWidth / (float)newHeight);
    val ppm = getPixelsPerMeter
    val aspectRatio = newHeight.toFloat / newWidth
    cam.viewportWidth = camVirtualWidth * ppm
    cam.viewportHeight = (camVirtualWidth * ppm) * aspectRatio
    cam.update()
    textBatch.getProjectionMatrix.setToOrtho2D(0, 0, newWidth, newHeight)
  }

  override def keyUp(keycode: Int): Boolean = {
    if (keycode == Keys.P) paused = !paused
    false
  }

  private def initCamera() = {
    val aspectRatio = Gdx.graphics.getHeight.toFloat / Gdx.graphics.getWidth
    // *  getPixelsPerMeter()
    cam = new OrthographicCamera(camVirtualWidth, camVirtualWidth * aspectRatio)
    camKeymap = CameraInputControllerKeymap(zoomOut=Keys.Q, zoomIn=Keys.R)
    camController = BasicCameraInputController(cam,
      camSpeed, camZoomSpeed, camMinZoom, camMaxZoom, camKeymap)
    cam.position.set(cam.viewportWidth / 2, cam.viewportHeight / 2, 0)
    cam.zoom = 5
    cam.update()
  }

  /**
    *
    * @param screenPosition a { @link Vector2} in screen space to be converted to world coordinate.
    * @return the converted { @link Vector2} in world space.
    */
  def screenToWorld(screenPosition: Vector2): Vector2 = {
    val u = new Vector3(screenPosition.x, screenPosition.y, 0)
    cam.unproject(u)
    new Vector2(u.x, u.y)
  }

  /**
    *
    * @param worldPosition a { @link Vector2} in world space to be converted to screen coordinate.
    * @return the converted { @link Vector2} in screen space.
    */
  def worldToScreen(worldPosition: Vector2): Vector2 = {
    val p = new Vector3(worldPosition.x, worldPosition.y, 0)
    cam.project(p)
    new Vector2(p.x, p.y)
  }
}