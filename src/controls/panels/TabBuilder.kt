package controls.panels

class TabBuilder(
  var tabName: String,
  var tabStyle: TabStyle = TabStyle.Base,
  var tabPanels: MutableList<Panelable>
) : PanelBuilder(tabPanels) {
  constructor(
    tabName: String,
    tabStyle: TabStyle = TabStyle.Base,
    block: PanelBuilder.() -> Unit = {}
  ) : this(
    tabName,
    tabStyle,
    tabPanels = mutableListOf(),
  ) {
    block()
  }

  fun buildTab(): ControlTab = ControlTab(tabName, tabStyle, build())
}
