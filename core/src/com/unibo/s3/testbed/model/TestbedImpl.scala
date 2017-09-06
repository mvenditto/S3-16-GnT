package com.unibo.s3.testbed.model

import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.{Color, OrthographicCamera}
import com.badlogic.gdx.{Gdx, InputMultiplexer}
import com.kotcrab.vis.ui.VisUI
import com.unibo.s3.main_system.AbstractMainApplication
import com.unibo.s3.testbed.ui.LogMessage
import com.unibo.s3.testbed.view.{TestbedListener, TestbedView}

case class TestbedImpl() extends AbstractMainApplication with Testbed {

  private[this] var currentSample: Option[TestbedModule] = None
  private[this] var nextSample: Option[TestbedModule] = None
  private[this] var inputMultiplexer: InputMultiplexer = _
  private[this] var loadingFinished = true
  private[this] var currSampleName: String = _

  private[this] val testbedPackage: String = "com.unibo.s3.testbed.modules."
  private[this] val testbedModulesFile: String = "testbed/modules.json"
  private[this] val moduleLoader = DynamicModuleLoaderImpl(testbedPackage)

  private[this] val gui = TestbedView(new TestbedListener {
    override def onSampleSelection(metadata: ModuleMetadata): Unit = {
      currSampleName = metadata.name
      moduleLoader.newModuleInstance(metadata.clazz.get).foreach(s => setSample(s))
    }
    override def onPause(flag: Boolean): Unit = {
      pause = flag
    }
  })

  private def parseModulesFile(): Option[Iterable[ModuleMetadata]] = {
    val modulesJson = Gdx.files.internal(testbedModulesFile).readString()
    ModulesMetadataParser.getModulesMetadata(modulesJson)
  }

  private def resetInputProcessor(sample: TestbedModule): Unit = {
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

  private def setSample(sample: TestbedModule): Unit = {
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

    parseModulesFile().foreach(modules =>
      gui.buildSamplesTree(
        modules.map(m => (m, isMetadataValid(m)))))
  }

  private def isMetadataValid(md: ModuleMetadata): Boolean = {
    md.clazz.isDefined && moduleLoader.moduleClassExists(md.clazz.get)
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
