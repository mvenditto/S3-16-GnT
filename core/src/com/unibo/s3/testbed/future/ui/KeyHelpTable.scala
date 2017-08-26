package com.unibo.s3.testbed.future.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.kotcrab.vis.ui.widget.{VisImage, VisLabel, VisTable}

class KeyHelpTable(
  defaults: Boolean,
  icoSize: Float = 48) extends VisTable(defaults) {

  private[this] val iconsPath = "icons/keys/"
  private[this] val png = ".png"

  private def loadKeyIcon(key: String): VisImage = {
    val texture = new Texture(Gdx.files.internal(iconsPath + key + png), true)
    texture.setFilter(TextureFilter.Linear,
      TextureFilter.Linear)
    new VisImage(texture)
  }

  def addKeyBinding(key: String, desc: String): Unit = {
    add(loadKeyIcon(key)).size(icoSize, icoSize).expandX()
    add(new VisLabel(desc)).expandX().row()
  }

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
