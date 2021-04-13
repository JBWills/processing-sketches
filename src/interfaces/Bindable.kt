package interfaces

import BaseSketch
import controls.panels.ControlTab
import kotlinx.serialization.KSerializer

fun interface KSerializable<T> {
  fun toSerializer(): KSerializer<T>
}

fun interface Copyable<T> {
  fun clone(): T
}

interface Bindable {
  /**
   * Just a helper so this can be called from outside of Bindable
   */
  fun bindSketch(s: BaseSketch): List<ControlTab> = s.bind()
  fun BaseSketch.bind(): List<ControlTab>
}
