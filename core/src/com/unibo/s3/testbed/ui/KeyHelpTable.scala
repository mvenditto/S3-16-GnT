package com.unibo.s3.testbed.ui

import com.kotcrab.vis.ui.widget.{VisImage, VisLabel, VisTable}
import com.unibo.s3.main_system.util.GraphicsUtils

/**
  * A facility class to build a table showing 'keyboard shortcuts'
  * @see [[VisTable]]
  *
  *  @author mvenditto
  */
class KeyHelpTable(
  defaults: Boolean,
  icoSize: Float = 48) extends VisTable(defaults) {

  private[this] val iconsPath = "icons/keys/"
  private[this] val png = ".png"

  private def loadKeyIcon(key: String): VisImage =
    GraphicsUtils.loadIconAsImage(iconsPath + key + png)

  /**
    * Add a new shortcut to the table.
    * @param key the key (name must match keys as in 'assets/icons/keys/')
    * @param desc the description of the action triggered by this shortcut
    */
  def addKeyBinding(key: String, desc: String): Unit = {
    add(loadKeyIcon(key)).size(icoSize, icoSize).expandX()
    add(new VisLabel(desc)).expandX().row()
  }

  /**
    * Add a new key combination shortcut to the table.
    * @param keys the keys combination (names must match keys as in 'assets/icons/keys/')
    * @param desc the description of the action triggered by this shortcut
    */
  def addKeyBinding(keys: Seq[String], desc: String): Unit = {
    val t = new VisTable(true)
    keys.foreach(k => {
      val ico = loadKeyIcon(k)
      t.add(ico).size(icoSize, icoSize)
      if (!k.equals(keys.last)) t.add(new VisLabel("+")).colspan(2)
    })

    add(t).expandX()
    add(new VisLabel(desc)).expandX().row()
  }

}
