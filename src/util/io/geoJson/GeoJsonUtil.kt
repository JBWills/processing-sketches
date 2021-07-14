package util.io.geoJson

import org.geotools.geojson.feature.FeatureJSON
import org.geotools.geojson.geom.GeometryJSON
import org.opengis.feature.Feature
import org.opengis.feature.Property
import org.opengis.util.ProgressListener
import util.io.streamFile
import java.io.File

const val DecimalFidelity: Int = 8

private fun getFeatureReader(decimalFidelity: Int) = FeatureJSON(GeometryJSON(decimalFidelity))

fun File.readGeoJsonCollection(
  decimalFidelity: Int = DecimalFidelity,
  progressListener: ProgressListener = GeoJsonProgressListener(),
  read: (Feature) -> Unit,
) = streamFile {
  getFeatureReader(decimalFidelity)
    .readFeatureCollection(it)
    .accepts(read, progressListener)
}

fun Feature.getStringProperty(name: String): String {
  val prop: Property? = getProperty(name)
  prop ?: throw Exception("Property with name: $name doesn't exist on Feature.")
  if (prop.type.binding == String::class.java) {
    return prop.value as String
  } else {
    throw Exception("Trying to get feature property: $name as string when actual type is: ${prop.type.binding}")
  }
}

fun Feature.getDoubleProperty(name: String): Double {
  val prop: Property? = getProperty(name)
  prop ?: throw Exception("Property with name: $name doesn't exist on Feature.")
  return when (prop.type.binding) {
    java.lang.Double::class.java,
    Double::class.java -> prop.value as Double
    java.lang.String::class.java,
    String::class.java -> {
      val stringVal = (prop.value as String)
      stringVal.toDoubleOrNull()
        ?: throw Exception("Could convert prop name: $name string value: $stringVal to double.")
    }
    java.lang.Integer::class.java,
    Int::class.java -> (prop.value as Int).toDouble()
    java.lang.Float::class.java,
    Float::class.java -> (prop.value as Float).toDouble()
    java.lang.Short::class.java,
    Short::class.java -> (prop.value as Short).toDouble()
    java.lang.Long::class.java,
    Long::class.java -> (prop.value as Long).toDouble()
    else -> {
      throw Exception("Trying to get feature property: $name as double when actual type is: ${prop.type.binding}")
    }
  }
}
