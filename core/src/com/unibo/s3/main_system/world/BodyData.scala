package com.unibo.s3.main_system.world

/**
  * Describes a body type.
  */
trait BodyType
case object Wall extends BodyType
case object Hideout extends BodyType
case object Exit extends BodyType

/**
  * Contains information to be attached to a map's entity.
  * @param userData optional user custom data
  * @param bodyType optional [[BodyType]]
  *
  * @author mvenditto
  */
case class BodyData (
  var userData: Option[Any] = None,
  var bodyType: Option[BodyType] = None
)
