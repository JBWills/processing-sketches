package util.polylines

import org.locationtech.jts.geom.Envelope

fun PolyLine.toEnvelope(): Envelope {
  val (min, max) = this.minMax
  return Envelope(min.x, max.x, min.y, max.y)
}
