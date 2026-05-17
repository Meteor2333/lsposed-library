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
        override fun onApproved(packageName: String) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "onApproved: $packageName",
                    Toast.LENGTH_SHORT
                ).show()
                binding.scope.text = "Scopes: " + XposedService.getScopes()
            }
        }

        override fun onFailed(message: String) {
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "onFailed: $message",
                    Toast.LENGTH_SHORT
                ).show()
                binding.scope.text = "Scopes: " + XposedService.getScopes()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.binder.text = "Binder acquired"
        binding.api.text = "API " + XposedService.getApiVersion()
        binding.framework.text = "Framework " + XposedService.getFrameworkName()
        binding.frameworkVersion.text = "Framework version " + XposedService.getFrameworkVersion()
        binding.frameworkVersionCode.text = "Framework version code " + XposedService.getFrameworkVersionCode()
        binding.scope.text = "Scopes: " + XposedService.getScopes()
        binding.requestScope.setOnClickListener {
            XposedService.requestScope("com.android.settings", mCallback)
        }
        binding.randomPrefs.setOnClickListener {
            val prefs = XposedService.getRemotePreferences("test")
            val old = prefs.getInt("test", -1)
            val new = Random.nextInt()
            Toast.makeText(this@MainActivity, "$old -> $new", Toast.LENGTH_SHORT).show()
            prefs.edit()?.putInt("test", new)?.apply()
        }
        binding.remoteFile.setOnClickListener {
            XposedService.openRemoteFile("test.txt").use { pfd ->
                FileWriter(pfd.fileDescriptor).use {
                    it.append("Hello World!")
                }
            }
        }
    }
}
