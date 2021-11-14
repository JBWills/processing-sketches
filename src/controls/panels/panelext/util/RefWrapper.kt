package controls.panels.panelext.util

import kotlin.reflect.KMutableProperty0

interface RefGetter<T> {
  val name: String
  fun get(): T
  fun set(item: T)
}

interface RefWrapperList<T, TWrap> : RefGetter<TWrap> {
  val backingRefs: List<KMutableProperty0<T>>

  override val name get() = backingRefs.joinToString()
}

interface RefWrapper2<T, K, TWrap> : RefGetter<TWrap> {
  val backingRef1: KMutableProperty0<T>
  val backingRef2: KMutableProperty0<K>

  override val name get() = "${backingRef1.name}.${backingRef2.name}"
}

interface RefWrapper<T, TWrap> : RefGetter<TWrap> {
  val backingRef: KMutableProperty0<T>

  override val name get() = backingRef.name
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

fun <T, K, TWrap> Pair<KMutableProperty0<T>, KMutableProperty0<K>>.wrapped(
  wrap: (T, K) -> TWrap,
  unwrap: TWrap.() -> Pair<T, K>,
) = object : RefWrapper2<T, K, TWrap> {
  override val backingRef1: KMutableProperty0<T> = this@wrapped.first
  override val backingRef2: KMutableProperty0<K> = this@wrapped.second

  override fun get(): TWrap = wrap(this@wrapped.first.get(), this@wrapped.second.get())

  override fun set(newItem: TWrap) {
    newItem.unwrap().let { (newItem1, newItem2) ->
      this@wrapped.first.set(newItem1)
      this@wrapped.second.set(newItem2)
    }
  }
}
