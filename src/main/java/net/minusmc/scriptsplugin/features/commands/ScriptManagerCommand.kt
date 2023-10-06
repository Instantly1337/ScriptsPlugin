
package net.minusmc.scriptsplugin.features.commands

import net.minusmc.minusbounce.features.command.Command
import net.minusmc.minusbounce.features.command.CommandManager
import net.minusmc.minusbounce.ui.client.clickgui.ClickGui
import net.minusmc.scriptsplugin.ScriptUtils

class ScriptManagerCommand : Command("scriptmanager", arrayOf("scripts")) {
    /**
     * Execute commands with provided [args]
     */

    override fun execute(args: Array<String>) {
        if (args.size <= 1) {
            val scriptManager = ScriptsPlugin.scriptManager
            if (scriptManager.scripts.isNotEmpty()) {
                chat("§c§lScripts")
                scriptManager.scripts.forEachIndexed { index, script -> chat("$index: §a§l${script.scriptName} §a§lv${script.scriptVersion} §3by §a§l${script.scriptAuthors.joinToString(", ")}") }
            }
            chatSyntax("scriptmanager <import/delete/reload/folder>")
            return
        }
        when (args[1].lowercase()) {
            "import" -> {
                val code = ScriptUtils.doImport()
                when (code) {
                    200 -> chat("Successfully imported script.")
                    404 -> chat("The file extension has to be .js or .zip")
                    500 -> chat("Error while importing script.")
                }
            }
            "delete" -> {
                if (args.size <= 2) {
                    chatSyntax("scriptmanager delete <index>")
                } else {
                    val index = args[2].toInt()
                    val code = ScriptUtils.doDelete()
                    when (code) {
                        200 -> chat("Successfully deleted script.")
                        403 -> chat("$index is not number.")
                        500 -> chat("Error while deleting script.")
                        501 -> chat("Index $index is too high.")
                    }
                }
            }
            "reload" -> {
                val code = ScriptUtils.doReload()
                when (code) {
                    200 -> chat("Successfully reloaded all scripts.")
                    500 -> chat("Error while reloading all scripts.")
                }
            }
            "folder" -> {
                val code = ScriptUtils.doOpenFolder()
                when (code) {
                    200 -> chat("Successfully opened scripts folder.")
                    500 -> chat("Error while opening scripts folder.")
                }
            }
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("delete", "import", "folder", "reload")
                .filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
    }
}
