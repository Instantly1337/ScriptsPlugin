
package net.minusmc.scriptsplugin.remapper

import net.minusmc.minusbounce.MinusBounce
import net.minusmc.minusbounce.utils.ClientUtils
import net.minusmc.minusbounce.utils.misc.HttpUtils
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * A srg remapper
 *
 * @author CCBlueX
 */
object Remapper {

    private const val srgName = "stable_22"
    private val srgFile = File(MinusBounce.fileManager.dir, "mcp-$srgName.srg")

    private val fields : HashMap<String, HashMap<String, String>> = hashMapOf()
    private val methods : HashMap<String, HashMap<String, String>> = hashMapOf()

    /**
     * Load srg
     */
    fun loadSrg() {

        // Copy file from resources to outside
        val source = this::class.java.getResource("/assets/minecraft/scriptsplugin/mcp-stable_22.srg")
        FileUtils.copyURLToFile(source, srgFile)

        // Load srg
        ClientUtils.logger.info("[Remapper] Loading srg...")
        parseSrg()
        ClientUtils.logger.info("[Remapper] Loaded srg.")
    }

    private fun parseSrg() {
        srgFile.readLines().forEach {
            val args = it.split(" ")

            when {
                it.startsWith("FD:") -> {
                    val name = args[1]
                    val srg = args[2]

                    val className = name.substring(0, name.lastIndexOf('/')).replace('/', '.')
                    val fieldName = name.substring(name.lastIndexOf('/') + 1)
                    val fieldSrg = srg.substring(srg.lastIndexOf('/') + 1)

                    if(!fields.contains(className))
                        fields[className] = hashMapOf()

                    fields[className]!![fieldSrg] = fieldName
                }

                it.startsWith("MD:") -> {
                    val name = args[1]
                    val desc = args[2]
                    val srg = args[3]

                    val className = name.substring(0, name.lastIndexOf('/')).replace('/', '.')
                    val methodName = name.substring(name.lastIndexOf('/') + 1)
                    val methodSrg = srg.substring(srg.lastIndexOf('/') + 1)

                    if(!methods.contains(className))
                        methods[className] = hashMapOf()

                    methods[className]!![methodSrg + desc] = methodName
                }
            }
        }
    }

    /**
     * Remap field
     */
    fun remapField(clazz : Class<*>, name : String) : String {
        if(!fields.containsKey(clazz.name))
            return name

        return fields[clazz.name]!!.getOrDefault(name, name)
    }

    /**
     * Remap method
     */
    fun remapMethod(clazz : Class<*>, name : String, desc : String) : String {
        if(!methods.containsKey(clazz.name))
            return name

        return methods[clazz.name]!!.getOrDefault(name + desc, name)
    }
}