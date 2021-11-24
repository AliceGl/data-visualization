enum class ChartType {
    Bar, StackedBar, NormStackedBAr, Line, Area, NormArea, Pie, Radar
}

val chartDrawFunction = mapOf(
    ChartType.Bar to ::drawBarChart,
    ChartType.StackedBar to ::drawStackedBarChart,
    ChartType.NormStackedBAr to ::drawNormStackedBarChart,
    ChartType.Line to ::drawLineChart,
    ChartType.Area to ::drawAreaChart,
    ChartType.NormArea to ::drawNormAreaChart,
    ChartType.Pie to ::drawPieChart,
    ChartType.Radar to ::drawRadarChart
)

val nameOfChart = mapOf(
    ChartType.Bar to "Histogram",
    ChartType.StackedBar to "Stacked histogram",
    ChartType.NormStackedBAr to "Stacked normalized histogram",
    ChartType.Line to "Line chart",
    ChartType.Area to "Stacked area chart",
    ChartType.NormArea to "Stacked normalized area chart",
    ChartType.Pie to "Pie chart",
    ChartType.Radar to "Radar chart"
)