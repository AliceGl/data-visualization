import kotlin.test.*
import kotlin.io.path.createTempFile
import kotlin.io.path.pathString
import kotlin.io.path.writeLines

internal class Test1 {

    @Test
    fun testGetDataFromFile() {
        assertFailsWith<FileNotFound> { getDataFromFile("ThisIsNotAFilePath") }
        assertFailsWith<FileNotFound> { getDataFromFile("src") }

        val temp = createTempFile()
        assertFailsWith<NotEnoughData> { getDataFromFile(temp.pathString) }

        temp.writeLines(listOf("Months september october november",
                               "Income 1000 10 350",
                               "Outcome 470 3000 350"))
        assertEquals(
            getDataFromFile(temp.pathString),
            InputData(listOf("Months", "Income", "Outcome"),
                listOf("september", "october", "november"),
                listOf(listOf(1000, 10, 350), listOf(470, 3000, 350))
            )
        )

        temp.writeLines(listOf("Months september october november",
            "Income 1000 10 350",
            "Outcome 470 what 350"))
        assertFailsWith<WrongDataFormat> { getDataFromFile(temp.pathString) }

    }
}
