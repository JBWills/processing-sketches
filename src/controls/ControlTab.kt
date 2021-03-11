package controls

import geomerativefork.src.util.flatMapArray
import geomerativefork.src.util.mapArray

/**
 * Contains data to build a tab full of controls.
 */
class ControlTab(val name: String, sections: List<ControlSectionable>) {
  val controlSections: List<ControlGroup> = sections.flatMapArray { section ->
    section.toControlGroups().mapArray { it.toControlGroup() }
  }.toList()

  constructor(name: String, vararg groups: ControlGroupable) : this(
    name,
    groups.map { it.toControlGroup() })

  constructor(name: String, vararg sections: ControlSectionable) : this(
    name,
    *sections.flatMapArray { it.toControlGroups() }
  )

  constructor(tab: ControlTab, name: String) : this(
    name,
    *tab.controlSections.toTypedArray()
  )

  fun withName(newName: String) = ControlTab(this, newName)
  fun withName(nameFunc: (String) -> String) = ControlTab(this, nameFunc(name))

  companion object {
    fun tab(name: String, vararg sections: ControlSectionable) = ControlTab(
      name,
      *sections
    )

    fun layerTab(vararg sections: ControlSectionable) = listOf(
      tab("L", *sections)
    )
  }

  override fun toString(): String {
    return "ControlTab(name='$name', controlSections=$controlSections)"
  }
}
