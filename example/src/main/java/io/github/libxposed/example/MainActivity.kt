package io.github.libxposed.example

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import io.github.libxposed.example.databinding.ActivityMainBinding
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.callback.HotReloadCallback
import io.github.libxposed.service.callback.ScopeEventCallback
import java.io.FileWriter
import kotlin.random.Random

@SuppressLint("SetTextI18n")
class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding

    private val mHotReloadCallback = object : HotReloadCallback() {
        override fun onResult(status: Status, message: String?) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "$status, $message",
                    Toast.LENGTH_LONG
                ).show()
                refreshRunningProcesses()
            }
        }
    }
    private val mScopeEventCallback = object : ScopeEventCallback() {
        override fun onPrompted(packageName: String) {
            onCallback("onPrompted\n$packageName")
        }

        override fun onApproved(packageName: String) {
            onCallback("onApproved\n$packageName")
        }

        override fun onFailed(message: String) {
            onCallback("onFailed\n$message")
        }

        private fun onCallback(message: String) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                refreshScopes()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.binderStatus.text = "Binder status: ${if (XposedService.isAvailable()) "Available" else "Unavailable"}"
        binding.apiVersion.text = "API version: ${runCatching { XposedService.getApiVersion() }.getOrDefault("unknown")}"
        binding.frameworkName.text = "Framework name: ${runCatching { XposedService.getFrameworkName() }.getOrDefault("unknown")}"
        binding.frameworkVersion.text = "Framework version: ${runCatching { XposedService.getFrameworkVersion() }.getOrDefault("unknown")}"
        binding.frameworkVersionCode.text = "Framework version code: ${runCatching { XposedService.getFrameworkVersionCode() }.getOrDefault("unknown")}"
        refreshScopes()
        refreshRunningProcesses()
        refreshRemoteFiles()

        binding.hotReload.setOnClickListener {
            if (XposedService.getApiVersion() < 102) {
                Toast.makeText(
                    this@MainActivity,
                    "Hot reload is unavailable",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            XposedService.getRunningProcesses().forEach {
                XposedService.hotReloadModule(it, null, mHotReloadCallback)
            }
        }
        binding.requestScope.setOnClickListener {
            execute(false) {
                XposedService.requestScope("com.android.settings", mScopeEventCallback)
            }
            refreshScopes()
        }
        binding.removeScope.setOnClickListener {
            execute {
                XposedService.removeScope("com.android.settings")
            }
            refreshScopes()
        }
        binding.randomPrefs.setOnClickListener {
            execute(false) {
                val prefs = XposedService.getRemotePreferences("test")
                val old = prefs.getInt("test", -1)
                val new = Random.nextInt()
                prefs.edit().putInt("test", new).apply()
                Toast.makeText(
                    this@MainActivity,
                    "$old -> $new",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.deletePrefs.setOnClickListener {
            execute {
                XposedService.deleteRemotePreferences("test")
            }
        }
        binding.writeRemoteFile.setOnClickListener {
            execute {
                XposedService.openRemoteFile("test.txt").use { pfd ->
                    FileWriter(pfd.fileDescriptor).use {
                        it.append("Hello World!")
                    }
                }
            }
            refreshRemoteFiles()
        }
        binding.deleteRemoteFile.setOnClickListener {
            execute {
                XposedService.deleteRemoteFile("test.txt")
            }
            refreshRemoteFiles()
        }
    }

    private fun refreshScopes() {
        binding.scopes.text = "Scopes: ${runCatching {
            XposedService.getScopes().joinToString("\n", "\n")
        }.getOrDefault("unknown")}"
    }

    private fun refreshRunningProcesses() {
        binding.runningProcesses.text = "RunningProcesses: ${runCatching {
            if (XposedService.getApiVersion() < 102) "unavailable"
            else XposedService.getRunningProcesses().joinToString("\n", "\n") { it.processName }
        }.getOrDefault("unknown")}"
    }

    private fun refreshRemoteFiles() {
        binding.remoteFiles.text = "RemoteFiles: ${runCatching {
            XposedService.getRemoteFiles().joinToString("\n", "\n")
        }.getOrDefault("unknown")}"
    }

    private fun execute(showToast: Boolean = true, block: () -> Unit) {
        runCatching {
            block()
        }.onSuccess {
            if (!showToast) return
            Toast.makeText(
                this@MainActivity,
                "Success",
                Toast.LENGTH_SHORT
            ).show()
        }.onFailure {
            it.printStackTrace()
            Toast.makeText(
                this@MainActivity,
                "Failure",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
