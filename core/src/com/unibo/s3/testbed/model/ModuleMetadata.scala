package com.unibo.s3.testbed.model

class ModuleMetadata(
  val name: String,
  val desc: Option[String],
  val clazz: Option[String],
  val version: Option[String],
  val category: String
)

object ModuleMetadata {
  def apply(
    name: String,
    desc: Option[String],
    clazz: Option[String],
    version: Option[String],
    category: String): ModuleMetadata =
    new ModuleMetadata(name, desc, clazz, version, category)
}
