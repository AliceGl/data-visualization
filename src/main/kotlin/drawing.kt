import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import java.awt.Polygon
import kotlin.math.ceil
import kotlin.math.floor
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

        if (chosenChart != null)
            drawChartOnScreen(canvas, w, h)

        layer.needRedraw()
    }

    private fun drawChartOnScreen(canvas: Canvas, w: Int, h: Int) {
        // временно
        chartDrawFunction[chosenChart]?.invoke(canvas,
            10f, 10f, w - 20f, h - 20f)
    }
}

fun calculateStep(minValue: Int, maxValue: Int) : Pair<Int, Int> {
    for (stepFirstDigit in listOf(1, 2, 5)) {
        var stepTenPow = 1
        repeat(9) {
            val step = stepFirstDigit * stepTenPow
            val stepNum = ceil(maxValue.toFloat() / step).toInt() -
                    floor(minValue.toFloat() / step).toInt()
            if (stepNum in 5..15)
                return Pair(step, stepNum)
            stepTenPow *= 10
        }
    }
    require(false)
    return Pair(0, 0)
}

fun drawGrid(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, height: Int, len: Int) {
    canvas.drawLine(x, y, x, y + h, blackPaint)
    canvas.drawLine(x, y + h, x + w, y + h, blackPaint)
    for (i in 0 until height)
        canvas.drawLine(x, y + h / height * i, x + w, y + h / height * i, greyPaint)
    for (i in 1..len)
        canvas.drawLine(x + w / (len + 1) * i, y + h * 101 / 100,
            x + w / (len + 1) * i, y + h * 99 / 100, blackPaint)
}

private fun drawScale(canvas: Canvas, step: Int, stepNum: Int,
                      x: Float, y: Float, h: Float, font: Font, isPercent: Boolean = false) {
    for (i in 0..stepNum) {
        val text = (i * step).toString().padStart((step * stepNum).toString().length) + if (isPercent) "%" else ""
        canvas.drawString(text, x, y + (stepNum - i) * h / stepNum + font.size / 2, font, blackPaint)
    }
}

fun drawBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    val dataNum = min(inputData.data.size, 5) // количество отображаемых наборов данных
    val maxValue = inputData.data.subList(0, dataNum).maxOf { it.maxOf { x -> x } }
    val (step, stepNum) = calculateStep(0, maxValue) // вертикальный шаг сетки и количество делений

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth((step * stepNum).toString()) * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, stepNum, len)

    drawScale(canvas, step, stepNum, x, y, h - bottomTab, font)

    val horStepLen = (w - leftTab) / (len + 1) // длина одного горизонтального деления
    inputData.firstRow.forEachIndexed { i, name ->
        canvas.drawString(name, x + leftTab + horStepLen * (i + 1) - font.measureTextWidth(name) / 2,
            y + h, font, blackPaint)

        val leftBorder = horStepLen * (i + 1) - horStepLen / 2 * 9 / 10 + x + leftTab
        val rightBorder = horStepLen * (i + 1) + horStepLen / 2 * 9 / 10 + x + leftTab
        for (pos in 0 until dataNum) {
            val left = leftBorder + (rightBorder - leftBorder) / dataNum * pos
            val right = left + (rightBorder - leftBorder) / dataNum
            val height = inputData.data[pos][i].toFloat() / step * (h - bottomTab) / stepNum
            canvas.drawRect(Rect(left, y + h - bottomTab - height, right, y + h - bottomTab),
                palettes[chosenPalette][pos])
        }
    }
}

fun drawStackedBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    val dataNum = min(inputData.data.size, 5) // количество отображаемых наборов данных
    val prefixSums = List(inputData.firstRow.size) { mutableListOf(0) }
    for (i in inputData.firstRow.indices) {
        inputData.data.forEach {
            prefixSums[i].add(prefixSums[i].last() + it[i])
        }
    }
    val maxValue = prefixSums.maxOf { it.last() }
    val (step, stepNum) = calculateStep(0, maxValue) // вертикальный шаг сетки и количество делений

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth((step * stepNum).toString()) * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, stepNum, len)

    drawScale(canvas, step, stepNum, x, y, h - bottomTab, font)

    val horStepLen = (w - leftTab) / (len + 1) // длина одного горизонтального деления
    inputData.firstRow.forEachIndexed { i, name ->
        canvas.drawString(name, x + leftTab + horStepLen * (i + 1) - font.measureTextWidth(name) / 2,
            y + h, font, blackPaint)

        val left = horStepLen * (i + 1) - horStepLen / 2 * 9 / 10 + x + leftTab
        val right = horStepLen * (i + 1) + horStepLen / 2 * 9 / 10 + x + leftTab
        for (pos in dataNum - 1 downTo 0) {
            val height = prefixSums[i][pos + 1].toFloat() / step * (h - bottomTab) / stepNum
            canvas.drawRect(Rect(left, y + h - bottomTab - height, right, y + h - bottomTab),
                palettes[chosenPalette][pos])
        }
    }
}

fun drawNormStackedBarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    val dataNum = min(inputData.data.size, 5) // количество отображаемых наборов данных
    val prefixSums = List(inputData.firstRow.size) { mutableListOf(0) }
    for (i in inputData.firstRow.indices) {
        inputData.data.forEach {
            prefixSums[i].add(prefixSums[i].last() + it[i])
        }
    }
    val (step, stepNum) = Pair(10, 10) // вертикальный шаг сетки и количество делений

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth("100%") * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, stepNum, len)

    drawScale(canvas, step, stepNum, x, y, h - bottomTab, font, true)

    val horStepLen = (w - leftTab) / (len + 1) // длина одного горизонтального деления
    inputData.firstRow.forEachIndexed { i, name ->
        canvas.drawString(name, x + leftTab + horStepLen * (i + 1) - font.measureTextWidth(name) / 2,
            y + h, font, blackPaint)

        val left = horStepLen * (i + 1) - horStepLen / 2 * 9 / 10 + x + leftTab
        val right = horStepLen * (i + 1) + horStepLen / 2 * 9 / 10 + x + leftTab
        for (pos in dataNum - 1 downTo 0) {
            val height = prefixSums[i][pos + 1].toFloat() / prefixSums[i].last() * (h - bottomTab)
            canvas.drawRect(Rect(left, y + h - bottomTab - height, right, y + h - bottomTab),
                palettes[chosenPalette][pos])
        }
    }
}

fun drawLineChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    val dataNum = min(inputData.data.size, 5) // количество отображаемых наборов данных
    val maxValue = inputData.data.subList(0, dataNum).maxOf { it.maxOf { x -> x } }
    val (step, stepNum) = calculateStep(0, maxValue) // вертикальный шаг сетки и количество делений

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth((step * stepNum).toString()) * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, stepNum, len)

    drawScale(canvas, step, stepNum, x, y, h - bottomTab, font)

    val horStepLen = (w - leftTab) / (len + 1) // длина одного горизонтального деления
    inputData.firstRow.forEachIndexed { i, name ->
        canvas.drawString(name, x + leftTab + horStepLen * (i + 1) - font.measureTextWidth(name) / 2,
            y + h, font, blackPaint)
    }

    inputData.data.forEachIndexed { pos, data ->
        val heights = data.map { it.toFloat() / step * (h - bottomTab) / stepNum }
        for (i in 1 until data.size) {
            canvas.drawLine(x + leftTab + horStepLen * i, y + h - bottomTab - heights[i - 1],
                x + leftTab + horStepLen * (i + 1), y + h - bottomTab - heights[i],
                palettes[chosenPalette][pos] )
        }
    }
}

fun drawAreaChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    val dataNum = min(inputData.data.size, 5) // количество отображаемых наборов данных
    val prefixSums = List(inputData.firstRow.size) { mutableListOf(0) }
    for (i in inputData.firstRow.indices) {
        inputData.data.forEach {
            prefixSums[i].add(prefixSums[i].last() + it[i])
        }
    }
    val maxValue = prefixSums.maxOf { it.last() }
    val (step, stepNum) = calculateStep(0, maxValue) // вертикальный шаг сетки и количество делений

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth((step * stepNum).toString()) * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, stepNum, len)

    drawScale(canvas, step, stepNum, x, y, h - bottomTab, font)

    val horStepLen = (w - leftTab) / (len + 1) // длина одного горизонтального деления
    inputData.firstRow.forEachIndexed { i, name ->
        canvas.drawString(name, x + leftTab + horStepLen * (i + 1) - font.measureTextWidth(name) / 2,
            y + h, font, blackPaint)
    }

    for (pos in dataNum - 1 downTo 0) {
        val heights = prefixSums.map { it[pos + 1].toFloat() / step * (h - bottomTab) / stepNum }
        val path = Path()
        path.moveTo(Point(x + leftTab + horStepLen, y + h - bottomTab))
        for (i in heights.indices)
            path.lineTo(Point(x + leftTab + horStepLen * (i + 1), y + h - bottomTab - heights[i]))
        path.lineTo(Point(x + w - horStepLen, y + h - bottomTab))
        canvas.drawPath(path, palettes[chosenPalette][pos])
    }
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