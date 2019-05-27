package activitytracker

import activitytracker.model.Diffs
import com.intellij.util.ui.UIUtil
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ImageCreator {
    var offX = 3
    var lowerOffY = 5
    var offY = 5

    val font = Font("Menlo", Font.PLAIN, 24)
    fun create(lines: List<String>,
               header: String,
               conclusion: String,
               imageType: Int,
               width: Int,
               height: Int,
               scopeL: Double,
               scopeR: Double): BufferedImage {
        try {
            val img = UIUtil.createImage(width, height, imageType)
            val g2d = img.createGraphics()
            g2d.font = font
            val fm = g2d.fontMetrics



            addRectangle(img, Color.BLACK, 0, 0, width, height)
            //addRectangle(img, Color.LIGHT_GRAY, width - 21, 0, 20, height)
            drawHeaders(img, width, height, header)
            drawConclusion(img, width, height, conclusion)
            val l = if (scopeL < 0 || scopeL > 1) 0.0 else scopeL
            val r = if (scopeR < 0 || scopeR > 1 || scopeR < l) 1.0 else scopeR
            val scopeHeight = height - offY * fm.ascent - lowerOffY * fm.ascent
            var yMin = Math.round(l * scopeHeight)
            var yMax = Math.round(r * scopeHeight)
            addRectangle(img, Color.LIGHT_GRAY, width - 21, offY * fm.ascent + yMin.toInt(), 20, (yMax - yMin).toInt())





            g2d.color = Color.WHITE

            for (i in 0 until lines.size) {
                g2d.drawString(lines[i], offX * fm.ascent, offY * fm.ascent + (i + 2) * fm.ascent)
            }
            g2d.dispose()
            return img
        } catch (e: Exception) {
            println(e.message)
            throw e
        }
    }


    private fun drawConclusion(image: BufferedImage,
                               width: Int,
                               height: Int,
                               line: String) {

        val g2d = image.createGraphics()
        g2d.font = font
        val fm = g2d.fontMetrics
        addRectangle(image, Color.WHITE, 0, height - fm.ascent * 5, width, fm.ascent * 5)
        g2d.color = Color.BLACK
        g2d.drawString(line, offX * fm.ascent, height - fm.ascent * 3 + 1)
    }

    private fun drawHeaders(image: BufferedImage,
                            width: Int,
                            height: Int,
                            line: String) {
        val g2d = image.createGraphics()
        g2d.font = font
        val fm = g2d.fontMetrics
        addRectangle(image, Color.WHITE, 0, 0, width, fm.ascent * (5))
        g2d.color = Color.BLACK
        g2d.drawString(line, offX * fm.ascent, 3 * fm.ascent)
    }


    public fun addRectangle(image: BufferedImage,
                            color: Color,
                            x: Int,
                            y: Int,
                            widthRectangle: Int,
                            heightRectangle: Int): BufferedImage {
        var g2d = image.createGraphics()
        g2d.drawRect(x, y, widthRectangle, heightRectangle)
        g2d.color = color
        g2d.fillRect(x, y, widthRectangle, heightRectangle)
        g2d.dispose()
        return image
    }

    public fun addText(image: BufferedImage,
                       text: String,
                       color: Color,
                       x: Int,
                       y: Int): BufferedImage {
        var g2d = image.createGraphics()
        g2d.font = font
        g2d.color = color
        val fm = g2d.fontMetrics
        g2d.drawString(text, x, y)
        return image
    }
}