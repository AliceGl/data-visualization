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