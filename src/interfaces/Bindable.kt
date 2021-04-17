package interfaces

import controls.panels.ControlTab
import kotlinx.serialization.KSerializer

fun interface KSerializable<T> {
  fun toSerializer(): KSerializer<T>
}

fun interface Copyable<T> {
  fun clone(): T
}

interface Bindable {
  fun bind(): List<ControlTab>
}
