package controls.panels

class TabBuilder(
  var tabName: String,
  var tabStyle: TabStyle = TabStyle.Base,
  var tabPanels: MutableList<Panelable>
) : PanelBuilder(tabPanels) {

  constructor(tabName: String, tabStyle: TabStyle = TabStyle.Base) : this(
    tabName,
    tabStyle,
    tabPanels = mutableListOf(),
  )

  fun buildTab(): ControlTab = ControlTab(tabName, tabStyle, build())
}
