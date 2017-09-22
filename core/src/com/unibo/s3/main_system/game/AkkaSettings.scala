package com.unibo.s3.main_system.game

object AkkaSystemNames {
  val GUISystem: String = "GUISystem"
  val ComputeSystem: String = "ComputeSystem"
}

sealed trait Ports {
  def portNumber: Int
}

object  GUISystemPort extends Ports {
  override val portNumber = 5050
}

object ComputeSystemPort extends Ports {
  override val portNumber = 2727
}

