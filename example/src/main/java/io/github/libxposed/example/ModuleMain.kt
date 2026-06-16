package io.github.libxposed.example

import android.os.Build
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

    override fun onHotReloading(param: XposedModuleInterface.HotReloadingParam): Boolean {
        log("onHotReloading")
        param.setSavedInstanceState("Hello from last generation")
        return true
    }

    override fun onHotReloaded(param: XposedModuleInterface.HotReloadedParam) {
        log("onHotReloaded: ${param.processName}")
        log("savedInstanceState: " + param.savedInstanceState)
    }

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        log("onPackageLoaded: ${param.packageName}")
        log("default classloader is ${param.defaultClassLoader}")
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        log("onPackageReady: " + param.packageName)
        log("app classloader is " + param.classLoader)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            log("app acf is " + param.appComponentFactory)
        }
        log("module apk path: " + moduleApplicationInfo.sourceDir)
        log("----------")

        if (!param.isFirstPackage) return

        val prefs = getRemotePreferences("test")
        log("remote prefs: " + prefs.getInt("test", -1))
        prefs.registerOnSharedPreferenceChangeListener { _, key ->
            val value = prefs.getInt(key, 0)
            log("onSharedPreferenceChanged: $key->$value")
        }

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

    override fun onSystemServerStarting(param: XposedModuleInterface.SystemServerStartingParam) {
        log("onSystemServerStarting: ${param.classLoader}")
        log("----------")
    }
}
