package activitytracker

import activitytracker.converter.DiffMatchPatchConverter
import activitytracker.model.Diffs
import com.xuggle.mediatool.ToolFactory
import com.xuggle.xuggler.ICodec
import name.fraser.neil.plaintext.diff_match_patch
import java.awt.Color
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

class VideoCreator(val curCode: String,
                   val filePath: String?) {
    private val defaultFilePathForVideo = "activity_tracker.mp4"
    private val imageCreator = ImageCreator()

    private val screenBounds = Toolkit.getDefaultToolkit().screenSize
    private val width = screenBounds.width
    private val height = screenBounds.width
    private val min5 = 300000
    private val min = 60000
    private val hour = 3600000
    private var off = 0L
    private var sec = 1000
    private var lastTs = 0L
    private var acceleration = 2.0
    private var lastPasteTs = -1L
    private var filePathForVideo = defaultFilePathForVideo

    init {
        if (filePath != null && !filePath.isEmpty()) {
            try {
                val file = File(filePath)
                file.deleteOnExit()
                file.createNewFile()
                filePathForVideo = filePath
            } catch (e: Throwable) {
                println(e.message)
            }
        }

    }

    fun create(diffs1: List<Diffs>) {
        val writer = ToolFactory.makeWriter(filePathForVideo)
        val diffs = diffs1.stream()
                .sorted { o1, o2 -> (o1.timestamp - o2.timestamp).toInt() }
                .collect(Collectors.toList())
        val old = getStartCode(diffs, curCode)
        val textRedactor = TextRedactor(old)
        if (diffs.isEmpty()) {
            return
        }
        val screenBounds = Toolkit.getDefaultToolkit().screenSize
        writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, screenBounds.width / 2, screenBounds.height / 2)
        val firstDiffOpt = diffs.stream().min { o1, o2 -> (o1.timestamp - o2.timestamp).toInt() }
        val minTs = if (firstDiffOpt.isPresent) firstDiffOpt.get().timestamp else diffs[0].timestamp
        val lastPasteTs = -1

        for (i in 0 until diffs.size) {
            val diff = diffs[i]
            val proc = getMA(diffs, i, off)
            val date = Date(diff.timestamp)
            val text = textRedactor.getText(diff, width, height)
            val header = "File: ${diff.fileName}     Project: ${diff.projectName}"
            val conclusion = "Date: $date"
            val x = textRedactor.getScope()
            var image = imageCreator.create(
                    text,
                    header,
                    conclusion,
                    BufferedImage.TYPE_3BYTE_BGR,
                    screenBounds.width,
                    screenBounds.height,
                    x.key,
                    x.value)
            val color = when {
                proc > 40 -> Color.RED
                proc < 20 -> Color.GREEN
                else -> Color.YELLOW
            }
            image = imageCreator.addRectangle(image, color, 0, 0, 30, 30)

            val ts = Math.round((diff.timestamp - minTs) / acceleration)
            lastTs = ts - off
            drawPasteRect(diff.action, ts - off, image)
            writer.encodeVideo(0, image, ts - off, TimeUnit.MILLISECONDS)
            if (dist(diffs, i) > 2 * min) {
                val d = dist(diffs, i)
                val infoText = if (d > hour) {
                    "${d / hour} hours ${(d % hour) / min} min later"
                } else {
                    "${d / min} min later"
                }

                image = imageCreator.addRectangle(image, Color.LIGHT_GRAY, 2 * width / 3, height / 9,
                        width / 4, 2 * height / 36)
                image = imageCreator.addText(image, infoText, Color.BLACK, 2 * width / 3 + 30, height / 9 + 30)
                writer.encodeVideo(0, image, lastTs + sec, TimeUnit.MILLISECONDS)
                off += Math.round(d / acceleration - 3 * sec)
            }
        }
        writer.close()
    }

    private fun dist(diffs: List<Diffs>, curInd: Int): Long {
        return if (curInd == diffs.size - 1) {
            0
        } else {
            diffs[curInd + 1].timestamp - diffs[curInd].timestamp
        }
    }

    private fun drawPasteRect(action: String?,
                              timestamp: Long,
                              image: BufferedImage) {
        if (action != "EditorPaste" &&
                (lastPasteTs == -1L || lastPasteTs + 2 * sec < timestamp)) {
            return
        }
        if (action == "EditorPaste") {
            lastPasteTs = timestamp
        }
        imageCreator.addRectangle(image, Color.CYAN, 2 * width / 3, height / 6,
                width / 8, 2 * height / 72)
        imageCreator.addText(image, "Paste", Color.BLACK, 2 * width / 3 + 30, height / 6 + 30)
    }

    private fun getMA(diffs: List<Diffs>, ind: Int, off: Long): Long {
        var eventCount = 0
        var pasteCount = 0
        var curInd = ind
        val minTs = diffs[curInd].timestamp - min5 - off

        while (curInd > 0) {
            if (diffs[curInd].timestamp < minTs) {
                break
            }
            eventCount++
            if (diffs[curInd].action.equals("EditorPaste")) {
                pasteCount++
            }
            curInd--
        }
        return Math.round((pasteCount * 100.0) / eventCount)
    }

    private fun getStartCode(diffs: List<Diffs>, newString: String): String {
        val dmp = diff_match_patch(null)
        var cur = newString
        for (i in diffs.size - 1 downTo 0) {
            val diffMatchPatch = DiffMatchPatchConverter.fromModelNew(diffs[i], cur)
            cur = dmp.diff_text1(LinkedList(diffMatchPatch))
        }
        return cur
    }

}