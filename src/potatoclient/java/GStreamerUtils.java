package potatoclient.java;

import com.sun.jna.Platform;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.lang.reflect.Field;

public class GStreamerUtils {
    // Common Windows GStreamer installation paths
    private static final String[] WINDOWS_GSTREAMER_PATHS = {
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
        "C:\\Program Files (x86)\\GStreamer\\1.0\\mingw_x86_64"
    };
    
    public static void configureGStreamerPaths(EventCallback callback) {
        if (!Platform.isWindows()) {
            return; // Linux/Mac paths are usually configured correctly
        }
        
        // Check if GStreamer paths are already set
        String existingPluginPath = System.getenv("GST_PLUGIN_PATH_1_0");
        if (existingPluginPath != null && !existingPluginPath.isEmpty()) {
            callback.onLog("DEBUG", "GST_PLUGIN_PATH_1_0 already set: " + existingPluginPath);
            return;
        }
        
        // Find GStreamer installation
        String gstreamerRoot = findGStreamerRoot(callback);
        if (gstreamerRoot == null) {
            callback.onLog("ERROR", "GStreamer installation not found on Windows. Please install GStreamer from https://gstreamer.freedesktop.org/download/");
            return;
        }
        
        callback.onLog("INFO", "Found GStreamer installation at: " + gstreamerRoot);
        
        // Set up paths
        String binPath = gstreamerRoot + "\\bin";
        String pluginPath = gstreamerRoot + "\\lib\\gstreamer-1.0";
        
        // Add to java.library.path
        String currentLibPath = System.getProperty("java.library.path");
        if (currentLibPath == null || !currentLibPath.contains(binPath)) {
            String newLibPath = binPath + File.pathSeparator + currentLibPath;
            System.setProperty("java.library.path", newLibPath);
            callback.onLog("DEBUG", "Updated java.library.path: " + newLibPath);
            
            // Force ClassLoader to reload java.library.path
            try {
                Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
                sysPathsField.setAccessible(true);
                sysPathsField.set(null, null);
            } catch (Exception e) {
                callback.onLog("WARN", "Could not reset java.library.path cache: " + e.getMessage());
            }
        }
        
        // Set GStreamer-specific properties
        System.setProperty("gstreamer.plugin.path", pluginPath);
        System.setProperty("jna.library.path", binPath);
        
        // Set environment variables for child processes
        try {
            ProcessBuilder pb = new ProcessBuilder();
            Map<String, String> env = pb.environment();
            env.put("GST_PLUGIN_PATH_1_0", pluginPath);
            env.put("GST_PLUGIN_SYSTEM_PATH_1_0", pluginPath);
            env.put("PATH", binPath + File.pathSeparator + env.getOrDefault("PATH", ""));
        } catch (Exception e) {
            callback.onLog("WARN", "Could not set environment variables: " + e.getMessage());
        }
        
        callback.onLog("INFO", "GStreamer paths configured for Windows");
    }
    
    private static String findGStreamerRoot(EventCallback callback) {
        // Check GSTREAMER_1_0_ROOT_MSVC_X86_64 or GSTREAMER_1_0_ROOT_MINGW_X86_64
        String[] envVars = {
            "GSTREAMER_1_0_ROOT_MSVC_X86_64",
            "GSTREAMER_1_0_ROOT_MINGW_X86_64",
            "GSTREAMER_1_0_ROOT_X86_64"
        };
        
        for (String envVar : envVars) {
            String path = System.getenv(envVar);
            if (path != null && !path.isEmpty() && Files.exists(Paths.get(path))) {
                callback.onLog("DEBUG", "Found GStreamer via " + envVar + ": " + path);
                return path;
            }
        }
        
        // Check common installation paths
        for (String path : WINDOWS_GSTREAMER_PATHS) {
            if (Files.exists(Paths.get(path)) && 
                Files.exists(Paths.get(path, "bin", "gstreamer-1.0.dll"))) {
                callback.onLog("DEBUG", "Found GStreamer at common path: " + path);
                return path;
            }
        }
        
        return null;
    }
    
    public interface EventCallback {
        void onLog(String level, String message);
    }
}