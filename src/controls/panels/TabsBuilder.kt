package controls.panels

class TabsBuilder(val tabs: MutableList<ControlTab>) : MutableList<ControlTab> by tabs {
  constructor() : this(mutableListOf())

  fun build(): List<ControlTab> = tabs.toList()

  operator fun ControlTab.unaryPlus() = tabs.add(this)
  operator fun List<ControlTab>.unaryPlus() = tabs.addAll(this)
  operator fun Array<ControlTab>.unaryPlus() = tabs.addAll(this)

  fun tab(name: String, tabStyle: TabStyle? = null, block: TabBuilder.() -> Unit) {
    +TabBuilder(name, tabStyle ?: TabStyle.Base).apply(block).buildTab()
  }

  fun <T> tabs(
    items: List<T>,
    getTabName: (Int, T) -> String,
    getTabStyle: ((Int, T) -> TabStyle)? = null,
    block: PanelBuilder.(Int, T) -> Unit
  ) {
    items.forEachIndexed { index, item ->
      val name = getTabName(index, item)
      val style = getTabStyle?.invoke(index, item) ?: TabStyle.Base
      +TabBuilder(name, style) { block(index, item) }.buildTab()
    }
  }

  companion object {
    fun tabs(block: TabsBuilder.() -> Unit): List<ControlTab> =
      TabsBuilder().apply(block).build()

    fun singleTab(
      name: String,
      style: TabStyle? = null,
      block: TabBuilder.() -> Unit,
    ) = tabs {
      tab(name, style, block)
    }

    fun layerTab(
      style: TabStyle? = null,
      block: TabBuilder.() -> Unit,
    ) = tabs {
      tab("L", style, block)
    }
  }
}
