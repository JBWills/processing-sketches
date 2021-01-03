package controls

interface ControlGroupable {
  abstract fun toControlGroup(): ControlGroup
}

fun List<ControlGroupable>.toControlGroups() = map { it.toControlGroup() }

class ControlGroup(vararg val controls: Control, val heightRatio: Number = 1) : ControlGroupable {
  val size get() = controls.size

  val controlsList get() = controls.toList()

  constructor(
    vararg controlFields: ControlField<*>,
    heightRatio: Number = 1,
  ) : this(
    controlFields.toList(),
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
