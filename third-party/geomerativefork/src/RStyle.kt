/**
 * Copyright 2004-2008 Ricard Marxer  <email></email>@ricardmarxer.com>
 *
 *
 * This file is part of Geomerative.
 *
 *
 * Geomerative is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * Geomerative is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with Geomerative.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package geomerativefork.src

import processing.core.PApplet
import processing.core.PGraphics
import processing.core.PImage
import util.with

/**
 * @extended
 */
class RStyle {
  var texture: PImage? = null
  var fillDef = false
  var fill = false
    set(value) {
      fillDef = true
      field = value
    }
  var fillColor = -0x1000000
  var fillAlphaDef = false
  var fillAlpha = -0x1000000
    set(value) {
      fillAlphaDef = true
      field = value
    }

  var strokeDef = false
  var stroke = false
    set(value) {
      strokeDef = true
      field = value
    }
  var strokeColor = -0x1000000
  var strokeAlphaDef = false
  var strokeAlpha = -0x1000000
    set(value) {
      strokeAlphaDef = true
      field = value
    }
  var strokeWeightDef = false
  var strokeWeight = 1f
    set(value) {
      strokeWeightDef = true
      field = value
    }
  var strokeCapDef = false
  var strokeCap: Int = RG.PROJECT
  var strokeJoinDef = false
  var strokeJoin: Int = RG.MITER
  private var oldFill = false
  private var oldFillColor = 0
  private var oldStroke = false
  private var oldStrokeColor = 0
  private var oldStrokeWeight = 1f
  private var oldStrokeCap: Int = RG.PROJECT
  private var oldStrokeJoin: Int = RG.MITER

  constructor() {}
  constructor(p: RStyle) {
    texture = p.texture
    fillDef = p.fillDef
    fill = p.fill
    fillColor = p.fillColor
    fillAlphaDef = p.fillAlphaDef
    fillAlpha = p.fillAlpha
    strokeDef = p.strokeDef
    stroke = p.stroke
    strokeColor = p.strokeColor
    strokeAlphaDef = p.strokeAlphaDef
    strokeAlpha = p.strokeAlpha
    strokeWeightDef = p.strokeWeightDef
    strokeWeight = p.strokeWeight
    strokeCapDef = p.strokeCapDef
    strokeCap = p.strokeCap
    strokeJoinDef = p.strokeJoinDef
    strokeJoin = p.strokeJoin
  }

  fun setStyle(styleString: String?) {
    //RG.parent().println("Style parsing: " + styleString);
    val styleTokens = PApplet.splitTokens(
      styleString,
      ";"
    )
    for (i in styleTokens.indices) {
      val tokens = PApplet.splitTokens(
        styleTokens[i],
        ":"
      )
      tokens[0] = PApplet.trim(tokens[0])
      if (tokens[0] == "fill") {
        setFill(tokens[1])
      } else if (tokens[0] == "fill-opacity") {
        setFillAlpha(tokens[1])
      } else if (tokens[0] == "stroke") {
        setStroke(tokens[1])
      } else if (tokens[0] == "stroke-width") {
        setStrokeWeight(tokens[1])
      } else if (tokens[0] == "stroke-linecap") {
        setStrokeCap(tokens[1])
      } else if (tokens[0] == "stroke-linejoin") {
        setStrokeJoin(tokens[1])
      } else if (tokens[0] == "stroke-opacity") {
        setStrokeAlpha(tokens[1])
      } else if (tokens[0] == "opacity") {
        setAlpha(tokens[1])
      } else {
        PApplet.println("Attribute '" + tokens[0] + "' not known.  Ignoring it.")
      }
    }
  }

  fun setFill(_fillColor: Int) {
    //RG.parent().println("Setting fill by int: " + RG.parent().hex(_fillColor));
    fill = true
    fillColor = fillColor and -0x1000000 or (_fillColor and 0x00ffffff)
  }

  fun setFill(str: String) {
    //RG.parent().println("id: " + id);
    //RG.parent().println("  set fill: " + str);
    if (str == "none") {
      fill = false
    } else {
      setFill(getColor(str))
    }
    //RG.parent().println("  fillColor after: " + RG.parent().hex(fillColor));
  }

  fun setStroke(_strokeColor: Int) {
    stroke = true
    strokeColor = strokeColor and -0x1000000 or (_strokeColor and 0x00ffffff)
  }

  fun setStroke(str: String) {
    //RG.parent().println("  set stroke: " + str);
    if (str == "none") {
      stroke = false
    } else {
      setStroke(getColor(str))
    }
  }

  fun setStrokeWeight(str: String) {
    strokeWeight =
      (if (str.endsWith("px")) str.dropLast(2) else str).toFloat()
  }

  fun setStrokeCap(str: String) {
    strokeCapDef = true
    when (str) {
      "butt" -> strokeCap = RG.PROJECT
      "round" -> strokeCap = RG.ROUND
      "square" -> strokeCap = RG.SQUARE
    }
  }

  fun setStrokeJoin(str: String) {
    strokeJoinDef = true
    when (str) {
      "miter" -> strokeJoin = RG.MITER
      "round" -> strokeJoin = RG.ROUND
      "bevel" -> strokeJoin = RG.BEVEL
    }
  }

  fun setStrokeAlpha(str: String) {
    strokeAlpha = (str.toFloat() * 255f).toInt()
  }

  fun setFillAlpha(str: String) {
    fillAlpha = (str.toFloat() * 255f).toInt()
  }

  fun setAlpha(opacity: Float) {
    setAlpha((opacity * 100f).toInt())
  }

  fun setAlpha(opacity: Int) {
    fillAlpha = opacity
    strokeAlpha = opacity
  }

  fun setAlpha(str: String) {
    setAlpha(str.toFloat())
  }

  fun saveContext(g: PGraphics) {
    oldFill = g.fill
    oldFillColor = g.fillColor
    oldStroke = g.stroke
    oldStrokeColor = g.strokeColor
    oldStrokeWeight = g.strokeWeight
    oldStrokeCap = g.strokeCap
    oldStrokeJoin = g.strokeJoin
  }

  fun saveContext(p: PApplet) = p.g.with {
    oldFill = fill
    oldFillColor = fillColor
    oldStroke = stroke
    oldStrokeColor = strokeColor
    oldStrokeWeight = strokeWeight
    oldStrokeCap = strokeCap
    oldStrokeJoin = strokeJoin
  }


  fun saveContext() {
    saveContext(RG.parent())
  }

  fun restoreContext(g: PGraphics) = g.with {
    fill(oldFillColor)
    if (!oldFill) noFill()
    stroke(oldStrokeColor)
    strokeWeight(oldStrokeWeight)
    try {
      strokeCap(oldStrokeCap)
      strokeJoin(oldStrokeJoin)
    } catch (e: RuntimeException) {
    }
    if (!oldStroke) noStroke()
  }

  fun restoreContext(p: PApplet) = p.with {
    fill(oldFillColor)
    if (!oldFill) {
      noFill()
    }
    stroke(oldStrokeColor)
    strokeWeight(oldStrokeWeight)
    try {
      strokeCap(oldStrokeCap)
      strokeJoin(oldStrokeJoin)
    } catch (e: RuntimeException) {
    }
    if (!oldStroke) {
      noStroke()
    }
  }

  fun restoreContext() {
    restoreContext(RG.parent())
  }

  fun setContext(g: PGraphics) {
    if (fillAlphaDef) {
      if (fillDef) {
        fillColor = fillAlpha shl 24 and -0x1000000 or (fillColor and 0x00ffffff)
      } else {
        if (g.fill) {
          g.fill(fillAlpha shl 24 and -0x1000000 or (g.fillColor and 0x00ffffff))
        }
      }
    }
    if (fillDef) {
      g.fill(fillColor)
      if (!fill) g.noFill()
    }
    if (strokeWeightDef) g.strokeWeight(strokeWeight)
    try {
      if (strokeCapDef) g.strokeCap(strokeCap)
      if (strokeJoinDef) g.strokeJoin(strokeJoin)
    } catch (e: RuntimeException) {
    }
    if (strokeAlphaDef) {
      if (strokeDef) {
        strokeColor = strokeAlpha shl 24 and -0x1000000 or (strokeColor and 0x00ffffff)
      } else {
        if (g.stroke) {
          g.stroke(strokeAlpha shl 24 and -0x1000000 or (g.strokeColor and 0x00ffffff))
        }
      }
    }
    if (strokeDef) {
      g.stroke(strokeColor)
      if (!stroke) g.noStroke()
    }
  }

  fun setContext(p: PApplet) {
    if (fillAlphaDef) {
      if (fillDef) {
        fillColor = fillAlpha shl 24 and -0x1000000 or (fillColor and 0x00ffffff)
      } else {
        if (p.g.fill) {
          p.fill(fillAlpha shl 24 and -0x1000000 or (p.g.fillColor and 0x00ffffff))
        }
      }
    }
    if (fillDef) {
      p.fill(fillColor)
      if (!fill) p.noFill()
    }
    if (strokeWeightDef) p.strokeWeight(strokeWeight)
    try {
      if (strokeCapDef) p.strokeCap(strokeCap)
      if (strokeJoinDef) p.strokeJoin(strokeJoin)
    } catch (e: RuntimeException) {
    }
    if (strokeAlphaDef) {
      if (strokeDef) {
        strokeColor = strokeAlpha shl 24 and -0x1000000 or (strokeColor and 0x00ffffff)
      } else {
        if (p.g.stroke) {
          p.stroke(strokeAlpha shl 24 and -0x1000000 or (p.g.strokeColor and 0x00ffffff))
        }
      }
    }
    if (strokeDef) {
      p.stroke(strokeColor)
      if (!stroke) p.noStroke()
    }
  }

  fun setContext() = setContext(RG.parent())

  private fun getColor(colorStringParam: String): Int {
    var colorString = colorStringParam
    colorString = colorString.trim()
    if (colorString.startsWith("#")) {
      return PApplet.unhex("FF" + colorString.substring(1))
    } else if (colorString.startsWith("rgb")) {
      val rgb = PApplet.splitTokens(colorString, "rgb( , )")
      return RG.parent().color(rgb[0].toInt(), rgb[1].toInt(), rgb[2].toInt())
    }

    return when (colorString) {
      "black" -> 0
      "red" -> RG.parent().color(255, 0, 0)
      "green" -> RG.parent().color(0, 255, 0)
      "blue" -> RG.parent().color(0, 0, 255)
      "yellow" -> RG.parent().color(0, 255, 255)
      else -> 0
    }
  }
}
