package com.unibo.s3.testbed.model

trait DynamicModuleLoader {

  def moduleClassExists(className: String): Boolean

  def newModuleInstance(className: String): Option[TestbedModule]

}

