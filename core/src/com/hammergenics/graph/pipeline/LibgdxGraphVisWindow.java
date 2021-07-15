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

package com.hammergenics.graph.pipeline;

import com.badlogic.gdx.Gdx;
import com.gempukku.libgdx.graph.loader.GraphLoader;
import com.gempukku.libgdx.graph.pipeline.PipelineFieldType;
import com.gempukku.libgdx.graph.ui.RenderPipelineGraphType;
import com.gempukku.libgdx.graph.ui.UIGraphLoaderCallback;
import com.gempukku.libgdx.graph.ui.graph.GraphDesignTab;
import com.gempukku.libgdx.graph.ui.pipeline.UIPipelineConfiguration;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;

import java.io.IOException;
import java.io.InputStream;

public class LibgdxGraphVisWindow extends VisWindow {

    public GraphDesignTab<PipelineFieldType> graphDesignTab;
    public TabbedPane tabbedPane;
    public UIGraphLoaderCallback<PipelineFieldType> uiGraphLoaderCallback;

    public LibgdxGraphVisWindow(String title) {
        super(title);

        initLibgdxGraph();

        addCloseButton();
        setResizable(true);
        setResizeBorder(16);
        setMovable(true);
        pack();
        centerWindow();
    }

    public void initLibgdxGraph() {
        // https://github.com/MarcinSc/libgdx-graph/wiki/Pipeline-basics#loading-a-pipeline-from-json
        UIPipelineConfiguration pipelineGraphConfiguration = new UIPipelineConfiguration();
        // TODO: fix the unchecked generic array creation
        graphDesignTab = new GraphDesignTab<>(false, RenderPipelineGraphType.instance, "main",
                "Render pipeline", VisUI.getSkin(), null, pipelineGraphConfiguration);
        // TODO: fix the unchecked generic array creation
        uiGraphLoaderCallback = new UIGraphLoaderCallback<>(VisUI.getSkin(), graphDesignTab, pipelineGraphConfiguration);

        graphDesignTab = loadPipelineRenderer(uiGraphLoaderCallback);
        tabbedPane = new TabbedPane();
        tabbedPane.add(graphDesignTab);

        add(tabbedPane.getTable()).expandX().left();
        row();
        add(graphDesignTab.getContentTable()).expand().fill();
    }

    // https://github.com/MarcinSc/libgdx-graph/wiki/Pipeline-basics#loading-a-pipeline-from-json
    private GraphDesignTab<PipelineFieldType> loadPipelineRenderer(UIGraphLoaderCallback<PipelineFieldType> callback) {
        try {
            // see GraphLoader.canReadVersion
            // see GraphLoader.VERSION_MAJOR = 0;
            // see GraphLoader.VERSION_MINOR = 5;
            // see GraphLoader.VERSION_PATCH = 0;
            // see GraphLoader.VERSION = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH

            // see PipelineLoaderCallback.end() -> populatePipelineNodes("end", pipelineNodeMap);
            // see 1. GraphLoader -> loadGraph -> graphLoaderCallback.addPipelineNode(id, type, x, y, data);
            //     2. GraphDataLoaderCallback.addPipelineNode
            //     3. PipelineLoaderCallback.getNodeConfiguration
            //     4. RendererPipelineConfiguration.findProducer(type)
            //
            // see 1. RendererPipelineConfiguration.pipelineNodeProducers
            //     2. RendererPipelineConfiguration.register
            //     3. RendererPipelineConfiguration: static { register's... }
//               StartPipelineNodeConfiguration = type:        PipelineStart | name:       Pipeline start | menu location:             Pipeline
//                 EndPipelineNodeConfiguration = type:          PipelineEnd | name:         Pipeline end | menu location:                 null
//      CustomRendererPipelineNodeConfiguration = type:       CustomRenderer | name:      Custom renderer | menu location:             Pipeline
//            PipelineRendererNodeConfiguration = type:     PipelineRenderer | name:    Pipeline renderer | menu location:             Pipeline
//
//          ValueFloatPipelineNodeConfiguration = type:           ValueFloat | name:                Float | menu location:             Constant
//        ValueVector2PipelineNodeConfiguration = type:         ValueVector2 | name:              Vector2 | menu location:             Constant
//        ValueVector3PipelineNodeConfiguration = type:         ValueVector3 | name:              Vector3 | menu location:             Constant
//          ValueColorPipelineNodeConfiguration = type:           ValueColor | name:                Color | menu location:             Constant
//        ValueBooleanPipelineNodeConfiguration = type:         ValueBoolean | name:              Boolean | menu location:             Constant
//
//          RenderSizePipelineNodeConfiguration = type:           RenderSize | name:          Render size | menu location:             Provided
//                TimePipelineNodeConfiguration = type:                 Time | name:                 Time | menu location:             Provided
//
//                 AddPipelineNodeConfiguration = type:                  Add | name:                  Add | menu location:      Math/Arithmetic
//              DividePipelineNodeConfiguration = type:               Divide | name:               Divide | menu location:      Math/Arithmetic
//            MultiplyPipelineNodeConfiguration = type:             Multiply | name:             Multiply | menu location:      Math/Arithmetic
//            OneMinusPipelineNodeConfiguration = type:             OneMinus | name:            One minus | menu location:      Math/Arithmetic
//          ReciprocalPipelineNodeConfiguration = type:           Reciprocal | name:           Reciprocal | menu location:      Math/Arithmetic
//            SubtractPipelineNodeConfiguration = type:             Subtract | name:             Subtract | menu location:      Math/Arithmetic
//
//                 AbsPipelineNodeConfiguration = type:                  Abs | name:       Absolute value | menu location:          Math/Common
//             CeilingPipelineNodeConfiguration = type:                 Ceil | name:              Ceiling | menu location:          Math/Common
//               ClampPipelineNodeConfiguration = type:                Clamp | name:                Clamp | menu location:          Math/Common
//               FloorPipelineNodeConfiguration = type:                Floor | name:                Floor | menu location:          Math/Common
//               FloorPipelineNodeConfiguration = type:                Floor | name:                Floor | menu location:          Math/Common
//                LerpPipelineNodeConfiguration = type:                  Mix | name:           Mix (lerp) | menu location:          Math/Common
//             MaximumPipelineNodeConfiguration = type:              Maximum | name:              Maximum | menu location:          Math/Common
//             MinimumPipelineNodeConfiguration = type:              Minimum | name:              Minimum | menu location:          Math/Common
//              ModuloPipelineNodeConfiguration = type:               Modulo | name:               Modulo | menu location:          Math/Common
//            SaturatePipelineNodeConfiguration = type:             Saturate | name:             Saturate | menu location:          Math/Common
//                SignPipelineNodeConfiguration = type:                 Sign | name:                 Sign | menu location:          Math/Common
//          SmoothstepPipelineNodeConfiguration = type:           Smoothstep | name:           Smoothstep | menu location:          Math/Common
//                StepPipelineNodeConfiguration = type:                 Step | name:                 Step | menu location:          Math/Common
//
//    ExponentialBase2PipelineNodeConfiguration = type:                 Exp2 | name:           Exp base 2 | menu location:     Math/Exponential
//         ExponentialPipelineNodeConfiguration = type:                  Exp | name:                Exp e | menu location:     Math/Exponential
//   InverseSquareRootPipelineNodeConfiguration = type:          Inversesqrt | name:  Inverse square root | menu location:     Math/Exponential
//      LogarithmBase2PipelineNodeConfiguration = type:                 Log2 | name:           Log base 2 | menu location:     Math/Exponential
//    NaturalLogarithmPipelineNodeConfiguration = type:                  Log | name:                Log e | menu location:     Math/Exponential
//               PowerPipelineNodeConfiguration = type:                Power | name:                Power | menu location:     Math/Exponential
//          SquareRootPipelineNodeConfiguration = type:                 Sqrt | name:          Square root | menu location:     Math/Exponential
//
//        CrossProductPipelineNodeConfiguration = type:         CrossProduct | name:        Cross product | menu location:       Math/Geometric
//            DistancePipelineNodeConfiguration = type:             Distance | name:             Distance | menu location:       Math/Geometric
//          DotProductPipelineNodeConfiguration = type:           DotProduct | name:          Dot product | menu location:       Math/Geometric
//              LengthPipelineNodeConfiguration = type:               Length | name:               Length | menu location:       Math/Geometric
//           NormalizePipelineNodeConfiguration = type:            Normalize | name:            Normalize | menu location:       Math/Geometric
//
//              ArccosPipelineNodeConfiguration = type:               Arccos | name:            Arccosine | menu location:    Math/Trigonometry
//              ArcsinPipelineNodeConfiguration = type:               Arcsin | name:              Arcsine | menu location:    Math/Trigonometry
//              ArctanPipelineNodeConfiguration = type:               Arctan | name:           Arctangent | menu location:    Math/Trigonometry
//                 CosPipelineNodeConfiguration = type:                  Cos | name:               Cosine | menu location:    Math/Trigonometry
//             DegreesPipelineNodeConfiguration = type:              Degrees | name:              Degrees | menu location:    Math/Trigonometry
//             RadiansPipelineNodeConfiguration = type:              Radians | name:              Radians | menu location:    Math/Trigonometry
//                 SinPipelineNodeConfiguration = type:                  Sin | name:                 Sine | menu location:    Math/Trigonometry
//                 TanPipelineNodeConfiguration = type:                  Tan | name:              Tangent | menu location:    Math/Trigonometry
//
//               MergePipelineNodeConfiguration = type:                Merge | name:                Merge | menu location:           Math/Value
//               SplitPipelineNodeConfiguration = type:                Split | name:                Split | menu location:           Math/Value
//
//
//               BloomPipelineNodeConfiguration = type:                Bloom | name: Bloom post-processor | menu location:      Post-processing
//        GaussianBlurPipelineNodeConfiguration = type:         GaussianBlur | name:        Gaussian blur | menu location:      Post-processing
//        DepthOfFieldPipelineNodeConfiguration = type:         DepthOfField | name:       Depth of Field | menu location:      Post-processing
//     GammaCorrectionPipelineNodeConfiguration = type:      GammaCorrection | name:     Gamma correction | menu location:      Post-processing
            InputStream stream = Gdx.files.internal("pipeline.json").read();
            try {
                return GraphLoader.loadGraph(stream, callback);
            } finally {
                stream.close();
            }
        } catch (IOException exp) {
            throw new RuntimeException("Unable to load pipeline", exp);
        }
    }
}