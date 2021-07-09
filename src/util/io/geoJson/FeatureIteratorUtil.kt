package util.io.geoJson

import org.geotools.feature.FeatureCollection
import org.geotools.feature.FeatureIterator
import org.opengis.feature.Feature

fun <F : Feature> FeatureIterator<F>.toIterator(): Iterator<F> = object : Iterator<F> {
  override fun hasNext(): Boolean = this@toIterator.hasNext()
  override fun next(): F = this@toIterator.next()
}

fun <F : Feature> FeatureCollection<*, F>.toSequence(): Sequence<F> =
  features().toIterator().asSequence()

