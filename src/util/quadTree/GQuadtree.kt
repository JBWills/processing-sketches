package util.quadTree

import org.locationtech.jts.geom.Envelope
import org.locationtech.jts.index.quadtree.Quadtree

/**
 * Just a version of QuadTree with proper generic support
 */
@Suppress("UNCHECKED_CAST")
class GQuadtree<T>(val toEnvelope: T.() -> Envelope) {
  private val backingTree = Quadtree()

  val depth get(): Int = backingTree.depth()

  val isEmpty get(): Boolean = backingTree.isEmpty
  val size get():  Int = backingTree.size()

  fun insert(item: T) = backingTree.insert(item.toEnvelope(), item)
  fun insertAll(items: Collection<T>) =
    items.map { item -> backingTree.insert(item.toEnvelope(), item) }

  fun remove(item: T): Boolean = backingTree.remove(item.toEnvelope(), item)

  fun query(searchEnv: Envelope): List<T> {
    return (backingTree.query(searchEnv) as List<T>).filter {
      it.toEnvelope().intersects(searchEnv)
    }
  }

  fun query(searchEnv: Envelope, visitor: (T) -> Unit): Unit = backingTree.query(searchEnv) {
    val casted = it as T
    if (searchEnv.intersects(casted.toEnvelope())) {
      visitor(casted)
    }
  }

  fun queryAll(): List<T> = backingTree.queryAll() as List<T>
}
