package com.unibo.s3.testbed.model

/**
  * Wraps the information about a [[TestbedModule]].
  * @param name the module name
  * @param desc an optional module description
  * @param clazz the name of this module class
  * @param version an optional indication on this module version
  * @param category the category of this module
  *
  * @author mvenditto
  */
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
