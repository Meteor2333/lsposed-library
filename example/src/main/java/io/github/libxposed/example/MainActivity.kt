package io.github.libxposed.example

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import io.github.libxposed.example.databinding.ActivityMainBinding
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.callback.ScopeEventCallback
import java.io.FileWriter
import kotlin.random.Random

@SuppressLint("SetTextI18n")
class MainActivity : Activity() {
    private lateinit var binding: ActivityMainBinding

    private val mCallback = object : ScopeEventCallback() {
        override fun onPrompted(packageName: String) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "onPrompted: $packageName",
                    Toast.LENGTH_SHORT
                ).show()
                refreshScopes()
            }
        }

        override fun onApproved(packageName: String) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "onApproved: $packageName",
                    Toast.LENGTH_SHORT
                ).show()
                refreshScopes()
            }
        }

        override fun onFailed(message: String) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "onFailed: $message",
                    Toast.LENGTH_SHORT
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

        binding.requestScope.setOnClickListener {
            XposedService.requestScope("com.android.settings", mCallback)
        }
        binding.randomPrefs.setOnClickListener {
            val prefs = XposedService.getRemotePreferences("test")
            val old = prefs.getInt("test", -1)
            val new = Random.nextInt()
            Toast.makeText(
                this@MainActivity,
                "$old -> $new",
                Toast.LENGTH_SHORT
            ).show()
            prefs.edit().putInt("test", new).apply()
        }
        binding.remoteFile.setOnClickListener {
            runCatching {
                XposedService.openRemoteFile("test.txt").use { pfd ->
                    FileWriter(pfd.fileDescriptor).use {
                        it.append("Hello World!")
                    }
                }
            }.onSuccess {
                Toast.makeText(
                    this@MainActivity,
                    "Success",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure {
                Toast.makeText(
                    this@MainActivity,
                    "Failure",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun refreshScopes() {
        binding.scopes.text = "Scopes: ${runCatching {
            XposedService.getScopes().joinToString("\n", "\n")
        }.getOrDefault("unknown")}"
    }
}
