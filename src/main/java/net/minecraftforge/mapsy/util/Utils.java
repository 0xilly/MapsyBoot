package net.minecraftforge.mapsy.util;

import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by covers1624 on 15/12/20.
 */
@SuppressWarnings ("UnstableApiUsage")
public class Utils {

    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(HashCode.class, new HashCodeAdapter())//
            .registerTypeAdapterFactory(new LowerCaseEnumAdapterFactory()).create();

    private static class HashCodeAdapter extends TypeAdapter<HashCode> {

        @Override
        public void write(JsonWriter out, HashCode value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            out.value(value.toString());
        }

        @Override
        public HashCode read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return HashCode.fromString(in.nextString());
        }
    }

    @SuppressWarnings ("unchecked")
    private static class LowerCaseEnumAdapterFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!type.getRawType().isEnum()) {
                return null;
            }
            Map<String, T> lookup = new HashMap<>();
            for (T e : (T[]) type.getRawType().getEnumConstants()) {
                lookup.put(e.toString().toLowerCase(Locale.ROOT), e);
            }
            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(value.toString().toLowerCase());
                    }
                }

                @Override
                public T read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    }
                    String name = in.nextString();
                    return name == null ? null : lookup.get(name.toLowerCase(Locale.ROOT));
                }
            };
        }
    }
}
