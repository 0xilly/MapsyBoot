package net.minecraftforge.mapsy.util;

import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.covers1624.quack.gson.HashCodeAdapter;
import net.covers1624.quack.gson.LowerCaseEnumAdapterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static net.covers1624.quack.io.IOUtils.toBytes;

/**
 * Created by covers1624 on 15/12/20.
 */
@SuppressWarnings ("UnstableApiUsage")
public class Utils {

    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(HashCode.class, new HashCodeAdapter())//
            .registerTypeAdapterFactory(new LowerCaseEnumAdapterFactory()).create();

    public static Map<String, byte[]> loadZip(InputStream is) throws IOException {
        Map<String, byte[]> files = new HashMap<>();
        try (ZipInputStream zin = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                files.put(entry.getName(), toBytes(zin));
            }
        }
        return files;
    }
}
