/*
 * Copyright 2016-2021 Crown Copyright
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
package uk.gov.gchq.gaffer.operation.impl.function;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.data.element.Edge;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.Entity;
import uk.gov.gchq.gaffer.data.element.function.ElementFilter;
import uk.gov.gchq.gaffer.operation.OperationTest;
import uk.gov.gchq.koryphe.impl.predicate.IsMoreThan;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class FilterTest extends OperationTest<Filter> {

    @Test
    @Override
    public void builderShouldCreatePopulatedOperation() {
        // Given
        final Filter filter = new Filter.Builder()
                .input(new Entity("road"), new Edge("railway"))
                .entity("road", new ElementFilter.Builder()
                        .select("count")
                        .execute(new IsMoreThan(10))
                        .build())
                .entity("road2", new ElementFilter.Builder()
                        .select("count")
                        .execute(new IsMoreThan(20))
                        .build())
                .build();

        // Then
        assertNotNull(filter.getInput());
    }

    @Test
    @Override
    public void shouldShallowCloneOperation() {
        // Given
        final List<Element> input = new ArrayList<>();
        final Edge edge = new Edge("road");
        input.add(edge);
        final Filter filter = new Filter.Builder()
                .input(input)
                .globalElements(new ElementFilter())
                .build();

        // When
        final Filter clone = filter.shallowClone();

        // Then
        assertNotSame(filter, clone);
        assertThat(clone.getInput().iterator().next()).isEqualTo(edge);
    }

    @Test
    public void shouldGetOutputClass() {
        // When
        final Class<?> outputClass = getTestObject().getOutputClass();

        // Then
        assertEquals(Iterable.class, outputClass);
    }

    @Override
    protected Filter getTestObject() {
        return new Filter();
    }
}
