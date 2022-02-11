package controls.panels.panelext.util

import kotlin.reflect.KMutableProperty0

interface RefGetter<T> {
  val name: String
  fun get(): T
  fun set(item: T)
}

interface RefWrapperList<T, TWrap> : RefGetter<TWrap> {
  val backingRefs: List<RefGetter<T>>

  override val name get() = backingRefs.joinToString()
}

interface RefWrapper2<T, K, TWrap> : RefGetter<TWrap> {
  val backingRef1: RefGetter<T>
  val backingRef2: RefGetter<K>

  override val name get() = "${backingRef1.name}.${backingRef2.name}"
}

interface RefWrapper<T, TWrap> : RefGetter<TWrap> {
  val backingRef: RefGetter<T>

  override val name get() = backingRef.name
}

fun <T> KMutableProperty0<T>.wrapSelf(): RefWrapper<T, T> = wrapped({ this }, { this })
fun <T> KMutableProperty0<T>.toRefGetter(): RefGetter<T> = object : RefGetter<T> {
  override val name: String
    get() = this@toRefGetter.name

  override fun get(): T = this@toRefGetter.get()

  override fun set(item: T) = this@toRefGetter.set(item)
}

fun <T, TWrap> KMutableProperty0<T>.wrapped(
  wrap: T.() -> TWrap,
  unwrap: TWrap.() -> T,
) = object : RefWrapper<T, TWrap> {
  override val backingRef: RefGetter<T> = this@wrapped.toRefGetter()

  override fun get(): TWrap = this@wrapped.get().wrap()

  override fun set(item: TWrap) = this@wrapped.set(item.unwrap())
}

fun <T, TWrap> RefGetter<T>.wrapped(
  wrap: T.() -> TWrap,
  unwrap: TWrap.() -> T,
) = object : RefWrapper<T, TWrap> {
  override val backingRef: RefGetter<T> = this@wrapped

  override fun get(): TWrap = this@wrapped.get().wrap()

  override fun set(item: TWrap) = this@wrapped.set(item.unwrap())
}

@JvmName("wrappedRefGetters")
fun <T, K, TWrap> Pair<RefGetter<T>, RefGetter<K>>.wrapped(
  wrap: Pair<T, K>.() -> TWrap,
  unwrap: TWrap.() -> Pair<T, K>,
) = object : RefWrapper2<T, K, TWrap> {
  override val backingRef1: RefGetter<T> = this@wrapped.first
  override val backingRef2: RefGetter<K> = this@wrapped.second

  override fun get(): TWrap = (this@wrapped.first.get() to this@wrapped.second.get()).wrap()

  override fun set(item: TWrap) {
    item.unwrap().let { (newItem1, newItem2) ->
      this@wrapped.first.set(newItem1)
      this@wrapped.second.set(newItem2)
    }
  }
}

fun <T, K, TWrap> Pair<KMutableProperty0<T>, KMutableProperty0<K>>.wrapped(
  wrap: Pair<T, K>.() -> TWrap,
  unwrap: TWrap.() -> Pair<T, K>,
) = (first.toRefGetter() to second.toRefGetter()).wrapped(wrap, unwrap)
