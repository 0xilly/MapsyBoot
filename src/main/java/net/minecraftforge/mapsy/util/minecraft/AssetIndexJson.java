package net.minecraftforge.mapsy.util.minecraft;

import java.util.Map;

/**
 * Created by covers1624 on 5/02/19.
 */
public class AssetIndexJson {

    public Map<String, AssetObject> objects;
    public boolean virtual;

    public class AssetObject {

        public String hash;
        public int size;
    }
}
