
package net.minusmc.scriptsplugin.ui

import net.minusmc.minusbounce.features.command.CommandManager
import net.minusmc.minusbounce.ui.font.Fonts
import net.minusmc.minusbounce.utils.misc.MiscUtils
import net.minusmc.scriptsplugin.ScriptUtils
import net.minusmc.scriptsplugin.ScriptsPlugin
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiSlot
import org.lwjgl.input.Keyboard
import java.awt.Color

class GuiScripts(private val prevGui: GuiScreen) : GuiScreen() {

    private lateinit var list: GuiList

    override fun initGui() {
        list = GuiList(this)
        list.registerScrollButtons(7, 8)
        list.elementClicked(-1, false, 0, 0)

        val j = 22
        this.buttonList.add(GuiButton(0, width - 80, height - 65, 70, 20, "Back"))
        this.buttonList.add(GuiButton(1, width - 80, j + 24, 70, 20, "Import"))
        this.buttonList.add(GuiButton(2, width - 80, j + 24 * 2, 70, 20, "Delete"))
        this.buttonList.add(GuiButton(3, width - 80, j + 24 * 3, 70, 20, "Reload"))
        this.buttonList.add(GuiButton(4, width - 80, j + 24 * 4, 70, 20, "Folder"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        list.drawScreen(mouseX, mouseY, partialTicks)

        drawCenteredString(Fonts.font40, "§9§lScripts", width / 2, 28, 0xffffff)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(prevGui)
            1 -> {
                val code = ScriptUtils.doImport()
                when (code) {
                    404 -> MiscUtils.showErrorPopup("Wrong file extension.", "The file extension has to be .js or .zip")
                    500 -> MiscUtils.showErrorPopup("Error", "Error while importing script.")
                }
            }

            2 -> {
                if (list.getSelectedSlot() == -1) return
                val code = ScriptUtils.doDelete(list.getSelectedSlot())
                when (code) {
                    500 -> MiscUtils.showErrorPopup("Error", "Error while deleting script.")
                }
            }
            3 -> {
                val code = ScriptUtils.doReload()
                when (code) {
                    500 -> MiscUtils.showErrorPopup("Error", "Error while reloading all script.")
                }
            }

            4 -> {
                val code = ScriptUtils.doOpenFolder()
                when (code) {
                    500 -> MiscUtils.showErrorPopup("Error", "Error while opening script folder.")
                }
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui)
            return
        }

        super.keyTyped(typedChar, keyCode)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        list.handleMouseInput()
    }

    private inner class GuiList(gui: GuiScreen) :
            GuiSlot(mc, gui.width, gui.height, 40, gui.height - 40, 30) {

        private var selectedSlot = 0

        override fun isSelected(id: Int) = selectedSlot == id

        internal fun getSelectedSlot() = if (selectedSlot > ScriptsPlugin.scriptManager.scripts.size) -1 else selectedSlot

        override fun getSize() = ScriptsPlugin.scriptManager.scripts.size

        public override fun elementClicked(id: Int, doubleClick: Boolean, var3: Int, var4: Int) {
            selectedSlot = id
        }

        override fun drawSlot(id: Int, x: Int, y: Int, var4: Int, var5: Int, var6: Int) {
            val script = ScriptsPlugin.scriptManager.scripts[id]
            drawCenteredString(Fonts.fontSFUI40, "§9" + script.scriptName + " §7v" + script.scriptVersion, width / 2, y + 3, Color.LIGHT_GRAY.rgb)
            drawCenteredString(Fonts.fontSFUI40, "by §c" + script.scriptAuthors.joinToString(", "), width / 2, y + 16, Color.LIGHT_GRAY.rgb)
        }

        override fun drawBackground() { }
    }
}
