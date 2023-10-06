package net.minusmc.scriptsplugin

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.command.CommandManager
import net.minusmc.minusbounce.utils.misc.MiscUtils
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.ui.client.clickgui.ClickGui
import net.minusmc.scriptsplugin.ScriptsPlugin
import java.awt.Desktop
import java.util.*

object ScriptUtils {
	fun doImport(): Int {
        try {
            val file = MiscUtils.openFileChooser() ?: return 403
            if (file.name.endsWith(".js")) {
                ScriptsPlugin.scriptManager.importScript(file)

                MinusBounce.clickGui = ClickGui()
                MinusBounce.fileManager.loadConfig(MinusBounce.fileManager.clickGuiConfig)

                return 200
            }
            return 404
        } catch (t: Throwable) {
            ClientUtils.getLogger().error("Something went wrong while importing a script.", t)
            return 500
        }
        return 0
    }

    fun doDelete(scriptIndex: Int): Int {
        try {       
            val scripts = ScriptsPlugin.scriptManager.scripts

            if (scriptIndex >= scripts.size) return 501

            val script = scripts[scriptIndex]

            ScriptsPlugin.scriptManager.deleteScript(script)

            MinusBounce.clickGui = ClickGui()
            MinusBounce.fileManager.loadConfig(MinusBounce.fileManager.clickGuiConfig)
            MinusBounce.fileManager.loadConfig(MinusBounce.fileManager.hudConfig)
            return 200
        } catch (numberFormat: NumberFormatException) {
            return 403
        } catch (t: Throwable) {
            ClientUtils.getLogger().error("Something went wrong while deleting a script.", t)
            return 500
        }
        return 0
    } 

    fun doReload(): Int {
        try {
            MinusBounce.commandManager = CommandManager()
            MinusBounce.pluginManager.registerCommands()
            MinusBounce.commandManager.registerCommands()
            MinusBounce.isStarting = true
            ScriptsPlugin.scriptManager.disableScripts()
            ScriptsPlugin.scriptManager.unloadScripts()
            for(module in MinusBounce.moduleManager.modules)
                MinusBounce.moduleManager.generateCommand(module)
            ScriptsPlugin.scriptManager.loadScripts()
            ScriptsPlugin.scriptManager.enableScripts()
            MinusBounce.fileManager.loadConfig(MinusBounce.fileManager.modulesConfig)
            MinusBounce.isStarting = false
            MinusBounce.fileManager.loadConfig(MinusBounce.fileManager.valuesConfig)
            MinusBounce.clickGui = ClickGui()
            MinusBounce.fileManager.loadConfig(MinusBounce.fileManager.clickGuiConfig)
            MinusBounce.moduleManager.initModeListValues()
            return 200
        } catch (t: Throwable) {
            ClientUtils.getLogger().error("Something went wrong while reloading all scripts.", t)
            return 500
        }
        return 0
    }

    fun doOpenFolder(): Int {
        try {
            Desktop.getDesktop().open(ScriptsPlugin.scriptManager.scriptsFolder)
            return 200
        } catch (t: Throwable) {
            ClientUtils.getLogger().error("Something went wrong while trying to open your scripts folder.", t)
        	return 500
        }
        return 0
    }
}