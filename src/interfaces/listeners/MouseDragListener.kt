package interfaces.listeners

import coordinate.Point
import processing.event.MouseEvent
import util.io.input.MouseEventType.Click
import util.io.input.MouseEventType.Drag
import util.io.input.MouseEventType.Enter
import util.io.input.MouseEventType.Exit
import util.io.input.MouseEventType.Move
import util.io.input.MouseEventType.Press
import util.io.input.MouseEventType.Release
import util.io.input.MouseEventType.Wheel
import util.io.input.point
import util.io.input.type
import util.iterators.copy

class MouseDragListener : MouseListener {
  var isDragging = false

  val currentMouseDrag = mutableListOf<Point>()

  val mouseDragsSinceLastDraw: MutableList<List<Point>> = mutableListOf()

  override fun onEvent(e: MouseEvent) {
    when (e.type) {
      Press -> {
        isDragging = true
        currentMouseDrag.add(e.point)
      }
      Drag -> {
        currentMouseDrag.add(e.point)
      }
      Release -> {
        isDragging = false
        currentMouseDrag.add(e.point)
        mouseDragsSinceLastDraw.add(currentMouseDrag.toList())
        currentMouseDrag.clear()
      }
      Click,
      Move,
      Enter,
      Exit,
      Wheel,
      -> return
    }
  }

  fun popDrags(): List<List<Point>> {
    val dragsCopy = mouseDragsSinceLastDraw.copy()
    mouseDragsSinceLastDraw.clear()
    return dragsCopy
  }

  fun peekCurrentDrags(): List<Point> = currentMouseDrag.copy()
}
