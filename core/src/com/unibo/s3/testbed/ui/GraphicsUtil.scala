package com.unibo.s3.testbed.ui

import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.Drawable

object GraphicsUtil {

  def drawableFromColor(
    width: Integer, height: Integer, color: Color): Drawable = {
    val pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888)
    pixmap.setColor(color)
    pixmap.fill()
    val drawable = new Image(new Texture(pixmap)).getDrawable
    pixmap.dispose()
    drawable
  }

}
