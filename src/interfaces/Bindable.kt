package interfaces

import BaseSketch
import controls.ControlTab
import kotlinx.serialization.KSerializer

interface KSerializable<T> {
  fun toSerializer(): KSerializer<T>
}

interface Copyable<T> {
  fun clone(): T
}

interface Bindable {
  /**
   * Just a helper so this can be called from outside of Bindable
   */
  fun bindSketch(s: BaseSketch): List<ControlTab> = s.bind()
  fun BaseSketch.bind(): List<ControlTab>
}

interface TabBindable : Bindable {
  override fun BaseSketch.bind(): List<ControlTab> = listOf(bindTab())
  fun BaseSketch.bindTab(): ControlTab
}
