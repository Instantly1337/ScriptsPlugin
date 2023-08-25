package me.toidicakhia.exampleplugin

import net.ccbluex.liquidbounce.plugin.Plugin

class ExamplePlugin: Plugin(name = "ExamplePlugin", version = "dev") {
	override fun init() {
		println("ExamplePlugin is ready!!!")
	}
}