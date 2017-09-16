package main_system

import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.{ApplicationListener, Gdx}
import com.badlogic.gdx.backends.headless.{HeadlessApplication, HeadlessApplicationConfiguration}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.GdxNativesLoader
import org.junit.runners.BlockJUnit4ClassRunner

/**
* Base test with Gdx dependencies but NO OpenGL context.
* @author mvenditto
* */
class GdxDependencies(klass: Class[_])
  extends BlockJUnit4ClassRunner(klass) with ApplicationListener {

  private val conf = new HeadlessApplicationConfiguration()
  new HeadlessApplication(this, conf)

  override def create(): Unit = {}

  override def pause(): Unit = {}

  override def dispose(): Unit = {}

  override def render(): Unit = {
    GdxAI.getTimepiece.update(Gdx.graphics.getDeltaTime)
  }

  override def resume(): Unit = {}

  override def resize(width: Int, height: Int): Unit = {}
}
