package com.unibo.s3.main_system.world

trait BodyType
case object Wall extends BodyType
case object Hideout extends BodyType
case object Exit extends BodyType

case class BodyData (
  var userData: Option[Any] = None,
  var bodyType: Option[BodyType] = None
)
