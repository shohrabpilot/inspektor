package pro.cashkeeper.inspektor

import androidx.compose.ui.awt.ComposePanel
import pro.cashkeeper.inspektor.ui.App
import javax.swing.JFrame

actual fun openInspektor() {
    val window = JFrame().apply {
        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        title = "Inspektor"
    }
    val composePanel = ComposePanel()
    composePanel.setContent {
        App()
    }
    window.apply {
        contentPane.add(composePanel)
        setSize(1100, 700)
        isVisible = true
    }
}
