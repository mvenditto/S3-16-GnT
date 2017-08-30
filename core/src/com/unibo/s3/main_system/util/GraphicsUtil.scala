package com.unibo.s3.main_system.util

import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.Drawable

object GraphicsUtil {

  private def pixmapFromColor(
    width: Integer, height: Integer, color: Color) = {
    val pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888)
    pixmap.setColor(color)
    pixmap.fill()
    pixmap
  }

  def drawableFromColor(
    width: Integer, height: Integer, color: Color): Drawable = {
    new Image(textureFromColor(width, height, color)).getDrawable
  }

  def textureFromColor(width: Integer, height: Integer, color: Color): Texture = {
    val pixmap = pixmapFromColor(width, height, color)
    val texture = new Texture(pixmap)
    pixmap.dispose()
    texture
  }

}
