package com.unibo.s3.testbed.future

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.utils.{ChangeListener, ClickListener}
import com.badlogic.gdx.scenes.scene2d.{Actor, InputEvent, Stage}
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.{Gdx, InputMultiplexer}
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.util.ToastManager
import com.kotcrab.vis.ui.widget.toast.Toast
import com.kotcrab.vis.ui.widget.{VisLabel, VisTree, _}
import com.unibo.s3.main_system.AbstractMainApplication
import com.unibo.s3.testbed.Testbed
import com.unibo.s3.testbed.future.samples._
import com.unibo.s3.testbed.future.ui.KeyHelpTable

trait TestbedController {

  def onSampleSelection(name: String): Unit

  def onPause(pause: Boolean): Unit

}

case class TestbedGui(controller: TestbedController) {

  private var stage: Stage = _
  private var menuBar: MenuBar = _
  private var viewport: Cell[_ <: Actor] = _
  private var loadingBar: VisProgressBar = _
  private var loadingLog: VisLabel = _
  private var samplePane: VisWindow = _
  private var toastManager: ToastManager = _
  private var currSampleShortcuts: Option[KeyHelpTable] = None

  private val defaultPaneSize = 200f
  private val currentSampleMenuWidth = 300f
  private var lastTransitionOut = false
  private var customBlue = new Color(2/255f, 179/255f, 255/255f, 1f)

  def resize(newWidth: Integer, newHeight: Integer): Unit = {
    stage.getViewport.update(newWidth, newHeight, true)
    viewport.width(Gdx.graphics.getWidth - (currentSampleMenuWidth + defaultPaneSize))
  }

  def init(): Unit = {
    stage = new Stage(new ScreenViewport())
    VisUI.load()
    toastManager = new ToastManager(stage)
    toastManager.setAlignment(Align.bottomRight)

    val eastPane = new VisWindow("")

    eastPane.getTitleLabel.setText("Testbed menu")
    eastPane.getTitleLabel.setColor(customBlue)

    val simulationLabel = new VisLabel("Simulation")
    simulationLabel.setColor(Color.LIGHT_GRAY)
    eastPane.add(simulationLabel).fillX().expandX().row()

    val pauseCheckbox = new VisCheckBox("update")
    pauseCheckbox.addListener(new ChangeListener {
      override def changed(event: ChangeEvent, actor: Actor): Unit =
        controller.onPause(!pauseCheckbox.isChecked)
    })

    eastPane.add(pauseCheckbox).row()

    val samplesLabel = new VisLabel("Samples")
    samplesLabel.setColor(Color.LIGHT_GRAY)

    val tree = buildSamplesTree()
    eastPane.add(samplesLabel).fillX().expandX().row()
    eastPane.add(tree).expand().fill().row()
    eastPane.setMovable(false)

    /*loading bar and log*/
    loadingLog = new VisLabel("")
    loadingLog.setColor(customBlue)
    eastPane.add(loadingLog).fillX().expandX().row()

    loadingBar = new VisProgressBar(0f, 100f, 1f, false)
    loadingBar.setAnimateDuration(2)
    eastPane.add(loadingBar).fillX().expandX()

    /*current sample panel (left panel)*/
    samplePane = new VisWindow("")
    samplePane.setWidth(defaultPaneSize)
    samplePane.getTitleLabel.setColor(customBlue)
    samplePane.setKeepWithinParent(false)
    samplePane.setKeepWithinStage(false)

    /*menu bar*/
    menuBar = new MenuBar()
    createMenu()

    /*root table*/
    val root = new VisTable()
    root.setFillParent(true)
    root.add(menuBar.getTable).fillX().expandX().row()
    root.add(samplePane).width(currentSampleMenuWidth).fillY().expandY()
    viewport = root.add()
    resize(Gdx.graphics.getWidth, Gdx.graphics.getHeight)
    root.add(eastPane).width(defaultPaneSize).fillY().expandY()

    stage.addActor(root)
  }

  def addSample(node: Node, sampleName: String): Unit = {
    val lab2 = new VisLabel(sampleName)
    lab2.addListener(new ClickListener(){
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        super.clicked(event, x, y)
        controller.onSampleSelection(lab2.getText.toString)
      }
    })
    node.add(new Node(lab2))
  }

  def setProgress(value: Float): Unit = loadingBar.setValue(value)

  def setLoadingLogText(msg: String): Unit = loadingLog.setText(msg)

  def resetLoadingBar(): Unit = {
    loadingBar.setAnimateDuration(0)
    loadingBar.setValue(0)
    loadingBar.setAnimateDuration(0.2f)
  }

  def resetSamplePane(): Unit = samplePane.clear()

  def displayModuleLoadedToast(name: String): Unit = {
    val toast = new Toast("dark", new VisTable(true))
    toast.getContentTable.add(new VisLabel("Loaded module: "))
    val t = new VisLabel(name + "!")
    t.setColor(Color.CORAL)
    toast.getContentTable.add(t)
    toastManager.show(toast, 5)
    centerToast(toast)
  }

  def setCurrentSampleShortcuts(shortcuts: Map[String, String]): Unit = {
    currSampleShortcuts = None
    val st = new KeyHelpTable(true)
    shortcuts.foreach(k => {
        if (!k._1.contains("+")) st.addKeyBinding(k._1, k._2)
        else st.addKeyBinding(k._1.split("\\+"), k._2)
      })
    currSampleShortcuts = Option(st)
  }

  def attachInputProcessor(inputMultiplexer: InputMultiplexer): Unit =
    inputMultiplexer.addProcessor(stage)

  def getSamplePane: VisWindow = samplePane

  def render(): Unit = stage.draw()

  def update(dt: Float): Unit = stage.act(dt)

  def toggleSamplePane(): Unit = {
    samplePane.addAction(
      Actions.moveTo(if (lastTransitionOut) -samplePane.getWidth else 0,
        0, 0.30f))
    lastTransitionOut = !lastTransitionOut
  }

  private def createMenu() = {
    val fileMenu = new Menu("File")
    val helpMenu = new Menu("Help")

    val testbedKeysHelp = new MenuItem("Testbed shortcuts", new ChangeListener {
      override def changed(event: ChangeEvent, actor: Actor): Unit = {
        val keys = new KeyHelpTable(true)
        keys.addKeyBinding("p", "pause.")
        keys.addKeyBinding("h", "hide left pane.")
        keys.addKeyBinding("q", "zoom in")
        keys.addKeyBinding("a", "zoom out")
        keys.addKeyBinding("arrow-left", "move camera left")
        keys.addKeyBinding("arrow-right", "move camera right")
        keys.addKeyBinding("arrow-up", "move camera up")
        keys.addKeyBinding("arrow-down", "move camera down")
        keys.addKeyBinding(List("ctrl", "a"), "test combination")
        keys.addKeyBinding(List("ctrl", "mouse-left"), "test combination + mouse")

        val t = new Toast("dark", keys)
        toastManager.show(t)
        centerToast(t)
      }
    })

    val sampleKeysHelp = new MenuItem("Sample shortcuts", new ChangeListener {
      override def changed(event: ChangeEvent, actor: Actor): Unit = {
        val t = currSampleShortcuts match {
          case Some(keys) =>
            new Toast("dark", keys)
          case _ =>
            val tab = new VisTable()
            tab.add(new VisLabel("No provided keybindigs!"))
            new Toast("dark", tab)
        }
        toastManager.show(t)
        centerToast(t)
      }
    })

    helpMenu.addItem(testbedKeysHelp)
    helpMenu.addItem(sampleKeysHelp)
    menuBar.addMenu(fileMenu)
    menuBar.addMenu(helpMenu)

  }

  private def centerToast(toast: Toast) = {
    toast.getMainTable.setY(
      stage.getHeight - toast.getMainTable.getPrefHeight - toastManager.getScreenPadding)
    toast.getMainTable.setX((stage.getWidth / 2) - toast.getMainTable.getPrefWidth / 2 )
  }

  private def buildSamplesTree(): VisTree = {
    val tree = new VisTree

    /*categories*/
    val core = new Node(new VisLabel("Core"))
    val dummy = new Node(new VisLabel("Dummy"))
    val actors = new Node(new VisLabel("Actors"))
    val tests = new Node(new VisLabel("Test"))
    core.setExpanded(true)

    /*add samples by name*/
    addSample(dummy, "Dummy Circle")
    addSample(dummy, "Dummy Circle 2")
    addSample(core, "Entities Playground")
    addSample(core, "Box2d World")
    addSample(actors, "Actor System")
    addSample(tests, "Graph/Map Test")

    tree.add(core)
    tree.add(dummy)
    tree.add(actors)
    tree.add(tests)
    tree
  }

}

case class FutureTestbed() extends AbstractMainApplication with Testbed {

  private[this] var currentSample: Option[Sample] = None
  private[this] var nextSample: Option[Sample] = None
  private[this] var inputMultiplexer: InputMultiplexer = _
  private[this] var loadingFinished = true
  private[this] var currSampleName: String = _

  private[this] val gui = TestbedGui(new TestbedController {

    override def onSampleSelection(name: String): Unit = {
      currSampleName = name
      nextSample = matchSample(currSampleName)
    }

    override def onPause(flag: Boolean): Unit = pause = flag

  })

  private def matchSample(sampleName: String): Option[Sample] = sampleName match {
    case "Dummy Circle" => Option(DummyCircleSample())
    case "Entities Playground" => Option(new ScalaEntitySystemModule())
    case "Box2d World" => Option(new ScalaBox2dModule())
    case "Actor System" => Option(new ActorSystemModule())
    case "Graph/Map Test" => Option(new GraphMapTest())
    case _ => None
  }

  private def resetInputProcessor(sample: Sample): Unit = {
    inputMultiplexer.clear()
    sample.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
    gui.attachInputProcessor(inputMultiplexer)
    Gdx.input.setInputProcessor(inputMultiplexer)
  }

  private def setSample(sample: Sample): Unit = {
    if (loadingFinished) {
      loadingFinished = false

      gui.resetLoadingBar()
      gui.resetSamplePane()
      sample.init(this)
      gui.setProgress(15)

      resetInputProcessor(sample)
      gui.setProgress(25)

      sample.getKeyShortcuts.foreach(sc =>
        gui.setCurrentSampleShortcuts(sc))
      gui.setProgress(45)

      currentSample.foreach(old => old.cleanup())
      currentSample = Some(sample)
      nextSample = None
      gui.setProgress(65)

      new Thread(new Runnable {
        override def run(): Unit = {
          sample.setup((msg: String) => gui.setLoadingLogText(msg))
          loadingFinished = true
          gui.setProgress(85)

          sample.initGui(gui.getSamplePane)
          gui.setProgress(100)

          gui.displayModuleLoadedToast(currSampleName)
          gui.setLoadingLogText("")
        }
      }).start()
    }
  }

  override def create(): Unit = {
    super.create()
    gui.init()

    inputMultiplexer = new InputMultiplexer()
    inputMultiplexer.addProcessor(this)
    gui.attachInputProcessor(inputMultiplexer)

    currentSample.foreach(s => {
      s.init(this)
      s.attachInputProcessors(inputMultiplexer)
    })

    Gdx.input.setInputProcessor(inputMultiplexer)
  }

  override def doRender(): Unit = {
    if (loadingFinished) currentSample.foreach(s => s.render(shapeRenderer))
    gui.render()
  }

  override def doUpdate(delta: Float): Unit = {
    gui.update(delta)
    nextSample.foreach(s => setSample(s))
    if (loadingFinished) currentSample.foreach(s => s.update(delta))
  }

  override def resize(newWidth: Int, newHeight: Int): Unit = {
    super.resize(newWidth, newHeight)
    currentSample.foreach(s => s.resize(newWidth, newHeight))
  }

  override def dispose(): Unit = {
    super.dispose()
    VisUI.dispose()
  }

  override def keyUp(keycode: Int): Boolean = {
    super.keyUp(keycode)
    if (keycode == Keys.H) {
      gui.toggleSamplePane()
    }
    false
  }
}