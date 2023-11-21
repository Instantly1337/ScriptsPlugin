
package net.minusmc.scriptsplugin

import jdk.internal.dynalink.beans.StaticClass
import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import jdk.nashorn.api.scripting.ScriptUtils
import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.features.command.Command
import net.minusmc.minusbounce.features.module.Module
import net.minusmc.scriptsplugin.api.*
import net.minusmc.scriptsplugin.api.global.Chat
import net.minusmc.scriptsplugin.api.global.Item
import net.minusmc.scriptsplugin.api.global.Setting
import net.minusmc.minusbounce.utils.ClientUtils
import net.minecraft.client.Minecraft
import java.io.File
import java.util.*
import java.util.function.Function
import javax.script.ScriptEngine
import kotlin.collections.HashMap

class Script(val scriptFile: File) {

    private val scriptEngine: ScriptEngine
    private var scriptText = scriptFile.readText()

    // Script information
    lateinit var scriptName: String
    lateinit var scriptVersion: String
    lateinit var scriptAuthors: Array<String>

    private var state = false
    private val events = HashMap<String, JSObject>()
    private val registeredModules = mutableListOf<Module>()
    private val registeredCommands = mutableListOf<Command>()

    private var apiType = ""

    init {
        recognizeAPI()

        scriptText.replace("Java\\.type\\('.+net\\.ccbluex\\.liquidbounce".toRegex(), "Java.type('net.minusmc.minusbounce")
        scriptText.replace("Java\\.type\\(\".+net\\.ccbluex\\.liquidbounce".toRegex(), "Java.type(\"net.minusmc.minusbounce")

        val engineFlags = getMagicComment("engine_flags")?.split(",")?.toTypedArray() ?: emptyArray()
        scriptEngine = NashornScriptEngineFactory().getScriptEngine(*engineFlags)

        // Global classes
        scriptEngine.put("Chat", StaticClass.forClass(Chat::class.java))
        scriptEngine.put("Setting", StaticClass.forClass(Setting::class.java))
        scriptEngine.put("Item", StaticClass.forClass(Item::class.java))

        // Global instances
        scriptEngine.put("mc", Minecraft.getMinecraft())
        scriptEngine.put("moduleManager", MinusBounce.moduleManager)
        scriptEngine.put("commandManager", MinusBounce.commandManager)
        scriptEngine.put("scriptManager", ScriptsPlugin.scriptManager)

        // Global functions
        scriptEngine.put("registerScript", RegisterScript())

        if (apiType.equals("corelib", true)) {
            supportCoreLibScripts()
        } else if (apiType.equals("legacy", true)) {
            supportLegacyScripts()
        }

        scriptEngine.eval(scriptText)

        callEvent("load")
    }

    /**
     * Recognize API Scripts
     */

    private fun recognizeAPI() {
        if (scriptText.contains("registerScript".toRegex())) {
            apiType = "corelib"
        } else if (scriptText.contains("var scriptName=".toRegex())) {
            apiType = "legacy"
        }
    }

    @Suppress("UNCHECKED_CAST")
    inner class RegisterScript : Function<JSObject, Script> {
        /**
         * Global function 'registerScript' which is called to register a script.
         * @param scriptObject JavaScript object containing information about the script.
         * @return The instance of this script.
         */
        override fun apply(scriptObject: JSObject): Script {
            scriptName = scriptObject.getMember("name") as String
            scriptVersion = scriptObject.getMember("version") as String
            scriptAuthors = ScriptUtils.convert(scriptObject.getMember("authors"), Array<String>::class.java) as Array<String>

            return this@Script
        }
    }

    /**
     * Registers a new script module.
     * @param moduleObject JavaScript object containing information about the module.
     * @param callback JavaScript function to which the corresponding instance of [ScriptModule] is passed.
     * @see ScriptModule
     */
    @Suppress("unused")
    fun registerModule(moduleObject: JSObject, callback: JSObject) {
        val module = ScriptModule(moduleObject)
        MinusBounce.moduleManager.registerModule(module)
        registeredModules += module
        callback.call(moduleObject, module)
    }

    /**
     * Registers a new script command.
     * @param commandObject JavaScript object containing information about the command.
     * @param callback JavaScript function to which the corresponding instance of [ScriptCommand] is passed.
     * @see ScriptCommand
     */
    @Suppress("unused")
    fun registerCommand(commandObject: JSObject, callback: JSObject) {
        val command = ScriptCommand(commandObject)
        MinusBounce.commandManager.registerCommand(command)
        registeredCommands += command
        callback.call(commandObject, command)
    }

    /**
     * Registers a new creative inventory tab.
     * @param tabObject JavaScript object containing information about the tab.
     * @see ScriptTab
     */
    @Suppress("unused")
    fun registerTab(tabObject: JSObject) {
        ScriptTab(tabObject)
    }

    /**
     * Gets the value of a magic comment from the script. Used for specifying additional information about the script.
     * @param name Name of the comment.
     * @return Value of the comment.
     */
    private fun getMagicComment(name: String): String? {
        val magicPrefix = "///"

        scriptText.lines().forEach {
            if (!it.startsWith(magicPrefix)) return null

            val commentData = it.substring(magicPrefix.length).split("=", limit = 2)
            if (commentData.first().trim() == name) {
                return commentData.last().trim()
            }
        }

        return null
    }

    /**
     * Adds support for scripts made for LiquidBounce's original script API.
     */
    private fun supportLegacyScripts() {
        if (getMagicComment("api_version") != "2") {
            ClientUtils.logger.info("[ScriptAPI] Running script '${scriptFile.name}' with legacy support.")
            val legacyScript = MinusBounce::class.java.getResource("/assets/minecraft/scriptsplugin/legacyapi.js").readText()
            scriptEngine.eval(legacyScript)
        }
    }

    private fun supportCoreLibScripts() {
        ClientUtils.logger.info("[ScriptAPI] Running script '${scriptFile.name}' with CoreLibAPI")
        val legacyScript = MinusBounce::class.java.getResource("/assets/minecraft/scriptsplugin/corelibapi.js").readText()
        scriptEngine.eval(legacyScript)
    }

    /**
     * Called from inside the script to register a new event handler.
     * @param eventName Name of the event.
     * @param handler JavaScript function used to handle the event.
     */
    fun on(eventName: String, handler: JSObject) {
        events[eventName] = handler
    }

    /**
     * Called when the client enables the script.
     */
    fun onEnable() {
        if (state) return

        callEvent("enable")
        state = true
    }

    /**
     * Called when the client disables the script. Handles unregistering all modules and commands
     * created with this script.
     */
    fun onDisable() {
        if (!state) return

        registeredModules.forEach { MinusBounce.moduleManager.unregisterModule(it) }
        registeredCommands.forEach { MinusBounce.commandManager.unregisterCommand(it) }

        callEvent("disable")
        state = false
    }

    /**
     * Imports another JavaScript file into the context of this script.
     * @param scriptFile Path to the file to be imported.
     */
    fun import(scriptFile: String) {
        scriptEngine.eval(File(ScriptsPlugin.scriptManager.scriptsFolder, scriptFile).readText())
    }

    /**
     * Calls the handler of a registered event.
     * @param eventName Name of the event to be called.
     */
    private fun callEvent(eventName: String) {
        try {
            events[eventName]?.call(null)
        } catch (throwable: Throwable) {
            ClientUtils.logger.error("[ScriptAPI] Exception in script '$scriptName'!", throwable)
        }
    }
}