package util.polylines.clipping.orient

import de.lighti.clipper.Path
import de.lighti.clipper.Paths

fun Path.orientAsHole(): Path = if (orientation()) apply { reverse() } else this
fun Path.orientAsPoly(): Path = if (orientation()) this else apply { reverse() }

fun Paths.orientAsHole(): Paths = onEach { it.orientAsHole() }
fun Paths.orientAsPoly(): Paths = onEach { it.orientAsPoly() }
