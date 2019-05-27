package activitytracker.converter

import activitytracker.model.Diff
import activitytracker.model.DiffType
import activitytracker.model.Diffs
import name.fraser.neil.plaintext.diff_match_patch
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import kotlin.collections.HashMap

class DiffMatchPatchConverterTest {
    @Test
    fun testGetModelDiff() {
        val text1 = "lalalatatatararara"
        val text2 = "lananalalatatararattt"
        val actual = Diffs.of(text1, text2, "1", "file", "project", HashMap())
        val expected = Arrays.asList(Diff(DiffType.INSERT, 2, "nana"),
                Diff(DiffType.DELETE, 5, "at"),
                Diff(DiffType.DELETE, 16, "ra"),
                Diff(DiffType.INSERT, 18, "ttt"))
        assertEquals(expected.size, actual.diffs.size)
    }

    @Test
    fun testGetDiffMatchPatch() {
        val text1 = "lalalatatatararara"
        val text2 = "lananalalatatararattt"
        val dmp = diff_match_patch(null)
        val expected = dmp.diff_main(text1, text2)

        val diffs = Diffs(ArrayList(Arrays.asList(Diff(DiffType.INSERT, 2, "nana"),
                Diff(DiffType.DELETE, 5, "at"),
                Diff(DiffType.DELETE, 16, "ra"),
                Diff(DiffType.INSERT, 18, "ttt"))), "1", "project", 0, null, HashMap())
        val actual = DiffMatchPatchConverter.fromModelOld(diffs, text1)
        assertEquals(expected, actual)
    }

    @Test
    fun testGetDiffMatchPatchNewString() {
        val text1 = "lalalatatatararara"
        val text2 = "lananalalatatararattt"
        val dmp = diff_match_patch(null)
        val diffs = Diffs(ArrayList(Arrays.asList(Diff(DiffType.INSERT, 2, "nana"),
                Diff(DiffType.DELETE, 5, "at"),
                Diff(DiffType.DELETE, 16, "ra"),
                Diff(DiffType.INSERT, 18, "ttt"))), "1", "project", 0, null, HashMap())
        val actualNew = DiffMatchPatchConverter.fromModelNew(diffs, text2)
        assertEquals(text1, dmp.diff_text1(LinkedList(actualNew)))
    }

    @Test
    fun test() {
        val old1 = "lalalatatatararara"
        val old2 = "lalalatatata"
        val new = "lananalalatatararattt"
        val dmp = diff_match_patch(null)
        val y = ArrayList<Diffs>();

        var diff = dmp.diff_main(old1, old2)
        var x = DiffMatchPatchConverter.toModel(diff)
        y.add(Diffs(x, "1", "project", 0, "1", HashMap()))

        diff = dmp.diff_main(old2, new)
        x = DiffMatchPatchConverter.toModel(diff)
        y.add(Diffs(x, "1", "project", 0, "1", HashMap()))
        assertEquals(old1, getStartCode(y, new))
    }

    private fun getStartCode(diffs: List<Diffs>, newString: String): String {
        val dmp = diff_match_patch(null)
        var curCode = newString
        for (i in diffs.size - 1 downTo 0) {
            val diffMatchPatch = DiffMatchPatchConverter.fromModelNew(diffs[i], curCode)
            curCode = dmp.diff_text1(LinkedList(diffMatchPatch))
        }
        return curCode
    }
}