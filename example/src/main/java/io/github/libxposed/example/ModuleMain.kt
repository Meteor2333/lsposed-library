package io.github.libxposed.example

import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface
import java.io.FileReader

class ModuleMain : XposedModule {
    companion object {
        const val TAG = "XposedExample"

        private fun log(msg: String) {
            Log.i(TAG, msg)
        }
    }

    constructor() : super()

    constructor(base: XposedInterface, param: XposedModuleInterface.ModuleLoadedParam) : super(base, param) {
        onModuleLoaded(param)
    }

    override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
        log("onModuleLoaded: ${param.processName}")
        log("framework: $frameworkName($frameworkVersion)")
        log("----------")
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        log("onPackageLoaded: ${param.packageName}")
        log("default classloader is ${param.defaultClassLoader}")

        if (!param.isFirstPackage) return

        val prefs = getRemotePreferences("test")
        log("remote prefs: ${prefs.getInt("test", -1)}")
        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            val value = prefs.getInt(key, 0)
            log("onSharedPreferenceChanged: $key->$value")
        }

        log("remote files: ${listRemoteFiles().joinToString()}")

        runCatching {
            openRemoteFile("test.txt").use {
                FileReader(it.fileDescriptor).readText()
            }
        }.onSuccess {
            log("remote file content: $it")
        }.onFailure {
            log("remote file not found")
        }

        log("----------")
    }

    override fun onSystemServerLoaded(param: XposedModuleInterface.SystemServerLoadedParam) {
        log("onSystemServerLoaded: ${param.classLoader}")
        log("----------")
    }
}
