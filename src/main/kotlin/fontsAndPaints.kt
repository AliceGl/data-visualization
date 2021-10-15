import org.jetbrains.skija.*

val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
val headerFont = Font(typeface, 40f)
val basicFont = Font(typeface, 10f)

val whitePaint = Paint().apply {
    color = 0xffffffff.toInt()
    mode = PaintMode.FILL
    strokeWidth = 1f
}

val blackPaint = Paint().apply {
    color = 0xff000000.toInt()
    mode = PaintMode.FILL
    strokeWidth = 1f
}

val greyPaint = Paint().apply {
    color = 0xffcfdbd5.toInt()
    mode = PaintMode.FILL
    strokeWidth = 1f
}

val greyPaintStroke = Paint().apply {
    color = 0xffcfdbd5.toInt()
    mode = PaintMode.STROKE
    strokeWidth = 1f
}

fun makePaint(colorGiven: Int): Paint = Paint().apply {
    color = colorGiven
    mode = PaintMode.FILL
    strokeWidth = 2f
}

typealias Palette = List<Paint>
val palettes : List<Palette> = listOf(
    listOf(
        makePaint(0xff8C3746.toInt()),
        makePaint(0xff6380A6.toInt()),
        makePaint(0xff400A14.toInt()),
        makePaint(0xff306C73.toInt()),
        makePaint(0xff6D7340.toInt()),
    ),
    listOf(
        makePaint(0xffE24F6D.toInt()),
        makePaint(0xffA5D5D8.toInt()),
        makePaint(0xffF2A391.toInt()),
        makePaint(0xff00968D.toInt()),
        makePaint(0xffEEA737.toInt()),
    ),
    listOf(
        makePaint(0xff35D6B8.toInt()),
        makePaint(0xff48A1D5.toInt()),
        makePaint(0xff8C48DB.toInt()),
        makePaint(0xffC068B3.toInt()),
        makePaint(0xff2DF58B.toInt()),
    )
)