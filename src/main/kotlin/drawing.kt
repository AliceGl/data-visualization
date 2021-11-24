import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import kotlin.math.*

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
            MainWindowStatus.ChartParameters -> drawChartParametersScreen(canvas)
        }

        layer.needRedraw()
    }

    private fun drawChoosingChartScreen(canvas: Canvas, w: Int, h : Int) {
        val header = "Choose chart type"
        canvas.drawString(header, (w - headerFont.measureTextWidth(header)) / 2, headerFont.size + 10f, headerFont, blackPaint)
        val headerH = headerFont.size + 20f // indented header height
        val chunkW = w.toFloat() / 3
        val chunkH = (h - headerH) / 3 // size of the part of the screen for one chart

        buttons.clear()
        for (chunkX in 0..2) {
            for (chunkY in 0..2) {
                val chartIdx = chunkY * 3 + chunkX
                val chartType = ChartType.values().getOrNull(chartIdx) ?: continue
                val chartName = nameOfChart[chartType]
                require(chartName != null)
                val x = chunkX * chunkW
                val y = headerH + chunkY * chunkH

                // button with name of chart
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

                // chart preview
                chartDrawFunction[chartType]?.invoke(canvas,
                    x + 10f, y + 10f, chunkW - 20f, chunkH - 50f)
            }
        }
    }

    private fun drawChartParametersScreen(canvas: Canvas) {
        require(chosenChart != null)
        buttons.clear()
        canvas.drawString("Parameters", 20f, 20f + headerFont.size, headerFont, blackPaint)
        canvas.drawString("Choose palette", 20f, 100f + headerFont.size, basicFont, blackPaint)
        val palettesY = 100f + headerFont.size + basicFont.size + 10f
        for (i in palettes.indices) {
            createButton(canvas, i * 120f + 20f, palettesY, (i + 1) * 120f, palettesY + 20f) {
                chosenPalette = i
            }
            for (pos in 0..4) {
                canvas.drawCircle(i * 120f + 20f + pos * 20f + 10f,
                    palettesY + 10f, 8f, palettes[i][pos])
            }
        }

        val showingLegendY = palettesY + 100f
        val textShowingLegend = "Show legend"
        canvas.drawString(textShowingLegend, 20f, showingLegendY + 5f,
            basicFont, blackPaint)
        val showingLegendButtonX = basicFont.measureTextWidth(textShowingLegend) + 30f
        createButton(canvas, showingLegendButtonX, showingLegendY - 10f,
            showingLegendButtonX + 20f, showingLegendY + 10f) {
            legendShowing = !legendShowing
        }
        if (legendShowing) {
            canvas.drawLine(showingLegendButtonX + 4f, showingLegendY - 6f,
                showingLegendButtonX + 10f, showingLegendY + 6f, blackPaint)
            canvas.drawLine(showingLegendButtonX + 10f, showingLegendY + 6f,
                showingLegendButtonX + 16f, showingLegendY - 6f, blackPaint)
        }

        val saveY = showingLegendY + 100f
        createButton(canvas, 20f, saveY, 220f, saveY + 40f, ::saveInFileAndExit)
        val saveText = "Save and quit"
        canvas.drawString(saveText, 20f + (200f - basicFont.measureTextWidth(saveText)) / 2,
            saveY + 25f, basicFont, blackPaint)
    }
}

class ChartRenderer(private val layer: SkiaLayer): SkiaRenderer {

    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        val contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        val w = (width / contentScale).toInt()
        val h = (height / contentScale).toInt()

        if (chosenChart != null) {
            drawChartOnScreen(canvas, w, h)
            surface.canvas.resetMatrix()
            surface.canvas.drawPaint(whitePaint)
            drawChartOnScreen(surface.canvas, w, h)
        }

        layer.needRedraw()
    }

    private fun drawChartOnScreen(canvas: Canvas, w: Int, h: Int) {
        require(chosenChart != null)
        val maxChartW : Float = when {
            legendShowing -> w.toFloat() * 5 / 6
            else -> w.toFloat()
        }
        val chartW = min(maxChartW, h.toFloat() * 4 / 3) * 9 / 10
        val chartH = chartW * 3 / 4
        chartDrawFunction[chosenChart]?.invoke(canvas,
            (maxChartW - chartW) / 2, (h - chartH) / 2, chartW, chartH)
        if (legendShowing) {
            drawLegend(canvas, maxChartW, h.toFloat() / 10,
                (w - maxChartW) * 9 / 10, h.toFloat() * 8 / 10,
                when (chosenChart) {
                    ChartType.Pie -> inputData.firstRow
                    else -> inputData.names.drop(1)
                })
        }
    }
}

fun drawLegend(canvas: Canvas, x : Float, y : Float, w: Float, h: Float, names: List<String>) {
    val len = min(names.size, 5)
    val startY = y + (h - basicFont.size * (len - 1) * 2) / 2

    val fontSize = w / 10
    val font = Font(typeface, fontSize)
    for (pos in 0 until len) {
        canvas.drawRect(Rect(x, startY + fontSize * (pos * 2 - 1),
            x + fontSize, startY + fontSize * pos * 2),
            palettes[chosenPalette][pos])
        canvas.drawString(names[pos], x + fontSize * 1.5f, startY + fontSize * pos * 2,
            font, blackPaint)
    }
}

fun calculateStep(maxValue: Int) : Pair<Int, Int> {
    for (stepFirstDigit in listOf(1, 2, 5)) {
        var stepTenPow = 1
        repeat(9) {
            val step = stepFirstDigit * stepTenPow
            val stepNum = ceil(maxValue.toFloat() / step).toInt()
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
    val dataNum = min(inputData.data.size, 5) // number of displayed data sets
    val maxValue = inputData.data.subList(0, dataNum).maxOf { it.maxOf { x -> x } }
    val (step, stepNum) = calculateStep(maxValue) // vertical grid spacing and number of segments

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth((step * stepNum).toString()) * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, stepNum, len)

    drawScale(canvas, step, stepNum, x, y, h - bottomTab, font)

    val horStepLen = (w - leftTab) / (len + 1) // length of one horizontal segment
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
    val dataNum = min(inputData.data.size, 5) // number of displayed data sets
    val prefixSums = List(inputData.firstRow.size) { mutableListOf(0) }
    for (i in inputData.firstRow.indices) {
        inputData.data.forEach {
            prefixSums[i].add(prefixSums[i].last() + it[i])
        }
    }
    val maxValue = prefixSums.maxOf { it.last() }
    val (step, stepNum) = calculateStep(maxValue) // vertical grid spacing and number of segments

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth((step * stepNum).toString()) * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, stepNum, len)

    drawScale(canvas, step, stepNum, x, y, h - bottomTab, font)

    val horStepLen = (w - leftTab) / (len + 1) // length of one horizontal segment
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
    val dataNum = min(inputData.data.size, 5) // number of displayed data sets
    val prefixSums = List(inputData.firstRow.size) { mutableListOf(0) }
    for (i in inputData.firstRow.indices) {
        inputData.data.forEach {
            prefixSums[i].add(prefixSums[i].last() + it[i])
        }
    }
    val (step, stepNum) = Pair(10, 10) // vertical grid spacing and number of segments

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth("100%") * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, stepNum, len)

    drawScale(canvas, step, stepNum, x, y, h - bottomTab, font, true)

    val horStepLen = (w - leftTab) / (len + 1) // length of one horizontal segment
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
    val dataNum = min(inputData.data.size, 5) // number of displayed data sets
    val maxValue = inputData.data.subList(0, dataNum).maxOf { it.maxOf { x -> x } }
    val (step, stepNum) = calculateStep(maxValue) // vertical grid spacing and number of segments

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth((step * stepNum).toString()) * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, stepNum, len)

    drawScale(canvas, step, stepNum, x, y, h - bottomTab, font)

    val horStepLen = (w - leftTab) / (len + 1) // length of one horizontal segment
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
    val dataNum = min(inputData.data.size, 5) // number of displayed data sets
    val prefixSums = List(inputData.firstRow.size) { mutableListOf(0) }
    for (i in inputData.firstRow.indices) {
        inputData.data.forEach {
            prefixSums[i].add(prefixSums[i].last() + it[i])
        }
    }
    val maxValue = prefixSums.maxOf { it.last() }
    val (step, stepNum) = calculateStep(maxValue) // vertical grid spacing and number of segments

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth((step * stepNum).toString()) * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, stepNum, len)

    drawScale(canvas, step, stepNum, x, y, h - bottomTab, font)

    val horStepLen = (w - leftTab) / (len + 1) // length of one horizontal segment
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
    val dataNum = min(inputData.data.size, 5) // number of displayed data sets
    val prefixSums = List(inputData.firstRow.size) { mutableListOf(0) }
    for (i in inputData.firstRow.indices) {
        inputData.data.forEach {
            prefixSums[i].add(prefixSums[i].last() + it[i])
        }
    }
    val (step, stepNum) = Pair(10, 10) // vertical grid spacing and number of segments

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val leftTab = font.measureTextWidth("100%") * 15 / 10
    val bottomTab = fontSize * 15 / 10

    val len = inputData.firstRow.size
    drawGrid(canvas, x + leftTab, y, w - leftTab, h - bottomTab, stepNum, len)

    drawScale(canvas, step, stepNum, x, y, h - bottomTab, font, true)

    val horStepLen = (w - leftTab) / (len + 1) // length of one horizontal segment
    inputData.firstRow.forEachIndexed { i, name ->
        canvas.drawString(name, x + leftTab + horStepLen * (i + 1) - font.measureTextWidth(name) / 2,
            y + h, font, blackPaint)
    }

    for (pos in dataNum - 1 downTo 0) {
        val heights = prefixSums.map { it[pos + 1].toFloat() / it.last() * (h - bottomTab) }
        val path = Path()
        path.moveTo(Point(x + leftTab + horStepLen, y + h - bottomTab))
        for (i in heights.indices)
            path.lineTo(Point(x + leftTab + horStepLen * (i + 1), y + h - bottomTab - heights[i]))
        path.lineTo(Point(x + w - horStepLen, y + h - bottomTab))
        canvas.drawPath(path, palettes[chosenPalette][pos])
    }
}

fun drawPieChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    val data = inputData.data.first()
    val dataNum = min(5, data.size) // max number of segments
    val prefixSums = mutableListOf(0)
    for (i in 0 until dataNum)
        prefixSums.add(prefixSums.last() + data[i])

    val fontSize = w / 60
    val font = Font(typeface, fontSize)
    val tab = font.measureTextWidth("100%") * 11 / 10

    val centerX = x + w / 2
    val centerY = y + h / 2
    val radius = min(w / 2, h / 2) - tab
    for (i in 0 until dataNum) {
        val startAngle = prefixSums[i].toFloat() / prefixSums.last()
        val sweepAngle = data[i].toFloat() / prefixSums.last()
        canvas.drawArc(centerX - radius, centerY - radius,
            centerX + radius, centerY + radius,
            startAngle * 360, sweepAngle * 360,
            true, palettes[chosenPalette][i])
        val midAngle = startAngle + sweepAngle / 2
        var textX = (radius * cos(midAngle * 2 * PI)).toFloat()
        var textY = (radius * sin(midAngle * 2 * PI)).toFloat()
        if (textX < 0)
            textX -= tab
        else
            textX += tab / 11
        if (textY > 0)
            textY += fontSize * 11 / 10
        else
            textY -= fontSize / 10
        canvas.drawString(round(data[i].toFloat() / prefixSums.last() * 100).toInt().toString() + "%",
            centerX + textX, centerY + textY, font, blackPaint)
    }
}

fun drawRadarChart(canvas: Canvas, x: Float, y: Float, w: Float, h: Float) {
    val dataNum = min(inputData.data.size, 5) // number of displayed data sets
    val maxValue = inputData.data.subList(0, dataNum).maxOf { it.maxOf { x -> x } }
    val (step, stepNum) = calculateStep(maxValue) // grid spacing and number of segments
    val angleStep = 2 * PI / inputData.firstRow.size

    val fontSize = w / 80
    val font = Font(typeface, fontSize)
    val tab = inputData.firstRow.maxOf { font.measureTextWidth(it) } * 11 / 10

    val centerX = x + w / 2
    val centerY = y + h / 2
    val radius = min(w / 2 - tab, h / 2 - fontSize * 3 / 2)
    // radar grid drawing
    for (i in 1..stepNum)
        canvas.drawCircle(centerX, centerY, radius / stepNum * i,
            greyPaintStroke)
    inputData.firstRow.forEachIndexed { i, name ->
        val deltaX = radius * cos(angleStep * i).toFloat()
        val deltaY = radius * sin(angleStep * i).toFloat()
        canvas.drawLine(centerX, centerY,
            centerX + deltaX, centerY + deltaY, blackPaint)
        val textLen = font.measureTextWidth(name)
        canvas.drawString(name, centerX + deltaX + when {
                    deltaX < -1e-8 -> -textLen - tab / 11
                    deltaX > 1e-8 -> tab / 11
                    else -> -textLen / 2
                },
            centerY + deltaY + when {
                deltaY > 1e-8 -> fontSize * 11 / 10
                deltaY < -1e-8 -> -fontSize / 2
                else -> fontSize / 2
            },
            font, blackPaint)
    }
    drawScale(canvas, step, stepNum, centerX - font.measureTextWidth((step * stepNum).toString()) * 11 / 10,
        centerY - radius, radius, font)

    // drawing data
    for (pos in 0 until dataNum) {
        val polygon = inputData.data[pos].mapIndexed { i, value ->
            val rad = value.toFloat() / step * radius / stepNum
            Point(centerX + rad * cos(angleStep * i).toFloat(),
                centerY + rad * sin(angleStep * i).toFloat())
        }
        canvas.drawPolygon((polygon + polygon.first()).toTypedArray(), palettes[chosenPalette][pos])
    }
}