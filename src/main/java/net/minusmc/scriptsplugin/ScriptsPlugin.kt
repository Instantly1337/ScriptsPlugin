package net.minusmc.scriptsplugin

import net.minusmc.minusbounce.event.EventTarget
import net.minusmc.minusbounce.event.ReloadClientEvent
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.plugin.Plugin
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.scriptsplugin.features.commands.ScriptManagerCommand
import net.minusmc.scriptsplugin.remapper.Remapper.loadSrg
import net.minusmc.scriptsplugin.ui.GuiScripts
import java.io.File

object ScriptsPlugin: Plugin(name = "ScriptsPlugin", version = "dev") {

    lateinit var scriptManager: ScriptManager
    val scriptsDir = File(MinusBounce.fileManager.dir, "scripts")

	override fun init() {
        MinusBounce.mainMenuButton.add("Scripts", GuiScripts::class.java)
        if(!scriptsDir.exists()) scriptsDir.mkdir()

		try {
            loadSrg()
            scriptManager = ScriptManager()
            scriptManager.loadScripts()
            scriptManager.enableScripts()
        } catch (throwable: Throwable) {
            ClientUtils.getLogger().error("Failed to load scripts.", throwable)
        }
	}

    override fun registerCommands() {
        MinusBounce.commandManager.registerCommand(ScriptManagerCommand())
    }

    @EventTarget
    fun onReloadClient(event: ReloadClientEvent) {
        scriptManager.disableScripts()
        scriptManager.unloadScripts()
        scriptManager.loadScripts()
        scriptManager.enableScripts()
    }
}