package com.unibo.s3.testbed.model

import scala.util.{Failure, Success, Try}

class DynamicModuleLoaderImpl(val modulePackage: String) extends DynamicModuleLoader {

  private def safeClassForName(className: String): Try[Class[_]] =
    Try(Class.forName(modulePackage + className))

  override def newModuleInstance(className: String): Option[TestbedModule] =
    safeClassForName(className) match {
      case Success(m) => Option(m.newInstance().asInstanceOf[TestbedModule])
      case Failure(_) => None
    }

  override def moduleClassExists(className: String): Boolean = {
    safeClassForName(className) match {
      case Success(_) => true
      case Failure(_) => false
    }
  }
}

object DynamicModuleLoaderImpl {
  def apply(modulePackage: String): DynamicModuleLoaderImpl =
    new DynamicModuleLoaderImpl(modulePackage)
}
