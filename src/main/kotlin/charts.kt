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
    ChartType.Bar to "Гистограмма",
    ChartType.StackedBar to "Гистограмма с накоплением",
    ChartType.NormStackedBAr to "Нормированная гистограмма с накоплением",
    ChartType.Line to "График",
    ChartType.Area to "С областями и накоплением",
    ChartType.NormArea to "Нормированная с областями и накоплением",
    ChartType.Pie to "Круговая",
    ChartType.Radar to "Радиальная"
)