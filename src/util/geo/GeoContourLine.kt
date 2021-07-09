package util.geo

import coordinate.BoundRect
import coordinate.BoundRect.Companion.toBoundRect
import coordinate.Point
import coordinate.Point.Companion.toPoint
import kotlinx.serialization.Serializable
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.LineString
import org.opengis.feature.Feature
import util.io.geoJson.getDoubleProperty
import util.pointsAndLines.polyLine.PolyLine

private fun Array<Coordinate>.toPoints(): List<Point> = map { it.toPoint() }

private const val ELEVATION_PROP_KEY = "elevation"
private const val SHAPE_LEN_PROP_KEY = "shape_len"

@Serializable
data class GeoContourLine(
  val elevation: Double,
  val length: Double,
  val bounds: BoundRect,
  val coordinates: PolyLine,
) : PolyLine by coordinates {
  companion object {
    fun Feature.toContourLine(): GeoContourLine {
      if (defaultGeometryProperty.type.binding == LineString::class.java) {
        val elevation = getDoubleProperty(ELEVATION_PROP_KEY)
        val length = getDoubleProperty(SHAPE_LEN_PROP_KEY)
        val contourBounds = bounds.toBoundRect()
        val points: PolyLine = (defaultGeometryProperty.value as LineString)
          .coordinates
          .toPoints()

        return GeoContourLine(elevation, length, contourBounds, points)
      } else {
        throw Exception("Feature is not a contour line.")
      }
    }
  }

  override fun toString(): String {
    return "GeoContourLine(elevation=$elevation, length=$length, bounds=$bounds, coordinates=[${coordinates.size} points])"
  }
}
