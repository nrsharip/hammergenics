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
import com.badlogic.gdx.utils.ObjectSet;
import com.gempukku.libgdx.graph.data.GraphConnection;
import com.gempukku.libgdx.graph.data.GraphNode;
import com.gempukku.libgdx.graph.data.GraphProperty;
import com.gempukku.libgdx.graph.data.GraphValidator;
import com.gempukku.libgdx.graph.data.NodeConnector;
import com.gempukku.libgdx.graph.pipeline.PipelineFieldType;
import com.gempukku.libgdx.graph.pipeline.PipelineLoaderCallback;
import com.gempukku.libgdx.graph.pipeline.PipelineRenderer;
import com.gempukku.libgdx.graph.time.TimeProvider;

/**
 * Add description here
 *
 * @author nrsharip
 */
public class HGPipelineLoaderCallback extends PipelineLoaderCallback {
    public HGPipelineLoaderCallback(TimeProvider timeProvider) {
        super(timeProvider);
    }

    @Override
    public PipelineRenderer end() {
        GraphValidator<GraphNode<PipelineFieldType>, GraphConnection, GraphProperty<PipelineFieldType>, PipelineFieldType> graphValidator = new GraphValidator<>();
        GraphValidator.ValidationResult<GraphNode<PipelineFieldType>, GraphConnection, GraphProperty<PipelineFieldType>, PipelineFieldType> result = graphValidator.validateGraph(this, "end");

        if (result.hasErrors()) {
            ObjectSet<GraphNode<PipelineFieldType>> errorNodes = result.getErrorNodes();
            ObjectSet<GraphNode<PipelineFieldType>> warningNodes = result.getWarningNodes();
            ObjectSet<GraphConnection> errorConnections = result.getErrorConnections();
            ObjectSet<NodeConnector> errorConnectors = result.getErrorConnectors();
            ObjectSet<GraphProperty<PipelineFieldType>> errorProperties = result.getErrorProperties();

            errorNodes.forEach(node -> Gdx.app.error("pipeline loader callback", "error node:" + node.getId()));
            warningNodes.forEach(node -> Gdx.app.error("pipeline loader callback", "warning node:" + node.getId()));
            errorConnections.forEach(connection -> Gdx.app.error("pipeline loader callback", "error connection:"
                    + " field from: " + connection.getFieldFrom()
                    + " field to: " + connection.getFieldTo()
                    + " node from: " + connection.getNodeFrom()
                    + " node to: " + connection.getNodeTo()
            ));
            errorConnectors.forEach(connector -> Gdx.app.error("pipeline loader callback", "error connector:"
                    + " field id: " + connector.getFieldId()
                    + " node id: " + connector.getNodeId()
            ));
            errorProperties.forEach(property -> Gdx.app.error("pipeline loader callback", "error property:"
                    + " name: " + property.getName()
                    + " data: " + property.getData()
                    + " type: " + property.getType()
            ));
        }
        return super.end();
    }
}
