/*
 * Copyright 2017-2022 Crown Copyright
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

package uk.gov.gchq.gaffer.store.util;

import uk.gov.gchq.gaffer.commonutil.stream.Streams;
import uk.gov.gchq.gaffer.data.element.Edge;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.GroupedProperties;
import uk.gov.gchq.gaffer.data.element.ReservedPropertyNames;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.data.elementdefinition.view.ViewElementDefinition;
import uk.gov.gchq.gaffer.store.schema.Schema;
import uk.gov.gchq.gaffer.store.schema.SchemaElementDefinition;
import uk.gov.gchq.koryphe.Since;
import uk.gov.gchq.koryphe.Summary;
import uk.gov.gchq.koryphe.binaryoperator.KorypheBinaryOperator;
import uk.gov.gchq.koryphe.function.KorypheFunction;
import uk.gov.gchq.koryphe.iterable.ChainedIterable;
import uk.gov.gchq.koryphe.predicate.KoryphePredicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Utility methods to help with doing aggregation of elements. Aggregation differs
 * depending on if it ingest or query time aggregation. Ingest aggregation uses
 * the groupBy properties in a {@link Schema}, whereas query time aggregation first
 * checks the {@link View} to see if the groupBy properties have been overridden.
 */
public final class AggregatorUtil {

    private AggregatorUtil() {
    }

    /**
     * Applies ingest aggregation to the provided iterable of {@link Element}s.
     * This uses the groupBy properties in the provided {@link Schema} to group
     * the elements prior to aggregating them.
     * <p>
     * NOTE - this is done in memory so the size of the iterable should be limited.
     *
     * @param elements the elements to be aggregated
     * @param schema   the schema containing the aggregators and groupBy properties to use
     * @return the aggregated elements.
     */
    @SuppressWarnings("unchecked")
    public static Iterable<Element> ingestAggregate(final Iterable<? extends Element> elements, final Schema schema) {
        if (isNull(schema)) {
            throw new IllegalArgumentException("Schema is required");
        }
        final Collection<String> aggregatedGroups = schema.getAggregatedGroups();
        final List<Element> aggregatableElements = new ArrayList<>();
        final List<Element> nonAggregatedElements = new ArrayList<>();
        for (final Element element : elements) {
            if (nonNull(element)) {
                if (aggregatedGroups.contains(element.getGroup())) {
                    aggregatableElements.add(element);
                } else {
                    nonAggregatedElements.add(element);
                }
            }
        }

        final Iterable<Element> aggregatedElements = Streams.toStream(aggregatableElements)
                .collect(Collectors.groupingBy(new ToIngestElementKey(schema),
                                               Collectors.reducing(null, new IngestElementBinaryOperator(schema))))
                .values();
        return new ChainedIterable<>(aggregatedElements, nonAggregatedElements);
    }

    /**
     * Applies query time aggregation to the provided iterable of {@link Element}s.
     * This uses the groupBy properties in the provided {@link View} or {@link Schema} to group
     * the elements prior to aggregating them. The Matched Vertex field is ignored during
     * aggregation.
     * <p>
     * NOTE - this is done in memory so the size of the iterable should be limited.
     *
     * @param elements the elements to be aggregated
     * @param schema   the schema containing the aggregators and groupBy properties to use
     * @param view     the view containing the aggregators and groupBy properties to use
     * @return the aggregated elements.
     */
    public static Iterable<Element> queryAggregate(final Iterable<? extends Element> elements, final Schema schema, final View view) {
        return queryAggregate(elements, schema, view, false);
    }

    /**
     * Applies query time aggregation to the provided iterable of {@link Element}s.
     * This uses the groupBy properties in the provided {@link View} or {@link Schema} to group
     * the elements prior to aggregating them. Aggregation of Edges can optionally be
     * configured to include the Matched Vertex field.
     * <p>
     * NOTE - this is done in memory so the size of the iterable should be limited.
     *
     * @param elements             the elements to be aggregated
     * @param schema               the schema containing the aggregators and groupBy properties to use
     * @param view                 the view containing the aggregators and groupBy properties to use
     * @param includeMatchedVertex whether aggregation groups should include the Edge Matched Vertex
     * @return the aggregated elements.
     */
    @SuppressWarnings("unchecked")
    public static Iterable<Element> queryAggregate(final Iterable<? extends Element> elements, final Schema schema, final View view,
                                                   final boolean includeMatchedVertex) {
        if (isNull(schema)) {
            throw new IllegalArgumentException("Schema is required");
        }
        if (isNull(view)) {
            throw new IllegalArgumentException("View is required");
        }
        final Collection<String> aggregatedGroups = schema.getAggregatedGroups();
        final List<Element> aggregatableElements = new ArrayList<>();
        final List<Element> nonAggregatedElements = new ArrayList<>();
        for (final Element element : elements) {
            if (nonNull(element)) {
                if (aggregatedGroups.contains(element.getGroup())) {
                    aggregatableElements.add(element);
                } else {
                    nonAggregatedElements.add(element);
                }
            }
        }
        final Iterable<Element> aggregatedElements = Streams.toStream(aggregatableElements)
                .collect(Collectors.groupingBy(new ToQueryElementKey(schema, view, includeMatchedVertex),
                                               Collectors.reducing(null, new QueryElementBinaryOperator(schema, view))))
                .values();
        return new ChainedIterable<>(aggregatedElements, nonAggregatedElements);
    }

    /**
     * A Function that takes and element as input and outputs an element key that consists of
     * the Group-by values in the {@link Schema}, the Identifiers and the Group. These act as a key and can be used in a
     * Collector to do ingest aggregation.
     */
    @Since("1.0.0")
    @Summary("Extracts the ingest key of an Element")
    public static class ToIngestElementKey extends ToElementKey {
        public ToIngestElementKey(final Schema schema) {
            super(getIngestGroupBys(schema));
        }
    }

    /**
     * A Function that takes and element as input and outputs an element key that consists of
     * the Group-by values in the {@link View}, the Identifiers and the Group. These act as a key and can be used in a
     * Collector to do query aggregation.
     */
    @Since("1.0.0")
    @Summary("Extracts the query time key of an Element")
    public static class ToQueryElementKey extends ToElementKey {
        public ToQueryElementKey(final Schema schema, final View view) {
            this(schema, view, false);
        }

        public ToQueryElementKey(final Schema schema, final View view, final boolean includeMatchedVertex) {
            super(getQueryGroupBys(schema, view), includeMatchedVertex);
        }
    }

    @Since("1.0.0")
    @Summary("Extracts the key of an element")
    public static class ToElementKey extends KorypheFunction<Element, Element> {
        private final Map<String, Set<String>> groupToGroupBys;
        private final boolean includeMatchedVertex;

        public ToElementKey(final Map<String, Set<String>> groupToGroupBys) {
            this(groupToGroupBys, false);
        }

        public ToElementKey(final Map<String, Set<String>> groupToGroupBys, final boolean includeMatchedVertex) {
            if (isNull(groupToGroupBys)) {
                throw new IllegalArgumentException("groupToGroupBys map is required");
            }
            this.groupToGroupBys = groupToGroupBys;
            this.includeMatchedVertex = includeMatchedVertex;
        }

        @Override
        public Element apply(final Element element) {
            final Element key = element.emptyClone();
            final Set<String> groupBy = groupToGroupBys.get(element.getGroup());
            if (isNull(groupBy)) {
                throw new IllegalArgumentException(String.format("Group %s was not recognised", element.getGroup()));
            }
            for (final String propertyName : groupBy) {
                key.putProperty(propertyName, element.getProperty(propertyName));
            }
            if (includeMatchedVertex && element instanceof Edge) {
                key.putProperty(ReservedPropertyNames.MATCHED_VERTEX.name(), Edge.class.cast(element).getMatchedVertex());
            }
            return key;
        }
    }

    @Since("1.0.0")
    @Summary("Aggregates elements, grouping the elements using the ingest key")
    public static class IngestElementBinaryOperator extends ElementBinaryOperator {
        public IngestElementBinaryOperator(final Schema schema) {
            super(schema, null);
        }
    }

    @Since("1.0.0")
    @Summary("Aggregates elements, grouping the elements using the query time key")
    public static class QueryElementBinaryOperator extends ElementBinaryOperator {
        public QueryElementBinaryOperator(final Schema schema, final View view) {
            super(schema, view);
            if (isNull(view)) {
                throw new IllegalArgumentException("View is required");
            }
        }
    }

    @Since("1.0.0")
    @Summary("Aggregates properties, grouping the properties using the ingest key")
    public static class IngestPropertiesBinaryOperator extends PropertiesBinaryOperator {
        public IngestPropertiesBinaryOperator(final Schema schema) {
            super(schema, null);
        }
    }

    @Since("1.0.0")
    @Summary("Aggregates properties, grouping the properties using the query time key")
    public static class QueryPropertiesBinaryOperator extends PropertiesBinaryOperator {
        public QueryPropertiesBinaryOperator(final Schema schema, final View view) {
            super(schema, view);
            if (isNull(view)) {
                throw new IllegalArgumentException("View is required");
            }
        }
    }

    @Since("1.0.0")
    @Summary("Tests if an element can be aggregated")
    public static class IsElementAggregated extends KoryphePredicate<Element> {
        final Collection<String> aggregatedGroups;

        public IsElementAggregated(final Schema schema) {
            if (isNull(schema)) {
                throw new IllegalArgumentException("Schema is required");
            }
            this.aggregatedGroups = schema.getAggregatedGroups();
        }

        public IsElementAggregated(final Collection<String> aggregatedGroups) {
            if (isNull(aggregatedGroups)) {
                throw new IllegalArgumentException("Aggregated groups is required");
            }
            this.aggregatedGroups = aggregatedGroups;
        }

        @Override
        public boolean test(final Element element) {
            return nonNull(element) && aggregatedGroups.contains(element.getGroup());
        }
    }

    protected static class ElementBinaryOperator extends KorypheBinaryOperator<Element> {
        private final Schema schema;
        private final View view;

        protected ElementBinaryOperator(final Schema schema, final View view) {
            if (isNull(schema)) {
                throw new IllegalArgumentException("Schema is required");
            }
            this.view = view;
            this.schema = schema;
        }

        @Override
        public Element _apply(final Element a, final Element b) {
            final String group = a.getGroup();
            if (isNull(view)) {
                return schema.getElement(group).getIngestAggregator().apply(a, b);
            }
            final ViewElementDefinition elementDef = view.getElement(group);
            return schema.getElement(group).getQueryAggregator(elementDef.getGroupBy(), elementDef.getAggregator()).apply(a, b);
        }
    }

    protected static class PropertiesBinaryOperator extends KorypheBinaryOperator<GroupedProperties> {
        private final Schema schema;
        private final View view;

        protected PropertiesBinaryOperator(final Schema schema, final View view) {
            if (isNull(schema)) {
                throw new IllegalArgumentException("Schema is required");
            }
            this.schema = schema;
            this.view = view;
        }

        @Override
        public GroupedProperties _apply(final GroupedProperties a, final GroupedProperties b) {
            final String group = a.getGroup();
            if (isNull(view)) {
                schema.getElement(a.getGroup()).getIngestAggregator().apply(a, b);
            } else {
                final ViewElementDefinition elementDef = view.getElement(group);
                schema.getElement(group).getQueryAggregator(elementDef.getGroupBy(), elementDef.getAggregator()).apply(a, b);
            }

            // The aggregator will always return a so this is safe
            return a;
        }
    }

    public static Map<String, Set<String>> getIngestGroupBys(final Schema schema) {
        if (isNull(schema)) {
            throw new IllegalArgumentException("Schema is required");
        }

        final Map<String, Set<String>> groupToGroupBys = new HashMap<>();
        for (final String group : schema.getGroups()) {
            groupToGroupBys.put(group, getIngestGroupBy(group, schema));
        }

        return groupToGroupBys;
    }

    public static Map<String, Set<String>> getQueryGroupBys(final Schema schema, final View view) {
        if (isNull(schema)) {
            throw new IllegalArgumentException("Schema is required");
        }
        if (isNull(view)) {
            throw new IllegalArgumentException("View is required");
        }
        final Map<String, Set<String>> groupToGroupBys = new HashMap<>();
        for (final String group : schema.getGroups()) {
            groupToGroupBys.put(group, getQueryGroupBy(group, schema, view));
        }

        return groupToGroupBys;
    }

    public static Set<String> getIngestGroupBy(final String group, final Schema schema) {
        final SchemaElementDefinition elDef = schema.getElement(group);
        if (isNull(elDef)) {
            throw new IllegalArgumentException(String.format("Received group %s which was not found in the schema", group));
        }
        if (isNull(schema.getVisibilityProperty()) || !elDef.containsProperty(schema.getVisibilityProperty())) {
            return elDef.getGroupBy();
        }

        final LinkedHashSet<String> groupBy = new LinkedHashSet<>(elDef.getGroupBy());
        groupBy.add(schema.getVisibilityProperty());
        return groupBy;
    }

    public static Set<String> getQueryGroupBy(final String group, final Schema schema, final View view) {
        Set<String> groupBy = null;
        if (nonNull(view)) {
            final ViewElementDefinition elDef = view.getElement(group);
            if (nonNull(elDef)) {
                groupBy = elDef.getGroupBy();
            }
        }
        if (isNull(groupBy)) {
            final SchemaElementDefinition elDef = schema.getElement(group);
            if (isNull(elDef)) {
                throw new IllegalArgumentException(String.format("Received group %s which was not found in the schema", group));
            }
            groupBy = elDef.getGroupBy();
        }
        return groupBy;
    }
}
