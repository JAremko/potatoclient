package potatoclient.kotlin

import com.sun.jna.Platform
import java.io.File
import java.lang.reflect.Field
import java.nio.file.Files
import java.nio.file.Paths

object GStreamerUtils {
    // Common Windows GStreamer installation paths
    private val WINDOWS_GSTREAMER_PATHS =
        arrayOf(
            // GStreamer 1.26+ default location
            "C:\\Program Files\\gstreamer\\1.0\\msvc_x86_64",
            "C:\\Program Files\\gstreamer\\1.0\\mingw_x86_64",
            // Legacy locations (pre-1.26)
            "C:\\gstreamer\\1.0\\msvc_x86_64",
            "C:\\gstreamer\\1.0\\mingw_x86_64",
            // Other common locations
            "C:\\Program Files\\GStreamer\\1.0\\msvc_x86_64",
            "C:\\Program Files\\GStreamer\\1.0\\mingw_x86_64",
            "C:\\Program Files (x86)\\GStreamer\\1.0\\msvc_x86_64",
            "C:\\Program Files (x86)\\GStreamer\\1.0\\mingw_x86_64",
        )

    fun configureGStreamerPaths(callback: EventCallback) {
        if (!Platform.isWindows()) {
            return // Linux/Mac paths are usually configured correctly
        }

        // Check if GStreamer paths are already set
        val existingPluginPath = System.getenv("GST_PLUGIN_PATH_1_0")
        if (!existingPluginPath.isNullOrEmpty()) {
            callback.onLog("DEBUG", "GST_PLUGIN_PATH_1_0 already set: $existingPluginPath")
            return
        }

        // Find GStreamer installation
        val gstreamerRoot = findGStreamerRoot(callback)
        if (gstreamerRoot == null) {
            callback.onLog(
                "ERROR",
                "GStreamer installation not found on Windows. Please install GStreamer from https://gstreamer.freedesktop.org/download/",
            )
            return
        }

        callback.onLog("INFO", "Found GStreamer installation at: $gstreamerRoot")

        // Set up paths
        val binPath = "$gstreamerRoot\\bin"
        val pluginPath = "$gstreamerRoot\\lib\\gstreamer-1.0"

        // Add to java.library.path
        val currentLibPath = System.getProperty("java.library.path")
        if (currentLibPath == null || !currentLibPath.contains(binPath)) {
            val newLibPath = "$binPath${File.pathSeparator}$currentLibPath"
            System.setProperty("java.library.path", newLibPath)
            callback.onLog("DEBUG", "Updated java.library.path: $newLibPath")

            // Force ClassLoader to reload java.library.path
            try {
                val sysPathsField: Field = ClassLoader::class.java.getDeclaredField("sys_paths")
                sysPathsField.isAccessible = true
                sysPathsField.set(null, null)
            } catch (e: Exception) {
                callback.onLog("WARN", "Could not reset java.library.path cache: ${e.message}")
            }
        }

        // Set GStreamer-specific properties
        System.setProperty("gstreamer.plugin.path", pluginPath)
        System.setProperty("jna.library.path", binPath)

        // Set environment variables for child processes
        try {
            val pb = ProcessBuilder()
            val env = pb.environment()
            env["GST_PLUGIN_PATH_1_0"] = pluginPath
            env["GST_PLUGIN_SYSTEM_PATH_1_0"] = pluginPath
            env["PATH"] = "$binPath${File.pathSeparator}${env.getOrDefault("PATH", "")}"
        } catch (e: Exception) {
            callback.onLog("WARN", "Could not set environment variables: ${e.message}")
        }

        callback.onLog("INFO", "GStreamer paths configured for Windows")
    }

    private fun findGStreamerRoot(callback: EventCallback): String? {
        // Check GSTREAMER_1_0_ROOT_MSVC_X86_64 or GSTREAMER_1_0_ROOT_MINGW_X86_64
        val envVars =
            arrayOf(
                "GSTREAMER_1_0_ROOT_MSVC_X86_64",
                "GSTREAMER_1_0_ROOT_MINGW_X86_64",
                "GSTREAMER_1_0_ROOT_X86_64",
            )

        for (envVar in envVars) {
            val path = System.getenv(envVar)
            if (!path.isNullOrEmpty() && Files.exists(Paths.get(path))) {
                callback.onLog("DEBUG", "Found GStreamer via $envVar: $path")
                return path
            }
        }

        // Check common installation paths
        for (path in WINDOWS_GSTREAMER_PATHS) {
            if (Files.exists(Paths.get(path)) &&
                Files.exists(Paths.get(path, "bin", "gstreamer-1.0.dll"))
            ) {
                callback.onLog("DEBUG", "Found GStreamer at common path: $path")
                return path
            }
        }

        return null
    }

    interface EventCallback {
        fun onLog(
            level: String,
            message: String,
        )
    }
}
