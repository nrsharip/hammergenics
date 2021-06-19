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

package com.hammergenics.screens.graphics.g3d.saver;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.LocalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.UBJsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.badlogic.gdx.graphics.VertexAttributes.Usage;
import static com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader.VERSION_HI;
import static com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader.VERSION_LO;
import static com.badlogic.gdx.utils.JsonWriter.OutputType;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class G3dModelSaver {
    // see:
    // - G3dModelLoader.loadModelData
    // - G3dModelLoader.parseModel
    // - G3dModelLoader.parseMeshes(model, json);
    // - G3dModelLoader.parseMaterials(model, json, handle.parent().path());
    // - G3dModelLoader.parseNodes(model, json);
    // - G3dModelLoader.parseAnimations(model, json);

    // - Model.loadMeshes(modelData.meshes);
    // - Model.loadMaterials(modelData.materials, textureProvider);
    // - Model.loadNodes(modelData.nodes);
    // - Model.loadAnimations(modelData.animations);

    public JsonWriter g3dj;
    public UBJsonWriter g3db;
    public FileHandleResolver resolver = new LocalFileHandleResolver();

    public void saveG3dj(FileHandle fh, ModelInstance mi) {
        g3dj = new JsonWriter(fh.writer(false, "UTF-8"));
        g3dj.setOutputType(OutputType.json);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();

        try {
            g3dj.object(); // root object
            //g3djWriter.name("raw").value(now).name("fmt").value(fmt.format(now));

            g3dj.array("version").value(VERSION_HI).value(VERSION_LO).pop();
            g3dj.name("id").value("");

            g3dj.array("meshes"); // array-meshes:start
            for (Mesh mesh:mi.model.meshes) {
                g3dj.object(); // object-mesh:start
                long attrMask = mesh.getVertexAttributes().getMask();

                g3dj.array("attributes"); // array-attributes:start
                for (String attrString:getAttributesByMask(attrMask, new Array<>(true, 16, String.class))) {
                    g3dj.value(attrString);
                }
                g3dj.pop(); // array-attributes:end

                float[] vertices = getMeshVertices(mesh);

                g3dj.array("vertices"); // array-vertices:start
                for (int i = 0; i < vertices.length; i++) { g3dj.value(String.valueOf(vertices[i])); }
                g3dj.pop(); // array-vertices:end

                g3dj.array("parts"); // array-parts:start
                for (MeshPart meshPart:mi.model.meshParts) {
                    if (mesh != meshPart.mesh) { continue; }

                    g3dj.object(); // object-part:start
                    short[] indices = getMeshIndices(mesh);

                    g3dj.name("id").value(meshPart.id);
                    g3dj.name("type").value(getStringByMeshPartType(meshPart.primitiveType));

                    g3dj.array("indices"); // array-indices:start
                    for (int i = meshPart.offset; i < meshPart.offset + meshPart.size; i++) {
                        g3dj.value(String.valueOf(indices[i]));
                    }
                    g3dj.pop(); // array-indices:end
                    g3dj.pop(); // object-part:end
                }
                g3dj.pop(); // array-parts:end
                g3dj.pop(); // object-mesh:end
            }
            g3dj.pop(); // array-meshes:end

            g3dj.array("materials"); // array-materials:start
            for (Material mat:mi.materials) {
                g3dj.object(); // object-material:start

                g3dj.name("id").value(mat.id);

                // see G3dModelLoader.parseMaterials
                ColorAttribute cad = mat.get(ColorAttribute.class, ColorAttribute.Diffuse);
                ColorAttribute caa = mat.get(ColorAttribute.class, ColorAttribute.Ambient);
                ColorAttribute cae = mat.get(ColorAttribute.class, ColorAttribute.Emissive);
                ColorAttribute cas = mat.get(ColorAttribute.class, ColorAttribute.Specular);
                ColorAttribute car = mat.get(ColorAttribute.class, ColorAttribute.Reflection);
                if (cad != null) { Color c = cad.color; g3dj.array("diffuse").value(c.r).value(c.g).value(c.b).pop(); }
                if (caa != null) { Color c = caa.color; g3dj.array("ambient").value(c.r).value(c.g).value(c.b).pop(); }
                if (cae != null) { Color c = cae.color; g3dj.array("emissive").value(c.r).value(c.g).value(c.b).pop(); }
                if (cas != null) { Color c = cas.color; g3dj.array("specular").value(c.r).value(c.g).value(c.b).pop(); }
                if (car != null) { Color c = car.color; g3dj.array("reflection").value(c.r).value(c.g).value(c.b).pop(); }

                FloatAttribute faat = mat.get(FloatAttribute.class, FloatAttribute.AlphaTest);
                FloatAttribute fash = mat.get(FloatAttribute.class, FloatAttribute.Shininess);
                if (fash != null) { g3dj.name("shininess").value(fash.value); }

                BlendingAttribute ba = mat.get(BlendingAttribute.class, BlendingAttribute.Type);
                if (ba != null) { g3dj.name("opacity").value(ba.opacity); }

                g3dj.pop(); // object-material:end
            }
            g3dj.pop(); // array-materials:end

            g3dj.array("nodes"); // array-nodes:start
            for(Node node:mi.nodes) { g3djAddNode(node); }
            g3dj.pop(); // array-nodes:end

            g3dj.array("animations"); // array-animations:start
            // TODO: not implemented
            g3dj.pop(); // array-animations:end

            g3dj.close();
        } catch (IOException e) {
            Gdx.app.error(getClass().getSimpleName(), "ERROR writing to file: " + e.getMessage());
        }
    }

    public void saveG3db(FileHandle fh, ModelInstance mi) {
        //g3dbWriter = new UBJsonWriter(fh.write(false, 8192));
    }

    // see: G3dModelLoader.parseAttributes
    public Array<String> getAttributesByMask(long mask, Array<String> out) {
        for (int i = 0; i < VertexAttributes.Usage.class.getFields().length; i++) {
            int usage = 1 << i;
            if ((mask & usage) != 0) {
                switch (usage) {
                    case Usage.Position: out.add("POSITION"); break;
                    case Usage.ColorUnpacked: out.add("COLOR"); break;
                    case Usage.ColorPacked: out.add("COLORPACKED"); break;
                    case Usage.Normal: out.add("NORMAL"); break;
                    case Usage.TextureCoordinates: out.add("TEXCOORD"); break; // TODO: revisit the index for TEXCOORD
                    case Usage.BoneWeight: out.add("BLENDWEIGHT"); break;      // TODO: revisit the index for BLENDWEIGHT
                    case Usage.Tangent: out.add("TANGENT"); break;
                    case Usage.BiNormal: out.add("BINORMAL"); break;
                    case Usage.Generic:
                    default:
                        Gdx.app.error(getClass().getSimpleName(), "ERROR: unsupported usage: " + usage);
                        break;
                }
            }
        }
        return out;
    }

    // see G3dModelLoader.parseType
    public String getStringByMeshPartType(int type) {
        switch (type) {
            case GL20.GL_TRIANGLES: return "TRIANGLES";
            case GL20.GL_LINES: return "LINES";
            case GL20.GL_POINTS: return "POINTS";
            case GL20.GL_TRIANGLE_STRIP: return "TRIANGLE_STRIP";
            case GL20.GL_LINE_STRIP: return "LINE_STRIP";
            default:
                Gdx.app.error(getClass().getSimpleName(), "ERROR: unsupported primitive type: " + type);
                return null;
        }
    }

    public short[] getMeshIndices(Mesh mesh) {
        short[] indices = new short[mesh.getNumIndices()];
        mesh.getIndices(indices);

        return indices;
    }

    public float[] getMeshVertices(Mesh mesh) {
        VertexAttributes vertexAttributes = mesh.getVertexAttributes();
        int vs = vertexAttributes.vertexSize / 4;
        float[] vertices = new float[vs * mesh.getNumVertices()];
        mesh.getVertices(vertices);

        return vertices;
    }

    public void g3djAddNode(Node node) {
        try {
            Vector3 trans = node.translation.equals(Vector3.Zero) ? null : node.translation;
            Quaternion rot = node.rotation.equals(new Quaternion().idt()) ? null : node.rotation;
            Vector3 scale = node.scale.equals(new Vector3(1, 1, 1)) ? null : node.scale;

            g3dj.object(); // object-node:start

            g3dj.name("id").value(node.id);
            if (trans != null) { g3dj.array("translation").value(trans.x).value(trans.y).value(trans.z).pop(); }
            if (rot != null) { g3dj.array("rotation").value(rot.x).value(rot.y).value(rot.z).value(rot.w).pop(); }
            if (scale != null) { g3dj.array("scale").value(scale.x).value(scale.y).value(scale.z).pop(); }

            if (node.parts.size > 0) {
                g3dj.array("parts"); // array-nodeparts:start
                for (NodePart np: node.parts) {
                    g3dj.object(); // object-nodepart:start
                    g3dj.name("meshpartid").value(np.meshPart.id);
                    g3dj.name("materialid").value(np.material.id);
                    g3dj.array("uvMapping").pop(); // TODO: not implemented

                    if (np.bones != null && np.bones.length > 0) {
                        g3dj.array("bones"); // array-bones:start
                        for (int i = 0; i < np.bones.length; i++) {
                            // see Model.loadNodes:
                            Matrix4 bone = np.invBoneBindTransforms.getValueAt(i).cpy().inv();
                            Vector3 boneTrans = bone.getTranslation(new Vector3());
                            Quaternion boneRot = bone.getRotation(new Quaternion());
                            Vector3 boneScale = bone.getScale(new Vector3());
                            Node boneNodeRef = np.invBoneBindTransforms.getKeyAt(i);

                            g3dj.object();
                            g3dj.name("node").value(boneNodeRef.id);
                            g3dj.array("translation").value(boneTrans.x).value(boneTrans.y).value(boneTrans.z).pop();
                            g3dj.array("rotation").value(boneRot.x).value(boneRot.y).value(boneRot.z).value(boneRot.w).pop();
                            g3dj.array("scale").value(boneScale.x).value(boneScale.y).value(boneScale.z).pop();
                            g3dj.pop();
                        }
                        g3dj.pop(); // array-bones:end
                    }
                    g3dj.pop(); // object-nodepart:end
                }
                g3dj.pop(); // array-nodeparts:end
            }

            if (node.hasChildren()) {
                g3dj.array("children");
                for (Node child: node.getChildren()) { g3djAddNode(child); }
                g3dj.pop();
            }

            g3dj.pop(); // object-node:end
        } catch (IOException e) {
            Gdx.app.error(getClass().getSimpleName(), "ERROR writing to file: " + e.getMessage());
        }
    }
}