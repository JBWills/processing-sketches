package controls.panels.panelext.util

import coordinate.Deg
import kotlin.reflect.KMutableProperty0

fun KMutableProperty0<Deg>.doubleWrapped(): RefGetter<Double> = wrapSelf().doubleWrapped()
fun RefGetter<Deg>.doubleWrapped(): RefGetter<Double> = wrapped({ value }, { Deg(this) })
