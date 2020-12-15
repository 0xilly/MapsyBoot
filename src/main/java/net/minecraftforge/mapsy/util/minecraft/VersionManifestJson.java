package net.minecraftforge.mapsy.util.minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by covers1624 on 5/02/19.
 */
public class VersionManifestJson {

    public Latest latest;
    public List<Version> versions = new ArrayList<>();

    public Optional<Version> findVersion(String version) {
        return versions.stream()
                .filter(v -> v.id.equalsIgnoreCase(version))
                .findFirst();
    }

    public static class Latest {

        public String release;
        public String snapshot;
    }

    public static class Version {

        public String id;
        public String type;
        public String url;
        public String time;
        public String releaseTime;
    }

}
