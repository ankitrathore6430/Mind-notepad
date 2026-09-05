package com.example

import android.app.Application
import android.system.Os

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
  }
}
