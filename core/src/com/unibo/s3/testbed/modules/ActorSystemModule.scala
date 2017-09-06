package com.unibo.s3.testbed.modules

import java.io.File

import akka.actor.{ActorRef, Props, UntypedAbstractActor}
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.kotcrab.vis.ui.widget._
import com.unibo.s3.main_system.communication.{GraphActor, MapActor, QuadTreeActor, SystemManager}
import com.unibo.s3.main_system.world.actors.WorldActor
import com.unibo.s3.testbed.model.BaseTestbedModule

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

object Eval {

  def apply[A](string: String): A = {
    val toolbox = currentMirror.mkToolBox()
    val tree = toolbox.parse(string)
    toolbox.eval(tree).asInstanceOf[A]
  }

  def fromFile[A](file: File): A =
    apply(scala.io.Source.fromFile(file).mkString(""))

  def fromFileName[A](file: String): A =
    fromFile(new File(file))

}


case class LoggerActor(logger: {def setText(s: String)}) extends UntypedAbstractActor {

  override def onReceive(message: Any): Unit = {
    println(message)
    logger.setText(message.toString)
  }
}

object LoggerActor {
  def props(logger: {def setText(s: String)}): Props = Props(new LoggerActor(logger))
}

class ActorSystemModule extends BaseTestbedModule {

  private[this] var loggerActor: ActorRef = _
  private[this] var messageTextField: VisTextArea = _
  private[this] var responseTextField: VisTextArea = new VisTextArea("")

  override def initGui(window: VisWindow): Unit = {
    super.initGui(window)
    window.getTitleLabel.setText("Actor System")


    val selectBox = new VisSelectBox[String]()
    selectBox.setItems("mapActor", "worldActor",
      "graphActor", "quadTreeActor", "loggerActor")

    window.add[VisLabel](new VisLabel("Target actor: ")).expandX()
    window.add[VisSelectBox[String]](selectBox).expandX().row()

    window.add[VisLabel](new VisLabel("Message (Scala code):")).fillX().colspan(2).padTop(4).row()
    messageTextField = new VisTextArea("Write message instantiation here\n ex. new MyMessage(true, 2.2f)")
    window.add[VisTextField](messageTextField).fillX().height(200).colspan(2).padTop(4).row()

    val testSendBtn = new VisTextButton("test send", "blue")
    window.add[VisTextButton](testSendBtn).colspan(2).expandX().row()

    window.add[VisLabel](new VisLabel("Message response:")).fillX().colspan(2).padTop(8).row()
    window.add[VisTextField](responseTextField).fillX().height(200).colspan(2).padTop(4).row()

    window.add().fillY().expandY()

    testSendBtn.addListener(new ClickListener {
      override def clicked(event: InputEvent, x: Float, y: Float): Unit = {
        super.clicked(event, x, y)

        val src =
          """import com.badlogic.gdx.math.Vector2;
             import com.unibo.s3.main_system.world.actors._;
             import com.unibo.s3.main_system.communication.Messages._;
          """+messageTextField.getText

        val msg = Eval[Any](src)
        SystemManager.getInstance()
          .getLocalActor(selectBox.getSelected).tell(msg, loggerActor)
      }
    })
  }

  override def setup(f: (String) => Unit): Unit = {
    super.setup(f)
    f("Init actor system")
    SystemManager.getInstance.createSystem("System", null)

    f("Deploy -> WorldActor")
    SystemManager.getInstance().createActor(WorldActor.props(new World(new Vector2(0, 0), true)), "worldActor")

    f("Deploy -> MapActor")
    SystemManager.getInstance.createActor(MapActor.props(), "mapActor")

    f("Deploy -> GraphActor")
    SystemManager.getInstance.createActor(GraphActor.props(), "graphActor")

    f("Deploy -> ProximityActor")
    SystemManager.getInstance.createActor(QuadTreeActor.props(), "quadTreeActor")

    f("Deploy -> ProximityActor")
    SystemManager.getInstance.createActor(LoggerActor.props(responseTextField), "loggerActor")
    loggerActor = SystemManager.getInstance().getLocalActor("loggerActor")
  }
}

object ActorSystemModule {
  def apply(): ActorSystemModule = new ActorSystemModule()
}

