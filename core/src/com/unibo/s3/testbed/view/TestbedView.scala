package com.unibo.s3.testbed.view

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent
import com.badlogic.gdx.scenes.scene2d.utils.{ChangeListener, ClickListener}
import com.badlogic.gdx.scenes.scene2d.{Actor, Group, InputEvent, Stage}
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.{Gdx, InputMultiplexer}
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.util.ToastManager
import com.kotcrab.vis.ui.widget._
import com.kotcrab.vis.ui.widget.toast.Toast
import com.unibo.s3.main_system.util.GraphicsUtils
import com.unibo.s3.testbed.model.ModuleMetadata
import com.unibo.s3.testbed.ui._

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
  private val errorRed = new Color(1f, 0.41f, 0.38f, 1f)

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
    eastPane.setSize(25, yPerc)
    consolePane.toggle(stage.getWidth, stage.getHeight)
  }

  def addSampleEntry(node: Node, sample: ModuleMetadata, valid: Boolean): Unit = {
    val sampleNameLbl = new VisLabel(sample.name)

    if (!valid) {
      sampleNameLbl.setColor(errorRed)
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

  def buildSamplesTree(modules: Iterable[(ModuleMetadata, Boolean)]): Unit = {
    var categories = Map[String, Node]()

    modules.foreach(m => {
      val c = m._1.category
      if(!categories.contains(c)) {
        val n = new Node(new VisLabel(c))
        categories += (c -> n)
        addSampleEntry(n, m._1, m._2)
      } else {
        addSampleEntry(categories(c), m._1, m._2)
      }
    })
    categories.values.foreach(n => tree.add(n))
  }
}
