import org.jetbrains.skija.Font
import org.jetbrains.skija.Paint
import org.jetbrains.skija.PaintMode
import org.jetbrains.skija.Typeface

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