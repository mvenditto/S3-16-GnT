package com.unibo.s3.testbed.model

import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.kotcrab.vis.ui.widget.VisWindow

/**
  * This traits represents a module, a self-contained logic unit / sample / test, that can
  * be executed by the Testbed.
  * @author mvenditto
  */
trait TestbedModule {

  var enabled: Boolean = true

  /**
    * This methods is called first in the lifecycle of a module.
    * it is executed within the 'Gdx render thread' and has OpenGL context.
    * This method should be used for initialize components needing Gl, for long
    * initialization tasks see [[TestbedModule.setup()]] method.
    * @param owner the Testbed owning this module.
    */
  def init(owner: Testbed): Unit

  /**
    * This method is used to initialize a panel ([[VisWindow]]) reserved by the
    * Testbed to the current running module.
    * @param pane the panel provided by the testbed
    */
  def initGui(pane: VisWindow): Unit

  /**
    * This methods should be used to do post [[TestbedModule.init()]] initialization,
    * tasks the doesn't need Gl context or long running initialization. it is executed on
    * a different thread.
    * @param log
    */
  def setup(log: String => Unit): Unit

  /**
    * Method called each frame for rendering.
    * @param shapeRenderer a [[ShapeRenderer]] provided by the by the testbed.
    */
  def render(shapeRenderer: ShapeRenderer): Unit

  /**
  * A method for custom rendering that is called AFTER [[TestbedModule.render()]]
  * */
  def customRender(): Unit

  /**
    * Method called each frame for updating logic before rendering.
    * @param dt seconds elapsed since last frame was rendered
    */
  def update(dt: Float): Unit

  /**
    * Method that is called on module' life end, dispose used resources here.
    */
  def cleanup(): Unit

  /**
    * Enable or disable this module, if it's disable no update() or render() call is made.
    * @param flag
    */
  def enable(flag: Boolean): Unit = enabled = flag

  /**
    * Notify the module of a change in screen size.
    * @param newWidth
    * @param newHeight
    */
  def resize(newWidth: Int, newHeight: Int): Unit

  /**
    * This method should be used to attach optionally [[com.badlogic.gdx.InputProcessor]]
    * used my this module.
    * @param inputMultiplexer
    */
  def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit

  /**
    * @return an [[Iterable]] of [[TestbedModule]](s) if there is any.
    */
  def getSubmodules: Iterable[TestbedModule]

  /*
  * Return a map of (shortcut -> action) related to this module
  * */
  def getKeyShortcuts: Option[Map[String, String]]

}
