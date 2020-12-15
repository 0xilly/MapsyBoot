package net.minecraftforge.mapsy.service;

import net.minecraftforge.mapsy.util.Utils;
import net.minecraftforge.mapsy.util.download.DownloadAction;
import net.minecraftforge.mapsy.util.minecraft.VersionInfoJson;
import net.minecraftforge.mapsy.util.minecraft.VersionManifestJson;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by covers1624 on 15/12/20.
 */
@Service
public class VersionCacheService {

    public static final VersionDownload CLIENT_JAR_DOWNLOAD = new VersionDownload("client", "jar");
    public static final VersionDownload CLIENT_MAPPINGS_DOWNLOAD = new VersionDownload("client_mappings", "txt");
    public static final VersionDownload SERVER_JAR_DOWNLOAD = new VersionDownload("server", "jar");
    public static final VersionDownload SERVER_MAPPINGS_DOWNLOAD = new VersionDownload("server_mappings", "txt");

    private static final String VERSION_MANIFEST = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    private final Path cacheDir = Paths.get("./version_cache");

    private final Map<String, VersionInfoJson> versionCache = new HashMap<>();
    private VersionManifestJson manifestCache = null;

    public VersionManifestJson getManifest() {
        Path manifestFile = cacheDir.resolve("version_manifest.json");
        DownloadAction action = new DownloadAction();
        try {
            action.setSrc(VERSION_MANIFEST);
            action.setDest(manifestFile);
            action.setUseETag(true);
            action.setOnlyIfModified(true);
            action.execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download versions manifest.", e);
        }

        if (manifestCache == null || !action.isUpToDate()) {
            try (BufferedReader reader = Files.newBufferedReader(manifestFile)) {
                manifestCache = Utils.gson.fromJson(reader, VersionManifestJson.class);
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse VersionManifest.", e);
            }
        }
        return manifestCache;
    }

    public VersionInfoJson getVersionJson(String version) {
        VersionManifestJson json = getManifest();

        VersionManifestJson.Version manifestVersion = json.findVersion(version)
                .orElseThrow(() -> new IllegalArgumentException("Failed to find Minecraft version: " + version));

        Path versionFile = cacheDir.resolve(version + ".json");
        DownloadAction action = new DownloadAction();
        try {
            action.setSrc(manifestVersion.url);
            action.setDest(versionFile);
            action.setUseETag(true);
            action.setOnlyIfModified(true);
            action.execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download version info.", e);
        }

        VersionInfoJson versionJson = versionCache.get(version);

        if (version == null || !action.isUpToDate()) {
            try (BufferedReader reader = Files.newBufferedReader(versionFile)) {
                versionJson = Utils.gson.fromJson(reader, VersionInfoJson.class);
                versionCache.put(version, versionJson);
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse VersionInfo.", e);
            }
        }
        return versionJson;
    }

    public Path getClientMappings(String version) {
        return getDownload(CLIENT_MAPPINGS_DOWNLOAD, version);
    }

    public Path getServerMappings(String version) {
        return getDownload(SERVER_MAPPINGS_DOWNLOAD, version);
    }

    public Path getDownload(VersionDownload versionDownload, String version) {
        return getDownload(versionDownload.name, versionDownload.extension, version);
    }

    public Path getDownload(String name, String extension, String version) {
        VersionInfoJson versionInfo = getVersionJson(version);

        Path versionFolder = cacheDir.resolve(version);
        Path file = versionFolder.resolve(name + "." + extension);

        VersionInfoJson.Download download = versionInfo.downloads.get(name);
        if (download == null) {
            throw new RuntimeException("Download '" + name + "' doesn't exist for version '" + version + "'.");
        }

        DownloadAction action = new DownloadAction();
        try {
            action.setSrc(download.url);
            action.setDest(file);
            action.setUseETag(true);
            action.setOnlyIfModified(true);
            action.execute();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download versions manifest.", e);
        }
        return file;
    }

    public static class VersionDownload {

        public final String name;
        public final String extension;

        public VersionDownload(String name, String extension) {
            this.name = name;
            this.extension = extension;
        }
    }

}
