import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import kotlin.math.ceil
import kotlin.math.min

fun createButton(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, clickAction: () -> (Unit)) {
    canvas.drawRRect(RRect(
        x0, y0, x1, y1,
        FloatArray(4) { 2f }
    ), blackPaint)
    canvas.drawRRect(RRect(
        x0 + 1f, y0 + 1f, x1 - 1f, y1 - 1f,
        FloatArray(4) { 2f }
    ), greyPaint)
    buttons.add(Button(x0, y0, x1, y1, clickAction))
}

class MainRenderer(private val layer: SkiaLayer): SkiaRenderer {

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()

        when(status) {
            MainWindowStatus.ChoosingChart -> drawChoosingChartScreen(canvas, w, h)
            MainWindowStatus.ChartParameters -> drawChartParametersScreen(canvas, w, h)
        }

        layer.needRedraw()
    }

    private fun drawChoosingChartScreen(canvas: Canvas, w: Int, h : Int) {
        val header = "Выберите тип диаграммы"
        canvas.drawString(header, (w - headerFont.measureTextWidth(header)) / 2, headerFont.size + 10f, headerFont, blackPaint)
        val headerH = headerFont.size + 20f // Высота заголовка с отступами
        val chunkW = w.toFloat() / 3
        val chunkH = (h - headerH) / 3 // Размеры одной части экрана, в которой помещается один вид диаграммы

        buttons.clear()
        for (chunkX in 0..2) {
            for (chunkY in 0..2) {
                val chartIdx = chunkY * 3 + chunkX
                val chartType = ChartType.values().getOrNull(chartIdx) ?: continue
                val chartName = nameOfChart[chartType]
                require(chartName != null)
                val x = chunkX * chunkW
                val y = headerH + chunkY * chunkH

                // кнопка с названием диаграммы
                val rectX0 = x + 5f
                val rectX1 = x + chunkW - 5f
                val rectY0 = y + chunkH - 25f
                val rectY1 = y + chunkH - 5f
                createButton(canvas, rectX0, rectY0, rectX1, rectY1) {
                    status = MainWindowStatus.ChartParameters
                    chosenChart = chartType
                }
                canvas.drawString(
                    chartName,
                    x + (chunkW - basicFont.measureTextWidth(chartName)) / 2,
                    y + chunkH - 10f,
                    basicFont, blackPaint
                )

                // предпросмотр диаграммы
                chartDrawFunction[chartType]?.invoke(canvas,
                    x + 10f, y + 10f, chunkW - 20f, chunkH - 50f)
            }
        }
    }

    private fun drawChartParametersScreen(canvas: Canvas, w: Int, h: Int) {
        require(chosenChart != null)
        val chartName = nameOfChart[chosenChart]
        require(chartName != null)
        // временно
        canvas.drawString(chartName, 30f, 30f, headerFont, blackPaint)
    }
}

class ChartRenderer(private val layer: SkiaLayer): SkiaRenderer {

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()



        layer.needRedraw()
    }
}

fun calculateStep(minValue: Int, maxValue: Int) : Int {
    return ceil((maxValue - minValue).toFloat() / 10).toInt()
}

fun drawGrid(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, len: Int) {
    canvas.drawLine(x, y, x, y + h, blackPaint)
    canvas.drawLine(x, y + h, x + w, y + h, blackPaint)
    for (i in 0..9)
        canvas.drawLine(x, y + h / 10 * i, x + w, y + h / 10 * i, greyPaint)
    for (i in 1..len)
        canvas.drawLine(x + w / (len + 1) * i, y + h * 101 / 100,
            x + w / (len + 1) * i, y + h * 99 / 100, blackPaint)
}

fun drawBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    val dataNum = min(inputData.data.size, 5) // количество отображаемых наборов данных
    val maxValue = inputData.data.subList(0, dataNum).maxOf { it.maxOf { x -> x } }
    val step = calculateStep(0, maxValue) // вертикальный шаг сетки

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth((step * 10).toString()) * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, len)

    for (i in 0..10) {
        val text = (i * step).toString().padStart((step * 10).toString().length)
        canvas.drawString(text, x,y + (10 - i) * (h - bottomTab) / 10 + fontSize / 2, font, blackPaint)
    }

    val horStepLen = (w - leftTab) / (len + 1) // длина одного горизонтального деления
    inputData.firstRow.forEachIndexed { i, name ->
        canvas.drawString(name, x + leftTab + horStepLen * (i + 1) - font.measureTextWidth(name) / 2,
            y + h, font, blackPaint)

        val leftBorder = horStepLen * (i + 1) - horStepLen / 2 * 9 / 10 + x + leftTab
        val rightBorder = horStepLen * (i + 1) + horStepLen / 2 * 9 / 10 + x + leftTab
        for (pos in 0 until dataNum) {
            val left = leftBorder + (rightBorder - leftBorder) / dataNum * pos
            val right = left + (rightBorder - leftBorder) / dataNum
            val height = inputData.data[pos][i].toFloat() / step * (h - bottomTab) / 10
            canvas.drawRect(Rect(left, y + h - height, right, y + h - bottomTab), palettes[chosenPalette][pos])
        }
    }
}

fun drawStackedBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawNormStackedBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawLineChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawAreaChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawNormAreaChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawPieChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}

fun drawRadarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    // временно
    canvas.drawString("Здесь будет диаграмма", x + 5f, y + h / 2, basicFont, blackPaint)
    canvas.drawPolygon(arrayOf(
        Point(x, y), Point(x + w, y),
        Point(x + w, y + h), Point(x, y + h)),
        blackPaint
    )
}