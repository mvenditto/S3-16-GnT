package com.unibo.s3.testbed.model

/**
  * This is a trait that models a [[TestbedModule]](s) loader.
  * A [[DynamicModuleLoader]] provides methods for checking the
  * existence of a module class and eventually creating a new instance of it.
  *
  * @author mvenditto
  */
trait DynamicModuleLoader {

  /**
    * Check if the given class exist at the location at which
    * [[TestbedModule]] classes are stored.
    * @param className the class name
    * @return true if the class exists false otherwise
    */
  def moduleClassExists(className: String): Boolean

  /**
    * Create a new instance of a [[TestbedModule]].
    * @param className the class name
    * @return an [[Option]] instance of the given [[TestbedModule]]
    */
  def newModuleInstance(className: String): Option[TestbedModule]

}

