package util.window

import coordinate.Point
import processing.awt.PSurfaceAWT.SmoothCanvas
import processing.core.PApplet
import processing.core.PConstants.JAVA2D
import java.awt.Frame

fun PApplet.getWindowLocation(): Point {
  return when (sketchRenderer()) {
    JAVA2D -> {
      val f = (surface.native as SmoothCanvas)
        .frame as Frame
      Point(f.x, f.y)
    }
    else -> {
      throw Exception("Need to implement other renderers")
    }
  }

}

fun PApplet.setLocation(p: Point) = surface.setLocation(p.xi, p.yi)
