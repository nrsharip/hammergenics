package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Version;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ObjectMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LibGDXUtil {
    private static final String GENERATED = "gen_";

    public static String getFieldsContents(Object obj, int depth, boolean writeToFile) {
        String out = getFieldsContents(obj, depth, writeToFile ? "" : "\n", "");

        if (writeToFile) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            String filename =
                    GENERATED + Version.VERSION + "_" + obj.getClass().getSimpleName() + "_" + depth
                            + "_" + fmt.format(LocalDateTime.now()) + ".txt";

            FileHandle fileHandle = Gdx.files.local(filename);
            fileHandle.writeString(out, false);
        }

        return out;
    }

    public static String getFieldsContents(Object obj, int depth) {
        return getFieldsContents(obj, depth, "\n", "");
    }

    private static String getFieldsContents(Object obj, int depth, String start, String indent) {
        if (obj == null || depth < 0) { return null; }

        StringBuilder out = new StringBuilder(start);
        Field[] fields = obj.getClass().getFields();
        int maxFieldType = 0;
        int maxFieldName = 0;

        int maxItType = 0;
        int maxItName = 0;

        for (Field field:fields) {
            maxFieldType = Math.max(maxFieldType, field.getType().getSimpleName().length());
            maxFieldName = Math.max(maxFieldName, field.getName().length());
        }

        String fieldFormat = indent + "field %s: %" + maxFieldType + "s %-" + maxFieldName + "s = %s\n";

        for (Field field: fields) {
            try {
                Object fieldObject = field.get(obj);

                out.append(String.format(fieldFormat,
                        Modifier.isStatic(field.getModifiers()) ? "(static)" : "",
                        field.getType().getSimpleName(),
                        field.getName(),
                        (fieldObject instanceof Iterable<?>) ? "" : toString(fieldObject,indent)));

                if (!field.getType().isPrimitive()
                        && !(fieldObject instanceof Vector3)
                        && !(fieldObject instanceof Quaternion)
                        && !(fieldObject instanceof String)
                        && !(fieldObject instanceof Matrix4)
                ) {
                    String sub = getFieldsContents(fieldObject, depth - 1, "", indent + "\t");

                    if (sub != null) { out.append(sub); }
                }

                if (fieldObject instanceof Iterable<?>) {
                    int index = 0;

                    maxItType = 0;

                    for (Object it:(Iterable<?>) fieldObject) {
                        maxItType = Math.max(maxItType, it.getClass().getSimpleName().length());
                    }

                    String itFormat = indent + "\t" + "%3d: %" + maxItType + "s %s\n";
                    String itKeyFormat = indent + "\t\t" + "%s key = %s\n";
                    String itValueFormat = indent + "\t\t" + "%s value = %s\n";

                    for (Object it:(Iterable<?>) fieldObject) {

                        String itString;
                        String sub = null;

                        Object key = null;
                        Object value = null;
                        String subKey = null;
                        String subValue = null;

                        if (it instanceof ObjectMap.Entry) {
                            key = ((ObjectMap.Entry<?, ?>) it).key;
                            value = ((ObjectMap.Entry<?, ?>) it).value;
                        }

                        if (key != null || value != null) {
                            itString = "";
                            subKey = getFieldsContents(key, depth - 1, "", indent + "\t\t");
                            subValue = getFieldsContents(value, depth - 1, "", indent + "\t\t");
                        } else {
                            itString = toString(it, indent);
                            sub = getFieldsContents(it, depth - 1, "", indent + "\t\t");
                        }

                        out.append(String.format(itFormat, index++, it.getClass().getSimpleName(), itString));

                        if (key != null) {
                            out.append(String.format(itKeyFormat, key.getClass().getSimpleName(), toString(key, indent)));
                        }
                        if (subKey != null) { out.append(subKey); }

                        if (value != null) {
                            out.append(String.format(itValueFormat, value.getClass().getSimpleName(), toString(value, indent)));
                        }
                        if (subValue != null) { out.append(subValue); }

                        if (sub != null) { out.append(sub); }
                    }
                }

            } catch (Exception e) {
                out.append(String.format(fieldFormat,
                        Modifier.isStatic(field.getModifiers()) ? "(static)" : "",
                        field.getType().getSimpleName(),
                        field.getName(),
                        "undefined (exception: " + e.getMessage() + ")"));
                e.printStackTrace();
            }

        }
        return out.toString();
    }

    private static String toString(Object obj, String indent) {
        String out = String.valueOf(obj);

        if (obj instanceof Matrix4) {
            out = "\n" + indent + "\t\t\t" +
                    out.replace("\n","\n" + indent + "\t\t\t");
        }

        // in case of standard Object.toString(), e.g.
        // com.badlogic.gdx.graphics.g3d.Material@b5b544cb
        // [Ljava.lang.Object;@1c9cdff3
        out = out.replaceAll("@[0-9a-z]+","");
        return out;
    }
}
