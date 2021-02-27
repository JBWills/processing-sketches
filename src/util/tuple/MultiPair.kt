package util.tuple

data class Pair3<A, B, C>(val a: A, val b: B, val c: C)
data class Pair4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
data class Pair5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
data class Pair6<A, B, C, D, E, F>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F)
data class Pair7<A, B, C, D, E, F, G>(
  val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G,
)

data class Pair8<A, B, C, D, E, F, G, H>(
  val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H,
)

infix fun <A, B, C> Pair<A, B>.and(that: C) = Pair3(this.first, this.second, that)
infix fun <A, B, C, D> Pair3<A, B, C>.and(that: D) = Pair4(a, b, c, that)
infix fun <A, B, C, D, E> Pair4<A, B, C, D>.and(that: E) = Pair5(a, b, c, d, that)
infix fun <A, B, C, D, E, F> Pair5<A, B, C, D, E>.and(that: F) = Pair6(a, b, c, d, e, that)
infix fun <A, B, C, D, E, F, G> Pair6<A, B, C, D, E, F>.and(that: G) = Pair7(a, b, c, d, e, f, that)
infix fun <A, B, C, D, E, F, G, H> Pair7<A, B, C, D, E, F, G>.and(that: H) = Pair8(a, b, c, d, e, f, g, that)
