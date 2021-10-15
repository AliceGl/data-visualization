import org.jetbrains.skija.*

val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
val headerFont = Font(typeface, 40f)
val basicFont = Font(typeface, 10f)

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

fun makePaint(colorGiven: Int): Paint = Paint().apply {
    color = colorGiven
    mode = PaintMode.FILL
    strokeWidth = 1f
}

typealias Palette = List<Paint>
val palettes : List<Palette> = listOf(
    listOf(
        makePaint(0xff8C3746.toInt()),
        makePaint(0xff6380A6.toInt()),
        makePaint(0xff400A14.toInt()),
        makePaint(0xff306C73.toInt()),
        makePaint(0xff6D7340.toInt()),
    )
)