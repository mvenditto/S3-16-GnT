package com.unibo.s3.testbed.ui

import java.text.SimpleDateFormat
import java.util.Date

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.kotcrab.vis.ui.widget.{VisLabel, VisTable}


case class LogMessage(tag: String, msg: String, color: Color) {
  val timestamp: Date = new Date()
}


class Console extends VisTable {

  private val topPad = 6f
  private var logsNum = 0
  private val sep = " - "
  private val space = " " * 4
  private val sqBracketOpen = "["
  private val sqBracketClose = "]"
  private val maxLogs = 100
  private val dateFormatter = new SimpleDateFormat("hh:mm:ss")
  private var logs = List[LogMessage]()

  private def logLimitReached(): Boolean = {
    logs.size + 1 > maxLogs
  }

  def addLog(log: LogMessage): Unit = {
    if (!logLimitReached()) logs :+= log
    rebuild()
  }

  def rebuild(): Unit = {
    safe(_ => {
      clear()
      for (i <- logs.indices) {
        addLogWithIdx(logs(i), i)
      }
    })
  }

  /* post on rendering thread, executed at next update.*/
  private def safe(f: Any => Any) {
    Gdx.app.postRunnable(new Runnable {
      override def run(): Unit = {
        f()
      }
    })
  }

  private def addLogWithIdx(log: LogMessage, idx: Integer): Unit = {
    val now = dateFormatter.format(log.timestamp)
    val date = new VisLabel(idx + sep + now + sep)
    val msg = new VisLabel(space + log.msg)

    val tag = new VisLabel(sqBracketOpen + log.tag + sqBracketClose)
    tag.setColor(log.color)

    val tmp = new VisTable()
    tmp.add(date)
    tmp.add(tag)

    add[VisTable](tmp).width(getWidth / 4f).padTop(topPad)
    add[VisLabel](msg).width(getWidth * 3/4f).colspan(1)
      .padTop(topPad).row()
  }
}
