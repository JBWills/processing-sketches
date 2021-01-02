package controls

interface ControlGroupable {
  abstract fun toControlGroup(): ControlGroup
}

fun List<ControlGroupable>.toControlGroups() = map { it.toControlGroup() }

class ControlGroup(vararg val controls: Control, val heightRatio: Number = 1) : ControlGroupable {
  val size get() = controls.size

  constructor(vararg controlFields: ControlField<*>, heightRatio: Number = 1) : this(*controlFields.flatMap { it.getControls().toList() }.toTypedArray(), heightRatio = heightRatio)

  override fun toControlGroup() = this
}

fun List<ControlGroup>.totalRatio() = sumByDouble { it.heightRatio.toDouble() }
