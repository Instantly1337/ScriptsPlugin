package net.minusmc.scriptsplugin

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.ReloadClientEvent
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.plugin.Plugin
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minusmc.scriptsplugin.features.commands.ScriptManagerCommand
import net.minusmc.scriptsplugin.remapper.Remapper.loadSrg

object ScriptsPlugin: Plugin(name = "ScriptsPlugin", version = "dev") {

    lateinit var scriptManager: ScriptManager
    val scriptsDir = File(LiquidBounce.fileManager.dir, "scripts");

	override fun init() {
        if(!scriptsDir.exists())
            scriptsDir.mkdir();

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
        LiquidBounce.commandManager.registerCommand(ScriptManagerCommand())
    }

    @EventTarget
    fun onReloadClient(event: ReloadClientEvent) {
        scriptManager.disableScripts()
        scriptManager.unloadScripts()
        scriptManager.loadScripts()
        scriptManager.enableScripts()
    }
}