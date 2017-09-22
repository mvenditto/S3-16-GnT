package com.unibo.s3.testbed.model

import scala.util.parsing.json.JSON

/**
  * This object contains the method to parse the information
  * about available [[TestbedModule]]s, as defined in 'modules.json' file.
  *
  * @author mvenditto
  */
object ModulesMetadataParser {

  private[this] val clazz = "class"
  private[this] val desc = "desc"
  private[this] val version = "version"

  type ModulesMap = Map[String, Map[String, Map[String, String]]]

  private def unwrapToModuleMetadata(
    d: (String, String, Map[String, String])): ModuleMetadata = d match {
    case (category: String, name: String, metadata: Map[String, String]) =>
      ModuleMetadata(name, metadata.get(desc),
        metadata.get(clazz), metadata.get(version), category)
  }

  private def parse(m: ModulesMap): Iterable[ModuleMetadata] = {
    m.keys.flatMap(c => m(c).map(mm => (c, mm._1, mm._2)))
      .map(unwrapToModuleMetadata)
  }

  /**
    * Parses the modulesJson (default: 'modules.json') file
    * and outputs the [[ModuleMetadata]] for each found module.
    * @param modulesJson
    * @return an [[Option]] [[Iterable]] of [[ModuleMetadata]]
    */
  def getModulesMetadata(modulesJson: String): Option[Iterable[ModuleMetadata]] = {
    val modulesMap = JSON.parseFull(modulesJson)
    modulesMap match {
      case Some(m: ModulesMap) => Option(parse(m))
      case _ => None
    }
  }
}
