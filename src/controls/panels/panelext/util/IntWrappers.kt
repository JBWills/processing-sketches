package controls.panels.panelext.util

import kotlin.reflect.KMutableProperty0

fun KMutableProperty0<Int>.doubleWrapped(): RefGetter<Double> = wrapSelf().doubleWrapped()
fun RefGetter<Int>.doubleWrapped(): RefGetter<Double> = wrapped({ toDouble() }, { toInt() })
