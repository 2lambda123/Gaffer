/*
 * Copyright 2016-2020 Crown Copyright
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
package uk.gov.gchq.gaffer.accumulostore.operation.impl;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;

import uk.gov.gchq.gaffer.commonutil.pair.Pair;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.id.DirectedType;
import uk.gov.gchq.gaffer.data.element.id.ElementId;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.graph.SeededGraphFilters;
import uk.gov.gchq.gaffer.operation.io.InputOutput;
import uk.gov.gchq.gaffer.operation.io.MultiInput;
import uk.gov.gchq.gaffer.operation.serialisation.TypeReferenceImpl;
import uk.gov.gchq.koryphe.Since;
import uk.gov.gchq.koryphe.Summary;

import java.util.Map;

/**
 * A {@code SummariseGroupOverRanges} operation will return an
 * {@link uk.gov.gchq.gaffer.data.element.Element} that represents the aggregated form of all data between the provided range for the provided group.
 * Note that one result per tablet on which data in the desired range resides will be returned, with large data sets and/or large ranges
 * more likely to produce multiple results and you will need to cache the results and aggregate them again to get a final answer.
 * For this reason it is recommended your provided ranges do not over-lap as you will be unable to tell for a given result which range the result is
 * from.
 * Standard filtering will still occur before the final aggregation of the vertices.
 */
@JsonPropertyOrder(value = {"class", "input", "view"}, alphabetic = true)
@Since("1.0.0")
@Summary("Gets summarised Elements for each group")
public class SummariseGroupOverRanges implements
        InputOutput<Iterable<? extends Pair<? extends ElementId, ? extends ElementId>>, Iterable<? extends Element>>,
        MultiInput<Pair<? extends ElementId, ? extends ElementId>>,
        SeededGraphFilters {

    private Iterable<? extends Pair<? extends ElementId, ? extends ElementId>> input;
    private IncludeIncomingOutgoingType includeIncomingOutGoing;
    private View view;
    private DirectedType directedType;
    private Map<String, String> options;

    @Override
    public Iterable<? extends Pair<? extends ElementId, ? extends ElementId>> getInput() {
        return input;
    }

    @Override
    public void setInput(final Iterable<? extends Pair<? extends ElementId, ? extends ElementId>> input) {
        this.input = input;
    }

    @Override
    public TypeReference<Iterable<? extends Element>> getOutputTypeReference() {
        return new TypeReferenceImpl.IterableElement();
    }

    @Override
    public IncludeIncomingOutgoingType getIncludeIncomingOutGoing() {
        return includeIncomingOutGoing;
    }

    @Override
    public void setIncludeIncomingOutGoing(final IncludeIncomingOutgoingType inOutType) {
        this.includeIncomingOutGoing = inOutType;
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void setView(final View view) {
        this.view = view;
    }

    @Override
    public DirectedType getDirectedType() {
        return directedType;
    }

    @Override
    public void setDirectedType(final DirectedType directedType) {
        this.directedType = directedType;
    }

    @Override
    public Map<String, String> getOptions() {
        return options;
    }

    @Override
    public void setOptions(final Map<String, String> options) {
        this.options = options;
    }

    @Override
    public SummariseGroupOverRanges shallowClone() {
        return new SummariseGroupOverRanges.Builder()
                .input(input)
                .inOutType(includeIncomingOutGoing)
                .view(view)
                .directedType(directedType)
                .options(options)
                .build();
    }

    public static class Builder extends Operation.BaseBuilder<SummariseGroupOverRanges, Builder> implements
            InputOutput.Builder<SummariseGroupOverRanges, Iterable<? extends Pair<? extends ElementId, ? extends ElementId>>, Iterable<? extends Element>, Builder>,
            MultiInput.Builder<SummariseGroupOverRanges, Pair<? extends ElementId, ? extends ElementId>, Builder>,
            SeededGraphFilters.Builder<SummariseGroupOverRanges, Builder> {
        public Builder() {
            super(new SummariseGroupOverRanges());
        }
    }
}
