package com.example

import android.app.Application
import android.system.Os
import java.io.File

class NoteApp : Application() {

  companion object {
    init {
      try {
        Os.setenv("LIBGL_ALWAYS_SOFTWARE", "1", true)
        Os.setenv("MESA_LOADER_DRIVER_OVERRIDE", "swrast", true)
        Os.setenv("MESA_DEBUG", "silent", true)
        Os.setenv("MESA_LOG_FILE", "/dev/null", true)
        Os.setenv("LIBGL_DEBUG", "quiet", true)
      } catch (_: Throwable) {
        // Silently ignore if not supported
      }
    }
  }

  override fun onCreate() {
    super.onCreate()
    try {
      val webViewDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
      File(webViewDir, "js").mkdirs()
      File(webViewDir, "wasm").mkdirs()
    } catch (_: Throwable) {
      // Ignore cache dir setup
    }
  }
}
