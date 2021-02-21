package controls

import geomerativefork.src.util.flatMapArray

interface ControlSectionable {
  fun toControlGroups(): Array<ControlGroupable>
}

interface ControlGroupable : ControlSectionable {
  fun toControlGroup(): ControlGroup
  override fun toControlGroups(): Array<ControlGroupable> = arrayOf(toControlGroup())
}

fun List<ControlGroupable>.toControlGroups() = map { it.toControlGroup() }
fun List<ControlSectionable>.flatten(): List<ControlGroupable> =
  flatMap { it.toControlGroups().toList() }

fun controls(vararg sections: ControlSectionable): List<ControlGroupable> =
  sections.flatMap { it.toControlGroups().toList() }

fun controlsArray(vararg sections: ControlSectionable): Array<ControlGroupable> =
  sections.flatMapArray { it.toControlGroups() }

class ControlGroup(vararg val controls: Control, val heightRatio: Number = 1) : ControlGroupable {
  val size get() = controls.size

  val controlsList get() = controls.toList()

  constructor(
    vararg controlFields: ControlField<*>,
    heightRatio: Number = 1,
  ) : this(
    *controlFields.flatMapArray { field ->
      field.toControlGroups().flatMapArray { it.toControlGroup().controlsList.toTypedArray() }
    },
    heightRatio = heightRatio
  )

  constructor(
    controls: List<ControlGroupable>,
    heightRatio: Number = 1,
  ) : this(
    *controls.flatMap { it.toControlGroup().controlsList }.toTypedArray(),
    heightRatio = heightRatio
  )

  override fun toControlGroup() = this
}

fun List<ControlGroup>.totalRatio() = sumByDouble { it.heightRatio.toDouble() }
