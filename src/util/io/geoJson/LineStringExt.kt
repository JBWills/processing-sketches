package util.io.geoJson

import coordinate.Point.Companion.toPoint
import org.locationtech.jts.geom.LineString

fun LineString.toPolyLine() =
  coordinates.mapNotNull { if (it.x.isNaN() || it.y.isNaN()) null else it.toPoint() }


