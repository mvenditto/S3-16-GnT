package com.unibo.s3.testbed.testbed_modules

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node
import com.badlogic.gdx.scenes.scene2d.utils.{ChangeListener, ClickListener}
import com.badlogic.gdx.scenes.scene2d.{Actor, InputEvent, Stage}
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.{Gdx, InputMultiplexer}
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.{VisLabel, VisTree, _}
import com.unibo.s3.main_system.AbstractMainApplication
import com.unibo.s3.testbed.Testbed

trait Sample {

  var enabled: Boolean = true

  def init(owner: Testbed): Unit

  def setup(): Unit

  def render(shapeRenderer: ShapeRenderer): Unit

  def update(dt: Float): Unit

  def cleanup(): Unit

  def enable(flag: Boolean): Unit = enabled = flag

  def resize(newWidth: Int, newHeight: Int): Unit

  def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit

  def description: String

}

abstract class SampleWithGui extends BaseSample {

  var gui: Stage = _
  var guiEnabled = true

  override def init(owner: Testbed): Unit = {
    super.init(owner)
    gui = new Stage(new ScreenViewport())
    initGui()
  }

  override def update(dt: Float): Unit = {
    super.update(dt)
    gui.act(dt)
  }

  override def cleanup(): Unit = {
    super.cleanup()
    gui.dispose()
  }

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit = {
    super.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(gui)
  }

  override def resize(newWidth: Int, newHeight: Int): Unit = {
    super.resize(newWidth, newHeight)
    gui.getViewport.update(newWidth, newHeight, true)
  }

  def enableGui(flag: Boolean): Unit = guiEnabled = flag

  def renderGui(): Unit = if (guiEnabled) gui.draw()

  def initGui(): Unit

}

abstract class BaseSample extends Sample {

  protected var submodules: Seq[Sample] = List[Sample]()
  protected var owner: Testbed = _

  override def init(owner: Testbed): Unit =
    this.owner = owner
    setup()
    submodules.foreach(sm => sm.init(owner))

  override def render(shapeRenderer: ShapeRenderer): Unit =
    submodules.foreach(sm => sm.render(shapeRenderer))
    submodules.foreach {
      case sm: SampleWithGui => sm.renderGui()
      case _ => None
    }

  override def update(dt: Float): Unit =
    submodules.foreach(sm => sm.update(dt))

  override def cleanup(): Unit =
    submodules.foreach(sm => sm.cleanup())

  override def resize(newWidth: Int, newHeight: Int): Unit =
    submodules.foreach(sm => sm.resize(newWidth, newHeight))

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit =
    submodules.foreach(sm => sm.attachInputProcessors(inputMultiplexer))

  override def setup(): Unit =
    submodules.foreach(sm => sm.setup())
}

case class DummyCircleSample() extends BaseSample {
  override def render(shapeRenderer: ShapeRenderer): Unit = {
    super.render(shapeRenderer)
    shapeRenderer.circle(0f, 0f, 100f)
  }

  override def description: String = "Just a dummy circle."
}

case class DummyCircleSample2() extends BaseSample {
  override def render(shapeRenderer: ShapeRenderer): Unit = {
    super.render(shapeRenderer)
    shapeRenderer.circle(0f, 0f, 200f)
  }

  override def description: String = "Just a dummy circle."
}

case class ScalaTestbed() extends AbstractMainApplication with Testbed {

  private[this] var currentSample: Option[Sample] = Option(DummyCircleSample())
  private[this] var nextSample: Option[Sample] = None
  private[this] var inputMultiplexer: InputMultiplexer = _
  private[this] var gui: Stage = _
  private[this] var defaultPaneSize = 200f
  private[this] var viewport: Cell[_ <: Actor] = _
  private[this] var loadingBar: VisProgressBar = _

  def resizeGui(): Unit = {
    viewport.width(Gdx.graphics.getWidth - (2 * defaultPaneSize))
  }

  private def addSample(node: Node, sampleName: String): Unit = {
    val lab2 = new VisLabel(sampleName)
    lab2.addListener(new ClickListener(){
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        super.clicked(event, x, y)
        nextSample = matchSample(lab2.getText.toString)
      }
    })
    node.add(new Node(lab2))
  }

  private def initGui() = {
    VisUI.load()

    val eastPane = new VisWindow("")
    val westPane = new VisWindow("")

    val tree = new VisTree
    val core = new Node(new VisLabel("Core"))
    core.setExpanded(true)
    val dummy = new Node(new VisLabel("Dummy"))

    addSample(dummy, "Dummy Circle")
    addSample(dummy, "Dummy Circle 2")
    addSample(core, "Entities Playground")

    tree.add(core)
    tree.add(dummy)

    val simulationLabel = new VisLabel("Simulation")
    simulationLabel.setColor(Color.LIGHT_GRAY)
    eastPane.add(simulationLabel).fillX().expandX().row()

    val pauseCheckbox = new VisCheckBox("update")
    pauseCheckbox.addListener(new ChangeListener {
      override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
        pause = !pauseCheckbox.isChecked
      }
    })

    eastPane.add(pauseCheckbox).row()

    val samplesLabel = new VisLabel("Samples")
    samplesLabel.setColor(Color.LIGHT_GRAY)

    eastPane.add(samplesLabel).fillX().expandX().row()
    eastPane.add(tree).expand().fill().row()
    eastPane.setMovable(false)

    loadingBar = new VisProgressBar(0f, 100f, 1f, false)
    loadingBar.setAnimateDuration(2)
    eastPane.add(loadingBar).fillX().expandX()

    val root = new VisTable()
    root.setFillParent(true)
    root.add(westPane).width(defaultPaneSize).fillY().expandY()
    viewport = root.add()
    resizeGui()
    root.add(eastPane).width(defaultPaneSize).fillY().expandY().row()
    gui.addActor(root)
  }

  private def matchSample(sampleName: String): Option[Sample] = sampleName match {
    case "Dummy Circle" => Option(DummyCircleSample())
    case "Dummy Circle 2" => Option(DummyCircleSample2())
    case _ => None
  }

  private def setSample(sample: Sample): Unit = {
    loadingBar.setAnimateDuration(0)
    loadingBar.setValue(0)
    loadingBar.setAnimateDuration(0.2f)

    sample.init(this)
    loadingBar.setValue(25)

    inputMultiplexer.clear()
    sample.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
    inputMultiplexer.addProcessor(gui)
    Gdx.input.setInputProcessor(inputMultiplexer)
    loadingBar.setValue(50)

    currentSample.foreach(old => old.cleanup())
    currentSample = Some(sample)
    nextSample = None
    loadingBar.setValue(100)
  }

  override def create(): Unit = {
    super.create()
    gui = new Stage(new ScreenViewport())
    initGui()

    inputMultiplexer = new InputMultiplexer()
    inputMultiplexer.addProcessor(this)
    inputMultiplexer.addProcessor(gui)

    currentSample.foreach(s => {
      s.init(this)
      s.attachInputProcessors(inputMultiplexer)
    })

    Gdx.input.setInputProcessor(inputMultiplexer)
  }

  override def doRender(): Unit = {
    currentSample.foreach(s => s.render(shapeRenderer))
    currentSample.foreach {
      case s: SampleWithGui =>
        s.renderGui()
      case _ => ()
    }
    gui.draw()
  }

  override def doUpdate(delta: Float): Unit = {
    gui.act(delta)
    nextSample.foreach(s => setSample(s))
    currentSample.foreach(s => s.update(delta))
  }

  override def resize(newWidth: Int, newHeight: Int): Unit = {
    super.resize(newWidth, newHeight)
    currentSample.foreach(s => s.resize(newWidth, newHeight))
    gui.getViewport.update(newWidth, newHeight, true)
    resizeGui()
  }

  override def dispose(): Unit = {
    super.dispose()
    VisUI.dispose()
  }
}