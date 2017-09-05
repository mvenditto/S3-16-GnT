package com.unibo.s3.main_system.world

import com.badlogic.gdx.math.Vector2

trait BodyType
case object Wall extends BodyType
case object Hideout extends BodyType

case class BodyData (
  userData: Object,
  size: Vector2,
  bodyType: Option[BodyType] = None
)
