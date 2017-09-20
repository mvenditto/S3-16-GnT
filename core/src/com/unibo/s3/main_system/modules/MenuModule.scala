package com.unibo.s3.main_system.modules

import com.badlogic.gdx.{Gdx, InputMultiplexer}
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.{Actor, InputEvent}
import com.badlogic.gdx.scenes.scene2d.ui.{ButtonGroup, Label}
import com.badlogic.gdx.scenes.scene2d.utils.{ChangeListener, ClickListener}
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.util.ToastManager
import com.kotcrab.vis.ui.widget._
import com.kotcrab.vis.ui.widget.toast.Toast
import com.unibo.s3.Main
import com.unibo.s3.testbed.ui.{AdaptiveSizeActor, Anchorable, TopLeft}
import com.unibo.s3.testbed.ui.KeyHelpTable

sealed trait MenuEvent
case class Start(guardsNum: Int, thiefsNum: Int, simulation: Boolean, mapDimension: Vector2, mazeTypeMap: Boolean) extends MenuEvent
case class Pause(pause: Boolean) extends MenuEvent
case class ViewDebug(debug: Boolean) extends MenuEvent

class MenuModule(listener: MenuEvent => Unit) extends BasicModuleWithGui{
  private var enabled = true
  private var windowSettings, windowMenu: VisWindow = _
  private var menuWindowPane: AdaptiveSizeActor with Anchorable = _
  private var guardsNum = 5
  private var thiefsNum = 1
  private var simulation = true
  private var dimensionMap: Vector2 = _
  private var mazeMap = true

  private var pause = false
  private var viewDebug = false

  override def init(owner: Main): Unit = {
    super.init(owner)
    //this.world = new World(new Vector2(0,0), true);
    initMenuGUI()
    initSettingsGUI()
  }

  private def initSettingsGUI() = {
    windowSettings = new VisWindow("Settings")
    //windowSettings.setDebug(true);
    windowSettings.setMovable(false)
    val title = windowSettings.getTitleLabel
    title.setColor(Color.GREEN)
    title.setAlignment(Align.center)

    //windowSettings.row
    val guardsNumS = new VisSlider(2f, 20f, 1f, false)
    guardsNumS.setValue(this.guardsNum)
    val labGuardsNum = new VisLabel(" " + "%02d".format(guardsNum))
    windowSettings.add(new VisLabel("Guards number: ")).row()
    val tableGuardum = new VisTable
    tableGuardum.add(guardsNumS).padLeft(10)
    tableGuardum.add(labGuardsNum).padRight(10).padLeft(5)
    windowSettings.add(tableGuardum).row()
    guardsNumS.addListener(new ChangeListener() {
      override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
        guardsNum = guardsNumS.getValue.toInt
        labGuardsNum.setText(" " + "%02d".format(guardsNum))
      }
    })

    val thiefsNumS = new VisSlider(1f, 20f, 1f, false)
    thiefsNumS.setValue(this.thiefsNum)
    val labThiefsNum = new VisLabel(" " + "%02d".format(thiefsNum))
    windowSettings.add(new VisLabel("Thiefs number: ")).padTop(10).row()
    val tableThiefsNum = new VisTable
    tableThiefsNum.add(thiefsNumS)
    tableThiefsNum.add(labThiefsNum)
    windowSettings.add(tableThiefsNum).row()
    thiefsNumS.addListener(new ChangeListener() {
      override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
        thiefsNum = thiefsNumS.getValue.toInt
        labThiefsNum.setText(" " + "%02d".format(thiefsNum))
      }
    })

    val tableThiefType = new VisTable
    val group = new ButtonGroup[VisRadioButton]
    val simThiefRB = new VisRadioButton("Simulated")
    group.add(simThiefRB)
    val pilThiefRB = new VisRadioButton("Piloted")
    group.add(pilThiefRB)
    tableThiefType.add(new VisLabel("Thief type: "))
    tableThiefType.add(simThiefRB).padRight(5)
    tableThiefType.add(pilThiefRB).padLeft(5)
    windowSettings.add(tableThiefType).padTop(10).padLeft(5).padRight(5).row()
    simThiefRB.addListener(new ChangeListener() {
      override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
        simulation = simThiefRB.isChecked
      }
    })

    //60x60, 80x60
    val tableMapDimension = new VisTable
    val numDimension = 2
    val dimensionS = new Array[String](numDimension)
    val dimensionI = new Array[Vector2](numDimension)
    var index = 0
    dimensionI.add(new Vector2(60,60))
    dimensionS.add(dimensionI.get(index).x.toInt + "x" + dimensionI.get(index).y.toInt)
    dimensionMap = dimensionI.get(index)
    index += 1
    dimensionI.add(new Vector2(80,60))
    dimensionS.add(dimensionI.get(index).x.toInt + "x" + dimensionI.get(index).y.toInt)
    val mapDimensionSB = new VisSelectBox[String]
    mapDimensionSB.setItems(dimensionS)
    tableMapDimension.add(new VisLabel("Map dimensions: "))
    tableMapDimension.add(mapDimensionSB)
    windowSettings.add(tableMapDimension).padTop(10).row()
    mapDimensionSB.addListener(new ChangeListener() {
      override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
        val indexSelected = mapDimensionSB.getSelectedIndex
        //System.out.println(dimensionI(indexSelected)(0) + "x" + dimensionI(indexSelected)(1))
        dimensionMap = dimensionI.get(indexSelected)
      }
    })

    //labirinto: maze - stanze: rooms
    val tableMapType = new VisTable
    val groupMaps = new ButtonGroup[VisRadioButton]
    val mazeCheck = new VisRadioButton("Maze")
    groupMaps.add(mazeCheck)
    val roomCheck = new VisRadioButton("Rooms")
    groupMaps.add(roomCheck)
    tableMapType.add(new VisLabel("Map type: "))
    tableMapType.add(mazeCheck).padRight(5)
    tableMapType.add(roomCheck).padLeft(5)
    windowSettings.add(tableMapType).padTop(10).row()
    mazeCheck.addListener(new ChangeListener() {
      override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
        mazeMap = mazeCheck.isChecked
      }
    })

    val okButton = new VisTextButton("Start!", "blue")
    windowSettings.add(okButton).padTop(10).padBottom(10).row()
    okButton.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        listener(Start(guardsNum, thiefsNum, simulation, dimensionMap, mazeMap))
        windowSettings.fadeOut(1f)
        windowMenu.setVisible(true)
        windowMenu.fadeIn(1f)
      }
    })
    windowSettings.pack()
    gui.addActor(windowSettings)
    windowSettings.centerWindow
    //windowSettings.setPosition((Gdx.graphics.getWidth()/2)-(windowSettings.getWidth()/2), (Gdx.graphics.getHeight()/2)-(windowSettings.getHeight()/2));
  }

  private def initMenuGUI() = {
    windowMenu = new VisWindow("Menu")
    windowMenu.setMovable(false)
    windowMenu.setVisible(false)
    val title = windowMenu.getTitleLabel
    title.setColor(Color.GREEN)
    title.setAlignment(Align.center)

    val pauseString = "Pause"
    val startString = "Resume"
    val buttonPause = new VisTextButton(pauseString)
    windowMenu.add(buttonPause).padTop(10).row()
    buttonPause.addListener(new ClickListener() {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        if (pause) buttonPause.setText(pauseString)
        else buttonPause.setText(startString)
        pause = !pause
        listener(Pause(pause))
      }
    })

    val debugView = new VisCheckBox(" View debug")
    windowMenu.add(debugView).padTop(10).padLeft(10).padRight(10).row()
    debugView.addListener(new ChangeListener {
      override def changed(event: ChangeListener.ChangeEvent, actor: Actor): Unit = {
        if(debugView.isChecked) viewDebug = true
        else viewDebug = false
        listener(ViewDebug(viewDebug))
      }
    })

    val shortButton = new VisTextButton("Shortcut")
    windowMenu.add(shortButton).padTop(10).padBottom(10)
    shortButton.addListener(new ClickListener() {

      def centerToast(toast: Toast, toastManager: ToastManager): Unit = {
        toast.getMainTable.setY(
          gui.getHeight - toast.getMainTable.getPrefHeight - toastManager.getScreenPadding)
        toast.getMainTable.setX((gui.getWidth / 2) - toast.getMainTable.getPrefWidth / 2 )
      }

      def openShortcut(): Unit = {
        val toastManager = new ToastManager(gui)
        toastManager.setAlignment(Align.center)

        val keys = new KeyHelpTable(true)
        keys.addKeyBinding("p", "pause")
        keys.addKeyBinding("q", "zoom in")
        keys.addKeyBinding("r", "zoom out")
        keys.addKeyBinding("arrow-left", "move camera left")
        keys.addKeyBinding("arrow-right", "move camera right")
        keys.addKeyBinding("arrow-up", "move camera up")
        keys.addKeyBinding("arrow-down", "move camera down")
        keys.addKeyBinding("mouse-wheel-move", "brightness control")
        keys.addKeyBinding("mouse-left", "spawn guard (only debug view)")
        keys.addKeyBinding("t", "spawn thief (only debug view)")
        keys.align(Align.center)

        val t = new Toast("dark", keys)
        toastManager.show(t)
        centerToast(t, toastManager)
      }

      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        openShortcut()
      }
    })

    gui.addActor(windowMenu)
    windowMenu.pack()
    //windowMenu.setPosition(50, Gdx.graphics.getHeight - 200)

    menuWindowPane = new AdaptiveSizeActor(windowMenu) with Anchorable
    menuWindowPane.setSize(10, 20) //10 % 20 % of stage size
    menuWindowPane.setPadding(10,0)
    menuWindowPane.setAnchor(TopLeft)
  }

  override def resize(newWidth: Int, newHeight: Int): Unit = {
    super.resize(newWidth, newHeight)
    menuWindowPane.resize(newWidth, newHeight)
  }

  override def attachInputProcessors(inputMultiplexer: InputMultiplexer): Unit = {
    super.attachInputProcessors(inputMultiplexer)
    inputMultiplexer.addProcessor(this)
  }

  override def enable(enabled: Boolean): Unit = {
    this.enabled = enabled
  }

  override def isEnabled: Boolean = this.enabled
}
