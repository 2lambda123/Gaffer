/*
 * Copyright 2017-2020 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.gaffer.rest.factory;

import uk.gov.gchq.gaffer.data.elementdefinition.exception.SchemaException;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.rest.SystemProperty;
import uk.gov.gchq.gaffer.store.StoreProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Default implementation of the {@link GraphFactory} interface, used by HK2 to
 * instantiate default {@link Graph} instances.
 */
public class DefaultGraphFactory implements GraphFactory {
    private static Graph graph;

    /**
     * Set to true by default - so the same instance of {@link Graph} will be
     * returned.
     */
    private boolean singletonGraph = true;

    public static GraphFactory createGraphFactory() {
        final String graphFactoryClass = System.getProperty(SystemProperty.GRAPH_FACTORY_CLASS,
                SystemProperty.GRAPH_FACTORY_CLASS_DEFAULT);

        try {
            return Class.forName(graphFactoryClass)
                    .asSubclass(GraphFactory.class)
                    .newInstance();
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to create graph factory from class: " + graphFactoryClass, e);
        }
    }

    protected static Path[] getSchemaPaths() {
        final String schemaPaths = System.getProperty(SystemProperty.SCHEMA_PATHS);
        if (null == schemaPaths) {
            return new Path[0];
        }

        final String[] schemaPathsArray = schemaPaths.split(",");
        final Path[] paths = new Path[schemaPathsArray.length];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = Paths.get(schemaPathsArray[i]);
        }

        return paths;
    }

    @Override
    public Graph getGraph() {
        if (singletonGraph) {
            if (null == graph) {
                setGraph(createGraph());
            }
            return graph;
        }

        return createGraph();
    }

    public static void setGraph(final Graph graph) {
        DefaultGraphFactory.graph = graph;
    }

    public boolean isSingletonGraph() {
        return singletonGraph;
    }

    public void setSingletonGraph(final boolean singletonGraph) {
        this.singletonGraph = singletonGraph;
    }

    @Override
    public Graph.Builder createGraphBuilder() {
        final String storePropertiesPath = System.getProperty(SystemProperty.STORE_PROPERTIES_PATH);
        if (null == storePropertiesPath) {
            throw new SchemaException("The path to the Store Properties was not found in system properties for key: " + SystemProperty.STORE_PROPERTIES_PATH);
        }
        final StoreProperties storeProperties = StoreProperties.loadStoreProperties(storePropertiesPath);

        // Disable any operations required
        storeProperties.addOperationDeclarationPaths("disableOperations.json");

        final Graph.Builder builder = new Graph.Builder();
        builder.storeProperties(storeProperties);

        final String graphConfigPath = System.getProperty(SystemProperty.GRAPH_CONFIG_PATH);
        if (null != graphConfigPath) {
            builder.config(Paths.get(graphConfigPath));
        }

        for (final Path path : getSchemaPaths()) {
            builder.addSchema(path);
        }

        return builder;
    }
}
