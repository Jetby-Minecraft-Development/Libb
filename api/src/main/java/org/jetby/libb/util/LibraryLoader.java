package org.jetby.libb.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibraryLoader {

    private static final Set<String> CACHE = new HashSet<>();

    public static synchronized void load(Plugin plugin, String cacheKey, String repo, String groupId, String artifactId, String version, String checkClass) {
        load(plugin, cacheKey, repo, List.of(new Dependency(groupId, artifactId, version, checkClass)));
    }

    public static synchronized void load(Plugin plugin, String cacheKey, String repo, Dependency dependency) {
        load(plugin, cacheKey, repo, List.of(dependency));
    }

    public static synchronized void load(Plugin plugin, String cacheKey, String repo, List<Dependency> dependencies) {
        if (CACHE.contains(cacheKey)) {

            return;
        }

        File libsFolder = new File("plugins" + File.separator + "Libb" + File.separator + "libs");
        libsFolder.mkdirs();

        ClassLoader pluginCL = plugin.getClass().getClassLoader();

        for (Dependency dependency : dependencies) {
            if (isAvailable(dependency.checkClass())) {
                plugin.getLogger().info("Found natively: " + dependency.checkClass());
                continue;
            }

            String groupPath = dependency.groupId().replace(".", "/");
            String jarName = dependency.artifactId() + "-" + dependency.version() + ".jar";
            String fullUrl = repo + groupPath + "/" + dependency.artifactId() + "/" + dependency.version() + "/" + jarName;

            try {
                File jar = downloadJar(fullUrl, libsFolder, jarName, plugin);
                injectUrl(pluginCL, jar.toURI().toURL());
                plugin.getLogger().info("Loaded library: " + jarName);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load: " + fullUrl);
                e.printStackTrace();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        CACHE.add(cacheKey);
    }

    private static void injectUrl(ClassLoader classLoader, URL url) throws Throwable {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

        Field implLookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        long offset = unsafe.staticFieldOffset(implLookup);
        MethodHandles.Lookup trustedLookup = (MethodHandles.Lookup) unsafe.getObject(MethodHandles.Lookup.class, offset);

        MethodHandle addURL = trustedLookup.findVirtual(
                URLClassLoader.class,
                "addURL",
                MethodType.methodType(void.class, URL.class)
        );
        addURL.invoke(classLoader, url);
    }

    private static boolean isAvailable(String className) {
        if (className == null) {
            return false;
        }

        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
        }

        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            String path = className.replace(".", "/") + ".class";
            if (p.getClass().getClassLoader().getResource(path) != null) {
                return true;
            }
        }

        return false;
    }

    private static File downloadJar(String link, File folder, String fileName, Plugin plugin) throws IOException {
        File output = new File(folder, fileName);
        if (output.exists()) return output;

        plugin.getLogger().info("Downloading Library: " + link);
        try (InputStream in = new URL(link).openStream();
             FileOutputStream fos = new FileOutputStream(output)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
        }
        return output;
    }

    public record Dependency(String groupId, String artifactId, String version, String checkClass) {
    }
}