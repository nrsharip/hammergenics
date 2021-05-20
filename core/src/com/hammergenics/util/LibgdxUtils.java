/*******************************************************************************
 * Copyright 2021 Nail Sharipov (sharipovn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.hammergenics.util;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Version;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.io.FileFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class LibgdxUtils {
    private static final String GENERATED = "generated/gen_";
    private static final String EXTENSION = ".txt";

    /**
     * Careful with the lambda closures...
     *
     * @return
     */
    private static String getTag() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        return stackTrace[3].getMethodName() + "->" + stackTrace[2].getMethodName();
    }

    /**
     * @param rootFileName
     * @param fileSoughtFor
     * @return
     */
    public static FileHandle fileOnPath(String rootFileName, String fileSoughtFor) {
        FileHandle rootFileHandle = Gdx.files.local(rootFileName);
        FileHandle parent = rootFileHandle;
        FileHandle soughtFileHandle = null;

        Gdx.app.debug(getTag(), "looking for '" + fileSoughtFor + "' in: " + rootFileName);
        rootLoop:
        do {
            parent = parent.parent();
            Gdx.app.debug(getTag(), parent.toString());

            for (FileHandle sub : parent.list()) {
                if (sub.toString().toLowerCase().endsWith(fileSoughtFor)) { // sub.isDirectory() &&
                    Gdx.app.debug(getTag(), "found " + fileSoughtFor + ": " + sub.toString());
                    soughtFileHandle = sub;
                    break rootLoop;
                }
            }
        } while (!parent.path().equals(parent.type() == Files.FileType.Absolute ? "/" : ""));

        return soughtFileHandle;
    }

    /**
     * @param mi
     * @return
     */
    public static String getModelInstanceInfo(ModelInstance mi) {
        // FIXME: calculateBoundingBox is a slow operation - BoundingBox object should be cached
        Vector3 dimensions = mi.calculateBoundingBox(new BoundingBox()).getDimensions(new Vector3());

        StringBuilder modelInstanceInfo = new StringBuilder("ModelInstance:\n")
                .append(String.format("Dimension X: %.5f\n", dimensions.x))
                .append(String.format("Dimension Y: %.5f\n", dimensions.y))
                .append(String.format("Dimension Z: %.5f\n", dimensions.z));

        modelInstanceInfo.append("Nodes:\n");

        for (int i = 0; i < mi.nodes.size; i++) {
            Node node = mi.nodes.get(i);
            modelInstanceInfo.append(String.format("%2d. %s\n",i, node.id));
            // TODO: see Node - there's more fields to add here
            // TODO: node.getChildren()
            for (int j = 0; j < node.parts.size; j++) {
                NodePart nodePart = node.parts.get(j);
                modelInstanceInfo.append(String.format("    %2d. NodePart\n", j));
                // TODO: see NodePart - there's more fields to add here
                modelInstanceInfo.append(String.format("        MeshPart (id: %s)\n", nodePart.meshPart.id));
            }
        }

        // https://github.com/libgdx/libgdx/wiki/Material-and-environment#materials
        // Materials are model (or modelinstance) specific. You can access them
        // - by index model.materials.get(0),
        // - by name model.getMaterial("material3") or
        // - by nodepart model.nodes.get(0).parts(0).material.
        // TODO: keep for now
        // !!! Materials are copied when creating a ModelInstance, meaning that changing the material of a ModelInstance
        // will not affect the original Model or other ModelInstances.
        modelInstanceInfo.append("Materials:\n");
        for (int i = 0; i < mi.materials.size; i++) {
            Material material = mi.materials.get(i);
            modelInstanceInfo.append(String.format("%2d. %s\n",i, material.id));
            modelInstanceInfo.append(extractAttributes(material, "", "    "));
        }

        return modelInstanceInfo.toString();
    }


    /**
     * @return
     */
    public static Array<String> getRegisteredAttributeAliases() {
        Array<String> out = new Array<>();
        // https://github.com/libgdx/libgdx/wiki/Material-and-environment#attributes
        // The Attributes class is most comparable with a Set.
        // It can contain at most one value for each attribute, just like an uniform can only be set to one value.
        // TODO: keep for now
        // !!! Theoretically both the Material and Environment can contain the same attribute,
        // the actual behavior in this scenario depends on the shader used,
        // but in most cases the Materials attribute will be used instead of the Environment attribute.
        // https://github.com/libgdx/libgdx/wiki/Material-and-environment#using-attributes

        // wow: https://www.youtube.com/watch?v=SuNNyjs9BO8
        long type = 0;
        String alias = Attribute.getAttributeAlias(1L << type);
        while (alias != null) {
            out.add(alias);
            alias = Attribute.getAttributeAlias(1L << ++type);
        }
        return out;
    }

    /**
     * @param container
     * @param start
     * @param indent
     * @return
     */
    public static String extractAttributes(Attributes container, String start, String indent) {
        if (container.size() == 0) return "";

        StringBuilder out = new StringBuilder(start);
        Class<?> grp = container.iterator().next().getClass();

        // https://github.com/libgdx/libgdx/wiki/Material-and-environment#attributes
        String alias;
        for (Attribute attr: container) {
            alias = Attribute.getAttributeAlias(attr.type);
            if (!grp.isInstance(attr)) {
                out.append("\n");
                grp = attr.getClass();
            }

            // IDEA finds 10 classes extending Attribute
            // com.badlogic.gdx.graphics.g3d.attributes:
//            if (BlendingAttribute.is(attr.type)) { }// BlendingAttribute
            if (ColorAttribute.is(attr.type)) {
                out.append(String.format(indent + "%14s %20s: %.3f %.3f %.3f %.3f \n",
                        attr.getClass().getSimpleName(),
                        alias,
                        ((ColorAttribute) attr).color.r,((ColorAttribute) attr).color.g,
                        ((ColorAttribute) attr).color.b,((ColorAttribute) attr).color.a));
                continue;
            } // ColorAttribute
//            if (CubemapAttribute.is(attr.type)) { }// CubemapAttribute
//            if (DepthTestAttribute.is(attr.type)) { }// DepthTestAttribute
            if (DirectionalLightsAttribute.is(attr.type)) {
                out.append(indent + attr.getClass().getSimpleName() + " " + DirectionalLightsAttribute.Alias + ".lights:\n");
                ((DirectionalLightsAttribute) attr).lights.forEach(light -> {
                    out.append(indent + " direction: " + light.direction + "\n");
                    out.append(String.format(indent + "     color: %.3f %.3f %.3f %.3f \n",
                            light.color.r,light.color.g,
                            light.color.b,light.color.a));
                });
                continue;
            } // DirectionalLightsAttribute
            if (((FloatAttribute.Shininess | FloatAttribute.AlphaTest) & attr.type) != 0) {
                out.append(indent + attr.getClass().getSimpleName() + " " + alias + ": " + ((FloatAttribute) attr).value + "\n");

                //Gdx.app.debug(LibGDXUtil.class.getSimpleName(), getFieldsContents(attr, 1, "", "", false));
                continue;
            } // FloatAttribute // missing is()
//            if ((IntAttribute.CullFace & attr.type) != 0) { }// IntAttribute // missing is()
            if (PointLightsAttribute.is(attr.type)) {
                out.append(indent + attr.getClass().getSimpleName() + " " + PointLightsAttribute.Alias + ".lights:\n");
                ((PointLightsAttribute) attr).lights.forEach(light -> {
                    out.append(indent + "  position: " + light.position + "\n");
                    out.append(indent + " intensity: " + light.intensity + "\n");
                    out.append(String.format(indent + "     color: %.3f %.3f %.3f %.3f \n",
                            light.color.r,light.color.g,
                            light.color.b,light.color.a));
                });
                continue;
            } // PointLightsAttribute
            if (SpotLightsAttribute.is(attr.type)) {
                out.append(indent + attr.getClass().getSimpleName() + " " + SpotLightsAttribute.Alias + ".lights:\n");
                ((SpotLightsAttribute) attr).lights.forEach(light -> {
                    out.append(indent + "    position: " + light.position + "\n");
                    out.append(indent + "   direction: " + light.direction + "\n");
                    out.append(indent + "   intensity: " + light.intensity + "\n");
                    out.append(indent + " cutoffAngle: " + light.cutoffAngle + "\n");
                    out.append(indent + "    exponent: " + light.exponent + "\n");
                    out.append(String.format(indent + "       color: %.3f %.3f %.3f %.3f \n",
                            light.color.r,light.color.g,
                            light.color.b,light.color.a));
                });
                continue;
            } // SpotLightsAttribute
            if (TextureAttribute.is(attr.type)) {
                TextureAttribute a = ((TextureAttribute) attr);
                out.append(String.format(indent + "%16s %20s:\n",
                        attr.getClass().getSimpleName(),
                        alias
                ));
                out.append(indent + " offsetU: " + a.offsetU + "\n");
                out.append(indent + " offsetV: " + a.offsetV + "\n");
                out.append(indent + "  scaleU: " + a.scaleU + "\n");
                out.append(indent + "  scaleV: " + a.scaleV + "\n");
                out.append(indent + " uvIndex: " + a.uvIndex + "\n");

                out.append(indent + " " + a.textureDescription.getClass().getSimpleName() + " textureDescription:\n");
                out.append(String.format(indent + "%15s texture: %s\n",
                        a.textureDescription.texture.getClass().getSimpleName(),
                        a.textureDescription.texture.toString()
                ));
                out.append(String.format(indent + "%15s minFilter: %s\n",
                        a.textureDescription.minFilter.getClass().getSimpleName(),
                        a.textureDescription.minFilter.toString()
                ));
                out.append(String.format(indent + "%15s magFilter: %s\n",
                        a.textureDescription.magFilter.getClass().getSimpleName(),
                        a.textureDescription.magFilter.toString()
                ));
                out.append(String.format(indent + "%15s uWrap: %s\n",
                        a.textureDescription.uWrap.getClass().getSimpleName(),
                        a.textureDescription.uWrap.toString()
                ));
                out.append(String.format(indent + "%15s vWrap: %s\n",
                        a.textureDescription.vWrap.getClass().getSimpleName(),
                        a.textureDescription.vWrap.toString()
                ));

                //Gdx.app.debug(LibGDXUtil.class.getSimpleName(), getFieldsContents(attr, 1, "", "", false));
                continue;
            } // TextureAttribute

            // if we got here after all continue's above then we got the type unimplemented yet:
            out.append(indent + "UNIMLPEMENTED: " + attr.getClass().getSimpleName());

            // alternatively the reflection can be used
            // comment above and uncomment below code to see more detailed output
//            out.append(Attribute.getAttributeAlias(attr.type) + ": " + attr.getClass().getSimpleName() + "\n");
//            out.append(getFieldsContents(attr, 1, "", "", false));
        }

        return out.toString().replace("field :","");
    }

    /**
     * @param fileHandle
     * @param filter
     * @return
     */
    public static Array<FileHandle> traversFileHandle(FileHandle fileHandle, FileFilter filter) {
        //Gdx.app.debug(LibGDXUtil.class.getSimpleName(), fileHandle.toString());
        Array<FileHandle> out = new Array<>();
        return traversFileHandle(fileHandle, filter, "\t", out);
    }

    /**
     * @param fileHandle
     * @param out
     * @param filter
     * @return
     */
    public static Array<FileHandle> traversFileHandle(FileHandle fileHandle, Array<FileHandle> out, FileFilter filter) {
        //Gdx.app.debug(LibGDXUtil.class.getSimpleName(), fileHandle.toString());
        if (fileHandle == null) {
            return out;
        }
        return traversFileHandle(fileHandle, filter, "\t", out);
    }

    /**
     * @param fileHandle
     * @param filter
     * @param indent
     * @param out
     * @return
     */
    private static Array<FileHandle> traversFileHandle(FileHandle fileHandle, FileFilter filter, String indent, Array<FileHandle> out) {
        for (FileHandle subFileHandle : fileHandle.list(filter)) {
            //Gdx.app.debug(LibGDXUtil.class.getSimpleName(), indent + subFileHandle);
            if (!subFileHandle.isDirectory()) { out.add(subFileHandle); }

            traversFileHandle(subFileHandle, filter, indent + "\t", out);
        }
        return out;
    }

    /**
     * @param obj
     * @param depth
     * @param start
     * @param writeToFile
     * @return
     */
    public static String getFieldsContents(Object obj, int depth, String start, boolean writeToFile) {
        String out = getFieldsContents(obj, depth, start, "");

        if (writeToFile) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            String filename =
                    GENERATED + Version.VERSION + "_" + obj.getClass().getSimpleName() + "_" + depth
                            + "_" + fmt.format(LocalDateTime.now());

            FileHandle fileHandle = Gdx.files.local(filename + EXTENSION);
            int index = 0;
            while(fileHandle.exists()) {
                fileHandle = Gdx.files.local(filename + String.format("_%05d",index++) + EXTENSION);
            }
            fileHandle.writeString(out, false);
        }

        return out;
    }

    /**
     * @param obj
     * @param depth
     * @param start
     * @param indent
     * @return
     */
    public static String getFieldsContents(Object obj, int depth, String start, String indent) {
        return getFieldsContents(obj, depth, start, indent, true);
    }

    /**
     * @param obj
     * @param depth
     * @param start
     * @return
     */
    public static String getFieldsContents(Object obj, int depth, String start) {
        return getFieldsContents(obj, depth, start, "");
    }

    /**
     * @param obj
     * @param depth
     * @return
     */
    public static String getFieldsContents(Object obj, int depth) {
        return getFieldsContents(obj, depth, "\n", "");
    }

    /**
     * @param obj
     * @param depth
     * @param start
     * @param indent
     * @param printStatic
     * @return
     */
    private static String getFieldsContents(Object obj, int depth, String start, String indent, boolean printStatic) {
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
                if (Modifier.isStatic(field.getModifiers()) && !printStatic) { continue; }

                Object fieldObject = field.get(obj);

                out.append(String.format(fieldFormat,
                        Modifier.isStatic(field.getModifiers()) ? "(static)" : "",
                        field.getType().getSimpleName(),
                        field.getName(),
                        (fieldObject instanceof Iterable<?>)
                                ? (fieldObject instanceof Array<?>)
                                    ? "(" + ((Array<?>)fieldObject).size + ", " + ((Array<?>)fieldObject).ordered + ")"
                                    : ""
                                : toString(fieldObject,indent)));

                if (!field.getType().isPrimitive()
                        && !(fieldObject instanceof Vector3)
                        && !(fieldObject instanceof Quaternion)
                        && !(fieldObject instanceof String)
                        && !(fieldObject instanceof Matrix4)
                ) {
                    String sub = getFieldsContents(fieldObject, depth - 1, "", indent + "\t", printStatic);

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
                            subKey = getFieldsContents(key, depth - 1, "", indent + "\t\t", printStatic);
                            subValue = getFieldsContents(value, depth - 1, "", indent + "\t\t", printStatic);
                        } else {
                            itString = toString(it, indent);
                            sub = getFieldsContents(it, depth - 1, "", indent + "\t\t", printStatic);
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

    /**
     * @param obj
     * @param indent
     * @return
     */
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
