package com.unibo.s3.main_system.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.{Color, Pixmap, Texture}
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.kotcrab.vis.ui.widget.VisImage

/**
  * An utility class containing graphics-related utility functionality.
  *
  * @author mvenditto
  */
object GraphicsUtils {

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

  def loadIconAsImage(path: String,
    minMagFilter: (TextureFilter, TextureFilter) = (TextureFilter.Linear, TextureFilter.Linear)): VisImage = {
    val texture = new Texture(Gdx.files.internal(path), true)
    texture.setFilter(TextureFilter.Linear,
      TextureFilter.Linear)
    new VisImage(texture)
  }

  def createBitmapFromTtf(pathToFont: String, size: Int): BitmapFont = {
    val gen = new FreeTypeFontGenerator(Gdx.files.internal(pathToFont))
    val p = new FreeTypeFontParameter()
    p.size = size
    p.magFilter = TextureFilter.Linear
    p.minFilter = TextureFilter.Linear
    val sizedFont = gen.generateFont(p)
    gen.dispose()
    sizedFont
  }

}
