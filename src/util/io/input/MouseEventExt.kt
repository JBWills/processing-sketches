package util.io.input

import coordinate.Point
import processing.event.MouseEvent


enum class MouseEventType(val action: Int) {
  Press(MouseEvent.PRESS),
  Release(MouseEvent.RELEASE),
  Click(MouseEvent.CLICK),
  Drag(MouseEvent.DRAG),
  Move(MouseEvent.MOVE),
  Enter(MouseEvent.ENTER),
  Exit(MouseEvent.EXIT),
  Wheel(MouseEvent.WHEEL),
  ;

  companion object {
    fun getType(action: Int): MouseEventType? = values().find { it.action == action }
  }

}

val MouseEvent.type: MouseEventType get() = MouseEventType.getType(action)!!

val MouseEvent.point: Point get() = Point(x, y)
