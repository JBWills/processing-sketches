package controls.panels.panelext.util

import kotlin.reflect.KMutableProperty0

interface RefWrapper<T, TWrap> {
  val backingRef: KMutableProperty0<T>

  val name get() = backingRef.name
  fun get(): TWrap
  fun set(newItem: TWrap)
}

fun <T> KMutableProperty0<T>.wrapSelf(): RefWrapper<T, T> = wrapped({ this }, { this })

fun <T, TWrap> KMutableProperty0<T>.wrapped(
  wrap: T.() -> TWrap,
  unwrap: TWrap.() -> T,
) = object : RefWrapper<T, TWrap> {
  override val backingRef: KMutableProperty0<T> = this@wrapped

  override fun get(): TWrap = this@wrapped.get().wrap()

  override fun set(newItem: TWrap) = this@wrapped.set(newItem.unwrap())
}
