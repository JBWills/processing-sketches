package util.polylines.clipping

import de.lighti.clipper.Path

val Path.isClosed get() = !isEmpty() && first() == last()
