package me.toidicakhia.exampleplugin

import net.minusmc.scriptsplugin.ScriptManager
import net.minusmc.scriptsplugin.remapper.Remapper.loadSrg
import net.minusmc.scriptsplugin.features.commands.ScriptManagerCommand
import net.ccbluex.liquidbounce.plugin.Plugin
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.ReloadClientEvent

class ScriptsPlugin: Plugin(name = "ScriptsPlugin", version = "dev") {

    lateinit var scriptManager

	override fun init() {
		try {
            loadSrg()
            scriptManager = ScriptManager()
            scriptManager.loadScripts()
            scriptManager.enableScripts()
        } catch (throwable: Throwable) {
            ClientUtils.getLogger().error("Failed to load scripts.", throwable)
        }
	}

    override fun registerCommand() {
        CommandManager.registerCommand(ScriptManagerCommand())
    }

    @EventTarget
    fun onReloadClient(event: ReloadClientEvent) {
        scriptManager.disableScripts()
        scriptManager.unloadScripts()
        chat("§c§lReloading scripts...")
        scriptManager.loadScripts()
        scriptManager.enableScripts()
    }
}