package me.jetby.libb.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class RawUtil {

    @Nullable
    public static String getResult(String link) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(link).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) sb.append(line);
                return sb.toString().trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static String getResult(String link, int timeout) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(link).openConnection();
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) sb.append(line);
                return sb.toString().trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static @NotNull File getFile(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            File pluginDir = new File("plugins");
            if (!pluginDir.exists()) pluginDir.mkdirs();

            return getFile(url, pluginDir, connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static @NotNull File getFile(URL url, File pluginDir, HttpURLConnection connection) {
        String fileName = new File(url.getPath()).getName();
        if (!fileName.endsWith(".jar")) fileName += ".jar";

        File file = new File(pluginDir, fileName);

        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
}
