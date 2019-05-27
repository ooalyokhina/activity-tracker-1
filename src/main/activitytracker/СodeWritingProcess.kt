package activitytracker

import activitytracker.log.Logger
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*


class CodeWritingProcess : AnAction("СodeWritingProcess") {
    override fun actionPerformed(e: AnActionEvent?) {
        if (e == null) {
            return
        }
        val editor = e.getData(PlatformDataKeys.EDITOR) ?: return
        val project = e.getData(PlatformDataKeys.PROJECT) ?: return

        val dw = MyDialogWrapper(project)
        val result = dw.showAndGet()
        if (result) {
            Logger.save()
            val curCode = editor.document.charsSequence.toString()
            val diffs = Logger.getLogs()
            val filePath = dw.getText()
            VideoCreator(curCode, filePath).create(diffs)
            SampleDialogWrapper().show()
        }
    }
}

class SampleDialogWrapper : DialogWrapper(true) {

    init {
        init();
        title = "Activity tracker"
    }

    override fun createCenterPanel(): JComponent? {
        val dialogPanel = JPanel(BorderLayout())
        val label = JLabel("   video saved")
        label.preferredSize = Dimension(100, 100)
        dialogPanel.add(label, BorderLayout.CENTER)
        return dialogPanel
    }

}

class MyDialogWrapper(project: Project) : DialogWrapper(true) {
    private var textField: JTextField? = null

    init {
        init()
        title = "Activity tracker"
    }

    override fun createCenterPanel(): JComponent? {
        val dialogPanel = JPanel(BorderLayout())
        dialogPanel.layout = FlowLayout(FlowLayout.LEFT)
        dialogPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        dialogPanel.add(JLabel("имя файла:"))
        val textFieldFileName = JTextField("activity_tracker.mp4", 35)
        this.textField = textFieldFileName
        dialogPanel.add(textFieldFileName)
        return dialogPanel
    }

    fun getText(): String {
        return if (textField == null) return "" else textField!!.text
    }
}




