package com.unibo.s3.testbed

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.utils.{ChangeListener, ClickListener}
import com.badlogic.gdx.scenes.scene2d.{Actor, Group, InputEvent, Stage}
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.{Gdx, InputMultiplexer}
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.util.ToastManager
import com.kotcrab.vis.ui.widget.toast.Toast
import com.kotcrab.vis.ui.widget.{VisLabel, VisTree, _}
import com.unibo.s3.InputProcessorAdapter
import com.unibo.s3.main_system.AbstractMainApplication
import com.unibo.s3.main_system.util.GraphicsUtils
import com.unibo.s3.testbed.samples._
import com.unibo.s3.testbed.ui.{AdaptiveSizeActor, Anchorable, Console, FpsCounter, KeyHelpTable, LogMessage, Toggleable, TopLeft, TopRight, TransitionFunctions}

import scala.util.{Failure, Success, Try}
import scala.util.parsing.json.JSON

trait Testbed {

  def screenToWorld(screenPosition: Vector2): Vector2

  def worldToScreen(worldPosition: Vector2): Vector2

  def getCamera: OrthographicCamera

  def getLogger: (LogMessage => Unit)
}

trait TestbedListener {

  def onSampleSelection(metadata: ModuleMetadata): Unit

  def onPause(pause: Boolean): Unit

}

case class TestbedView(listener: TestbedListener) {

  type AnchorableToggleableActor =
    AdaptiveSizeActor with Anchorable with Toggleable

  private var stage: Stage = _
  private var menuBar: MenuBar = _
  private var loadingBar: VisProgressBar = _
  private var loadingLog: VisLabel = _
  private var samplePane: AnchorableToggleableActor = _
  private var eastPane: AnchorableToggleableActor = _
  private var console: Console = _
  private var consolePane: AdaptiveSizeActor with Toggleable = _
  private var toastManager: ToastManager = _
  private var fpsCounter: AdaptiveSizeActor with Anchorable = _
  private var currSampleShortcuts: Option[KeyHelpTable] = None
  private var tree: VisTree = _

  private val consolePadding = 50f
  private val customBlue = new Color(0f, 0.7f, 1f, 1f)


  def resize(newWidth: Integer, newHeight: Integer): Unit = {
    stage.getViewport.update(newWidth, newHeight, true)

    List(consolePane, eastPane, samplePane, fpsCounter).foreach(w =>
      w.resize(newWidth.toFloat, newHeight.toFloat)
    )

    console.setSize(stage.getWidth - consolePadding * 2, stage.getHeight / 4.5f)
    console.rebuild()
  }

  def init(): Unit = {
    stage = new Stage(new ScreenViewport())
    VisUI.load()
    toastManager = new ToastManager(stage)
    toastManager.setAlignment(Align.bottomRight)

    val _eastPane = new VisWindow("")

    _eastPane.getTitleLabel.setText("Testbed menu")
    _eastPane.getTitleLabel.setColor(customBlue)

    val simulationLabel = new VisLabel("Simulation")
    simulationLabel.setColor(Color.LIGHT_GRAY)
    _eastPane.add(simulationLabel).fillX().expandX().row()

    val pauseCheckbox = new VisCheckBox("update")
    pauseCheckbox.addListener(new ChangeListener {
      override def changed(event: ChangeEvent, actor: Actor): Unit =
        listener.onPause(!pauseCheckbox.isChecked)
    })

    _eastPane.add(pauseCheckbox).row()
    pauseCheckbox.setChecked(true)

    val samplesLabel = new VisLabel("Samples")
    samplesLabel.setColor(Color.LIGHT_GRAY)

    tree = new VisTree()
    _eastPane.add(samplesLabel).fillX().expandX().row()
    _eastPane.add(tree).expand().fill().row()
    _eastPane.setMovable(false)

    loadingLog = new VisLabel("")
    loadingLog.setColor(customBlue)
    _eastPane.add(loadingLog).fillX().expandX().row()

    loadingBar = new VisProgressBar(0f, 100f, 1f, false)
    loadingBar.setAnimateDuration(2)
    _eastPane.add(loadingBar).fillX().expandX()

    eastPane = new AdaptiveSizeActor(_eastPane) with Anchorable with Toggleable
    eastPane.setAnchor(TopRight)

    val sp =  new VisWindow("")
    sp.getTitleLabel.setColor(customBlue)
    sp.setKeepWithinParent(false)
    sp.setKeepWithinStage(false)

    samplePane = new AdaptiveSizeActor(sp) with Toggleable with Anchorable
    samplePane.setSize(20, 100)
    samplePane.setAnchor(TopLeft)
    samplePane.setTransitionFunc(TransitionFunctions.slideLeft)

    menuBar = new MenuBar()
    createMenu()

    console = new Console()
    val ss = new VisScrollPane(console)
    consolePane = new AdaptiveSizeActor(ss) with Toggleable
    consolePane.setTransitionFunc(TransitionFunctions.slideDown)
    val tmp = Color.BLACK.cpy()
    tmp.a = 0.5f
    val bg = GraphicsUtils.drawableFromColor(stage.getWidth.toInt,
      (stage.getHeight / 4.5f).toInt, tmp)
    ss.getStyle.background = bg

    val _fps = new FpsCounter()
    _fps.setSize(64, 48)
    fpsCounter = new AdaptiveSizeActor(_fps) with Anchorable
    fpsCounter.setAnchor(TopRight)

    val root = new VisTable()
    root.setFillParent(true)
    root.add(menuBar.getTable).fillX().expandX().row()
    root.add().fillY().expandY()
    resize(Gdx.graphics.getWidth, Gdx.graphics.getHeight)

    val hud = new Group()
    hud.addActor(sp)
    hud.addActor(_eastPane)

    stage.addActor(hud)
    stage.addActor(ss)
    stage.addActor(_fps)
    stage.addActor(root)

    val padY = menuBar.getTable.getPrefHeight
    val yPerc = 100 - (100 * padY) / stage.getHeight
    List(eastPane, samplePane, fpsCounter)
      .foreach(w => w.setPadding(0, -padY.toInt))
    consolePane.setSize(100, 20)
    samplePane.setSize(25, yPerc)
    eastPane.setSize(20, yPerc)
    consolePane.toggle(stage.getWidth, stage.getHeight)
  }

  def addSampleEntry(node: Node, sample: ModuleMetadata): Unit = {
    val sampleNameLbl = new VisLabel(sample.name)

    if (sample.clazz.isEmpty) {
      sampleNameLbl.setColor(Color.RED)
      new Tooltip.Builder("Wrong/Undefined 'class' attribute!")
        .target(sampleNameLbl).build()
    } else {
      sampleNameLbl.addListener(new ClickListener() {
        override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
          super.clicked(event, x, y)
          listener.onSampleSelection(sample)
        }
      })
    }
    node.add(new Node(sampleNameLbl))
  }

  def setProgressBarValue(value: Float): Unit = loadingBar.setValue(value)

  def setLoadingLogText(msg: String): Unit = loadingLog.setText(msg)

  def writeConsoleLog(log: LogMessage): Unit = console.addLog(log)

  def resetProgressBar(): Unit = {
    loadingBar.setAnimateDuration(0)
    loadingBar.setValue(0)
    loadingBar.setAnimateDuration(0.2f)
  }

  def resetSamplePane(): Unit = samplePane.getActor[VisWindow].clear()

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

  def getSamplePane: VisWindow = samplePane.getActor[VisWindow]

  def render(): Unit = stage.draw()

  def update(dt: Float): Unit = {
    stage.act(dt)
  }

  def toggleSamplePane(): Unit = {
    samplePane.toggle(stage.getWidth, stage.getHeight)
  }

  def toggleConsole(): Unit = {
    consolePane.toggle(stage.getWidth, stage.getHeight)
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

  def buildSamplesTree(modules: Iterable[ModuleMetadata]): Unit = {
    var categories = Map[String, Node]()

    modules.foreach(m => {
      val c = m.category
      if(!categories.contains(c)) {
        val n = new Node(new VisLabel(c))
        categories += (c -> n)
        addSampleEntry(n, m)
      } else {
        addSampleEntry(categories(c),m)
      }
    })
    categories.values.foreach(n => tree.add(n))
  }
}

case class FutureTestbed() extends AbstractMainApplication with Testbed {

  private[this] var currentSample: Option[Sample] = None
  private[this] var nextSample: Option[Sample] = None
  private[this] var inputMultiplexer: InputMultiplexer = _
  private[this] var loadingFinished = true
  private[this] var currSampleName: String = _

  private[this] val testbedPackage: String = "com.unibo.s3.testbed.samples."
  private[this] val testbedModulesFile: String = "testbed/modules.json"

  private[this] val gui = TestbedView(new TestbedListener {
    override def onSampleSelection(metadata: ModuleMetadata): Unit = {
      currSampleName = metadata.name
      loadSample(metadata.clazz.get).foreach(s => setSample(s))
    }

    override def onPause(flag: Boolean): Unit = {
      pause = flag
    }
  })

  private def loadSample(sampleClass: String): Option[Sample] = {
    Option(Class.forName(sampleClass).newInstance().asInstanceOf[Sample])
  }

  private def checkModuleClassExits(clazz: String): Option[String] = {
    Try(Class.forName(testbedPackage + clazz)) match {
      case Success(c) => Some(c.getName)
      case Failure(_) => None
    }
  }

  private def parseModulesFile(): Option[Iterable[ModuleMetadata]] = {
    val modulesJson = Gdx.files.internal(testbedModulesFile).readString()
    val modulesMap = JSON.parseFull(modulesJson)

    modulesMap match {
      case Some(m: Map[String, Map[String, Map[String, String]]]) =>
        val modules =
          m.keys.flatMap(cat =>
            m(cat).map(mm => (cat, mm._1, mm._2)))
          .map(md => {
            val meta = md._3
            var clazz = meta.get("class")
            if (clazz.isDefined) clazz = checkModuleClassExits(clazz.get)
            ModuleMetadata(md._2, meta.get("desc"), clazz, meta.get("version"), md._1)
          })
        Some(modules)
      case _ => None
    }
  }

  private def resetInputProcessor(sample: Sample): Unit = {
    val this_ = this
    Gdx.app.postRunnable(new Runnable {
      override def run(): Unit = {
        inputMultiplexer.clear()
        sample.attachInputProcessors(inputMultiplexer)
        inputMultiplexer.addProcessor(this_)
        gui.attachInputProcessor(inputMultiplexer)
        Gdx.input.setInputProcessor(inputMultiplexer)
      }
    })
  }

  private def setSample(sample: Sample): Unit = {
    if (loadingFinished) {
      loadingFinished = false

      gui.resetProgressBar()
      gui.resetSamplePane()
      sample.init(this)
      gui.setProgressBarValue(15)

      resetInputProcessor(sample)
      gui.setProgressBarValue(25)

      sample.getKeyShortcuts.foreach(sc =>
        gui.setCurrentSampleShortcuts(sc))
      gui.setProgressBarValue(45)

      currentSample.foreach(old => old.cleanup())
      currentSample = Some(sample)
      nextSample = None
      gui.setProgressBarValue(65)

      new Thread(new Runnable {
        override def run(): Unit = {
          sample.setup((msg: String) => {
            gui.setLoadingLogText(msg)
            gui.writeConsoleLog(LogMessage(currSampleName, msg, Color.GOLD))
          })
          gui.setProgressBarValue(85)

          sample.initGui(gui.getSamplePane)
          gui.setProgressBarValue(100)

          gui.displayModuleLoadedToast(currSampleName)
          gui.setLoadingLogText("")
          loadingFinished = true
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
    pause = false
    gui.writeConsoleLog(LogMessage("Testbed", "*** Welcome ***", Color.CYAN))
    gui.writeConsoleLog(LogMessage("Testbed", "Version : 0.9fut", Color.CYAN))

    parseModulesFile().foreach(modules => gui.buildSamplesTree(modules))
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
    gui.resize(newWidth, newHeight)
    currentSample.foreach(s => s.resize(newWidth, newHeight))
  }

  override def dispose(): Unit = {
    super.dispose()
    VisUI.dispose()
  }

  override def keyUp(keycode: Int): Boolean = {
    keycode match {
      case Keys.V => gui.toggleConsole()
      case Keys.H => gui.toggleSamplePane()
      case _ => ()
    }
    false
  }

  override def getCamera: OrthographicCamera = cam

  override def getLogger: (LogMessage) => Unit = gui.writeConsoleLog

}