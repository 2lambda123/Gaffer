/*
 * Copyright 2016-2018 Crown Copyright
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

package uk.gov.gchq.gaffer.operation.impl.get;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.gaffer.commonutil.ToStringBuilder;
import uk.gov.gchq.gaffer.commonutil.iterable.CloseableIterable;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.id.DirectedType;
import uk.gov.gchq.gaffer.data.element.id.ElementId;
import uk.gov.gchq.gaffer.data.elementdefinition.view.GlobalViewElementDefinition;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.data.elementdefinition.view.ViewElementDefinition;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.SeedMatching;
import uk.gov.gchq.gaffer.operation.graph.SeededGraphFilters;
import uk.gov.gchq.gaffer.operation.io.InputOutput;
import uk.gov.gchq.gaffer.operation.io.MultiElementIdInput;
import uk.gov.gchq.gaffer.operation.serialisation.TypeReferenceImpl;
import uk.gov.gchq.koryphe.Since;
import uk.gov.gchq.koryphe.Summary;
import uk.gov.gchq.koryphe.tuple.function.TupleAdaptedFunction;
import uk.gov.gchq.koryphe.tuple.predicate.TupleAdaptedPredicate;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Gets elements from Gaffer based on {@link ElementId}s as
 * seeds and returns {@link uk.gov.gchq.gaffer.data.element.Element}s
 * There are various flags to filter out the elements returned:
 * seedMatching - can either be {@code SeedMatchingType.RELATED} or {@code SeedMatchingType.EQUAL}.
 * Equal will only return Elements with identifiers that match the seed exactly.
 * Related will return:
 * <ul>
 * <li>Entities when their vertex matches vertex of a EntityId</li>
 * <li>Entities when their vertex matches the source or destination of a EdgeId</li>
 * <li>Edges when their source, destination and directed type matches the EdgeId</li>
 * <li>Edges when their source, destination matches the EdgeId where the DirectedType of the EdgeId is {@code DirectedType.EITHER}</li>
 * <li>Edges when their source or destination match the EntityId's vertex</li>
 * </ul>
 * includeIncomingOutGoing - what type of edges to include
 * <ul>
 * <li>{@code IncludeIncomingOutgoingType.INCOMING} - only returns edges where the destination matches the vertex of EntityId</li>
 * <li>{@code IncludeIncomingOutgoingType.OUTGOING} - only returns edges where the source matches the vertex of EntityId</li>
 * <li>{@code IncludeIncomingOutgoingType.EITHER} - returns all edges regardless of their direction</li>
 * </ul>
 * directedType - whether to return directed, undirected or either edges
 * <ul>
 * <li>{@code DirectedType.DIRECTED} - only return directed edges</li>
 * <li>{@code DirectedType.UNDIRECTED} - only return undirected edges</li>
 * <li>{@code DirectedType.EITHER} - return both directed or undirected edges</li>
 * </ul>
 */
@JsonPropertyOrder(value = {"class", "input", "view"}, alphabetic = true)
@Since("1.0.0")
@Summary("Gets elements related to provided seeds")
public class GetElements implements
        InputOutput<Iterable<? extends ElementId>, CloseableIterable<? extends Element>>,
        MultiElementIdInput,
        SeededGraphFilters,
        SeedMatching {

    /**
     * @deprecated use a {@link View} instead to specify whether
     * Edges/Entities that are 'equal to' or 'related to' seeds are wanted.
     * See filtering documentation.
     */
    private SeedMatchingType seedMatching;

    private View view = new View();
    private IncludeIncomingOutgoingType includeIncomingOutGoing;
    private DirectedType directedType;
    private Iterable<? extends ElementId> input;
    private Map<String, String> options;

    private ViewElementDefinition tmpDef;

    public GetElements outgoing() {
        setIncludeIncomingOutGoing(IncludeIncomingOutgoingType.OUTGOING);
        return this;
    }

    public GetElements global() {
        tmpDef = new GlobalViewElementDefinition.Builder()
                .preAggregationFilterFunctions(null)
                .postAggregationFilterFunctions(null)
                .transformFunctions(null)
                .postTransformFilterFunctions(null)
                .build();
        view.getGlobalElements().add((GlobalViewElementDefinition) tmpDef);
        return this;
    }

    public GetElements summarise() {
        return groupBy();
    }

    public GetElements groupBy(final String... groupBy) {
        if (null == tmpDef) {
            global();
        }
        tmpDef.setGroupBy(new LinkedHashSet<>(Arrays.asList(groupBy)));
        return this;
    }

    public GetElements entity(final String entity) {
        tmpDef = new ViewElementDefinition.Builder()
                .preAggregationFilterFunctions(null)
                .postAggregationFilterFunctions(null)
                .transformFunctions(null)
                .postTransformFilterFunctions(null)
                .build();
        view.getEntities().put(entity, tmpDef);
        return this;
    }

    public GetElements entities(final String... entities) {
        tmpDef = new ViewElementDefinition.Builder()
                .preAggregationFilterFunctions(null)
                .postAggregationFilterFunctions(null)
                .transformFunctions(null)
                .postTransformFilterFunctions(null)
                .build();
        for (final String entity : entities) {
            view.getEntities().put(entity, tmpDef);
        }
        return this;
    }

    public GetElements edge(final String edge) {
        tmpDef = new ViewElementDefinition.Builder()
                .preAggregationFilterFunctions(null)
                .postAggregationFilterFunctions(null)
                .transformFunctions(null)
                .postTransformFilterFunctions(null)
                .build();
        view.getEdges().put(edge, tmpDef);
        return this;
    }

    public GetElements edges(final String... edges) {
        tmpDef = new ViewElementDefinition.Builder()
                .preAggregationFilterFunctions(null)
                .postAggregationFilterFunctions(null)
                .transformFunctions(null)
                .postTransformFilterFunctions(null)
                .build();
        for (final String edge : edges) {
            view.getEdges().put(edge, tmpDef);
        }
        return this;
    }

    public static String[] select(final String... selection) {
        return selection;
    }

    public static String[] project(final String... projection) {
        return projection;
    }

    public GetElements filter(final String selection, final Predicate<?> predicate) {
        return filter(select(selection), predicate);
    }

    public GetElements filter(final String[] selection, final Predicate<?> predicate) {
        if (null != tmpDef.getTransformFunctions() && !tmpDef.getTransformFunctions().isEmpty()) {
            tmpDef.getPostTransformFilterFunctions().add(new TupleAdaptedPredicate<>(predicate, selection));
        } else if (null == tmpDef.getGroupBy()) {
            tmpDef.getPreAggregationFilterFunctions().add(new TupleAdaptedPredicate<>(predicate, selection));
        } else {
            tmpDef.getPostAggregationFilterFunctions().add(new TupleAdaptedPredicate<>(predicate, selection));
        }
        return this;
    }

    public GetElements transform(final String selection, final Function<?, ?> function, final String projection) {
        return transform(select(selection), function, project(projection));
    }

    public GetElements transform(final String[] selection, final Function<?, ?> function, final String[] projection) {
        final TupleAdaptedFunction transformer = new TupleAdaptedFunction<>();
        transformer.setSelection(selection);
        transformer.setFunction(function);
        transformer.setProjection(projection);
        tmpDef.getTransformFunctions().add(transformer);
        return this;
    }

    /**
     * Sets the seedMatchingType which determines how to match seeds to identifiers in the Graph.
     *
     * @param seedMatching a {@link SeedMatchingType} describing how the seeds should be
     *                     matched to the identifiers in the graph.
     * @see SeedMatchingType
     * @deprecated use a {@link View} instead to specify whether
     * Edges/Entities that are 'equal to' or 'related to' seeds are wanted.
     * See filtering documentation
     * Gets the seedMatchingType which determines how to match seeds to identifiers in the Graph.
     */
    @Deprecated
    @Override
    public void setSeedMatching(final SeedMatchingType seedMatching) {
        this.seedMatching = seedMatching;
    }

    /**
     * @return seedMatching a {@link SeedMatchingType} describing how the seeds should be
     * matched to the identifiers in the graph.
     * @see SeedMatchingType
     * @deprecated use a {@link View} instead to specify whether
     * Edges/Entities that are 'equal to' or 'related to' seeds are wanted.
     * See filtering documentation
     * Gets the seedMatchingType which determines how to match seeds to identifiers in the Graph.
     */
    @Deprecated
    @Override
    public SeedMatchingType getSeedMatching() {
        return seedMatching;
    }

    /**
     * Gets the incomingOutGoingType for this operation which is used for filtering Edges.
     *
     * @return includeIncomingOutGoing an {@link IncludeIncomingOutgoingType}
     * that controls the incoming/outgoing direction of {@link uk.gov.gchq.gaffer.data.element.Edge}s that are
     * filtered out in the operation.
     * @see IncludeIncomingOutgoingType
     */
    @Override
    public IncludeIncomingOutgoingType getIncludeIncomingOutGoing() {
        return includeIncomingOutGoing;
    }

    /**
     * Sets the incomingOutGoingType for this operation which is used for filtering Edges.
     *
     * @param inOutType an {@link IncludeIncomingOutgoingType}
     *                  that controls the incoming/outgoing direction of {@link uk.gov.gchq.gaffer.data.element.Edge}s that are
     *                  filtered out in the operation.
     * @see IncludeIncomingOutgoingType
     */
    @Override
    public void setIncludeIncomingOutGoing(final IncludeIncomingOutgoingType inOutType) {
        this.includeIncomingOutGoing = inOutType;
    }

    /**
     * Gets the view of this operation which restricts which elements can be retrieved.
     *
     * @return view the {@link View} for the operation.
     * @see View
     */
    @Override
    public View getView() {
        return view;
    }

    /**
     * Sets the view of this operation which restricts which elements can be retrieved.
     *
     * @param view the {@link View} for the operation.
     * @see View
     */
    @Override
    public void setView(final View view) {
        this.view = view;
    }

    /**
     * Gets the flag determining whether to return directed, undirected or both types of edges.
     *
     * @return directedType the {@link DirectedType} which relates to whether the edges are directed, undirected or
     * either
     * @see DirectedType
     */
    @Override
    public DirectedType getDirectedType() {
        return directedType;
    }

    /**
     * Sets the flag determining whether to return directed, undirected or both types of edges.
     *
     * @param directedType the {@link DirectedType} which relates to whether the edges are directed, undirected or
     *                     either
     * @see DirectedType
     */
    @Override
    public void setDirectedType(final DirectedType directedType) {
        this.directedType = directedType;
    }

    /**
     * Gets the {@link ElementId}s that are used to filter the elements. These can either be iterable of
     * {@link uk.gov.gchq.gaffer.data.element.id.EdgeId}s or {@link uk.gov.gchq.gaffer.data.element.id.EntityId} or a mix
     *
     * @return input the iterable of {@link ElementId}s
     * @see ElementId
     */
    @Override
    public Iterable<? extends ElementId> getInput() {
        return input;
    }

    /**
     * Sets the {@link ElementId}s that are used to filter the elements. These can either be iterable of
     * {@link uk.gov.gchq.gaffer.data.element.id.EdgeId}s or {@link uk.gov.gchq.gaffer.data.element.id.EntityId} or a mix
     *
     * @param input the iterable of {@link ElementId}s
     * @see ElementId
     */
    @Override
    public void setInput(final Iterable<? extends ElementId> input) {
        this.input = input;
    }

    /**
     * Get the output type which in this case is {@link CloseableIterable} of {@link Element}s
     *
     * @return the ClosableIterable of Elements type reference
     */
    @Override
    public TypeReference<CloseableIterable<? extends Element>> getOutputTypeReference() {
        return new TypeReferenceImpl.CloseableIterableElement();
    }

    @Override
    public Map<String, String> getOptions() {
        return options;
    }

    /**
     * Set options specific to the store implementation.
     *
     * @param options the operation options. This may contain store specific options such as authorisation strings or and
     *                other properties required for the operation to be executed. Note these options will probably not be interpreted
     */
    @Override
    public void setOptions(final Map<String, String> options) {
        this.options = options;
    }

    @Override
    public GetElements shallowClone() {
        return new GetElements.Builder()
                .seedMatching(seedMatching)
                .view(view)
                .inOutType(includeIncomingOutGoing)
                .directedType(directedType)
                .input(input)
                .options(options)
                .build();
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (null == obj || getClass() != obj.getClass()) {
            return false;
        }

        final GetElements getElements = (GetElements) obj;

        return new EqualsBuilder()
                .append(seedMatching, getElements.seedMatching)
                .append(view, getElements.view)
                .append(includeIncomingOutGoing, getElements.includeIncomingOutGoing)
                .append(directedType, getElements.directedType)
                .append(input, getElements.input)
                .append(options, getElements.options)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(67, 23)
                .append(options)
                .append(input)
                .append(directedType)
                .append(includeIncomingOutGoing)
                .append(view)
                .append(seedMatching)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("options", options)
                .append("input", input)
                .append("includeIncomingOutGoing", includeIncomingOutGoing)
                .append("view", view)
                .append("seedMatching", seedMatching)
                .append("directedType", directedType)
                .toString();
    }

    public static class Builder extends Operation.BaseBuilder<GetElements, Builder>
            implements InputOutput.Builder<GetElements, Iterable<? extends ElementId>, CloseableIterable<? extends Element>, Builder>,
            MultiElementIdInput.Builder<GetElements, Builder>,
            SeededGraphFilters.Builder<GetElements, Builder>,
            SeedMatching.Builder<GetElements, Builder> {
        public Builder() {
            super(new GetElements());
        }
    }
}
