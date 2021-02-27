package controls

import geomerativefork.src.util.flatMapArray


interface ControlSectionable {
  fun toControlGroups(): Array<ControlGroupable>
}

class ControlSection(vararg val groups: ControlGroupable) : ControlSectionable {
  constructor(vararg sections: ControlSectionable) : this(*sections.flatMapArray { it.toControlGroups() })
  constructor(groups: List<ControlGroupable>) : this(*groups.toTypedArray())

  override fun toControlGroups(): Array<ControlGroupable> = groups.toList().toTypedArray()

  companion object {
    fun List<ControlGroupable>.toControlSection() = ControlSection(this)
    fun Array<ControlGroupable>.toControlSection() = ControlSection(*this)
    fun section(vararg groups: ControlGroupable) = ControlSection(*groups)
    fun section(vararg sections: ControlSectionable) = ControlSection(*sections)
  }
}
