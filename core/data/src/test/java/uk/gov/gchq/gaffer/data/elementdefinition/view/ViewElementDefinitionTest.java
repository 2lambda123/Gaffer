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

package uk.gov.gchq.gaffer.data.elementdefinition.view;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.commonutil.TestPropertyNames;
import uk.gov.gchq.gaffer.data.element.function.ElementAggregator;
import uk.gov.gchq.gaffer.data.element.function.ElementFilter;
import uk.gov.gchq.gaffer.data.element.function.ElementTransformer;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.koryphe.function.KorypheFunction;
import uk.gov.gchq.koryphe.impl.binaryoperator.Max;
import uk.gov.gchq.koryphe.impl.predicate.IsEqual;
import uk.gov.gchq.koryphe.impl.predicate.IsLessThan;
import uk.gov.gchq.koryphe.impl.predicate.IsMoreThan;
import uk.gov.gchq.koryphe.tuple.binaryoperator.TupleAdaptedBinaryOperator;
import uk.gov.gchq.koryphe.tuple.function.TupleAdaptedFunction;
import uk.gov.gchq.koryphe.tuple.predicate.TupleAdaptedPredicate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class ViewElementDefinitionTest {

    @Test
    public void shouldBuildElementDefinition() {
        // Given
        final ElementTransformer transformer = mock(ElementTransformer.class);
        final ElementFilter preFilter = mock(ElementFilter.class);
        final ElementAggregator aggregator = mock(ElementAggregator.class);
        final ElementFilter postFilter = mock(ElementFilter.class);

        // When
        final ViewElementDefinition elementDef = new ViewElementDefinition.Builder()
                .transientProperty(TestPropertyNames.PROP_1, String.class)
                .transientProperty(TestPropertyNames.PROP_2, String.class)
                .properties(TestPropertyNames.COUNT, TestPropertyNames.DATE)
                .preAggregationFilter(preFilter)
                .aggregator(aggregator)
                .postTransformFilter(postFilter)
                .transformer(transformer)
                .build();

        // Then
        assertEquals(2, elementDef.getTransientProperties().size());
        assertTrue(elementDef.containsTransientProperty(TestPropertyNames.PROP_1));
        assertTrue(elementDef.containsTransientProperty(TestPropertyNames.PROP_2));

        assertEquals(Sets.newHashSet(TestPropertyNames.COUNT, TestPropertyNames.DATE), elementDef.getProperties());
        assertNull(elementDef.getExcludeProperties());
        assertSame(preFilter, elementDef.getPreAggregationFilter());
        assertSame(aggregator, elementDef.getAggregator());
        assertSame(postFilter, elementDef.getPostTransformFilter());
        assertSame(transformer, elementDef.getTransformer());
    }

    @Test
    public void shouldJsonSerialiseAndDeserialise() throws SerialisationException {
        // Given
        final ViewElementDefinition elementDef = new ViewElementDefinition.Builder()
                .transientProperty(TestPropertyNames.PROP_1, String.class)
                .transientProperty(TestPropertyNames.PROP_2, String.class)
                .properties(TestPropertyNames.COUNT, TestPropertyNames.DATE)
                .preAggregationFilter(new ElementFilter.Builder()
                        .select(TestPropertyNames.COUNT)
                        .execute(new IsMoreThan(5))
                        .build())
                .aggregator(new ElementAggregator.Builder()
                        .select(TestPropertyNames.COUNT)
                        .execute(new Max())
                        .build())
                .postAggregationFilter(new ElementFilter.Builder()
                        .select(TestPropertyNames.COUNT)
                        .execute(new IsLessThan(10))
                        .build())
                .transformer(new ElementTransformer.Builder()
                        .select(TestPropertyNames.COUNT)
                        .execute(new TestTransform())
                        .project(TestPropertyNames.PROP_1)
                        .build())
                .postTransformFilter(new ElementFilter.Builder()
                        .select(TestPropertyNames.PROP_1)
                        .execute(new IsEqual("9"))
                        .build())
                .build();

        // When
        final byte[] json = JSONSerialiser.serialise(elementDef, true);
        final ViewElementDefinition deserialisedElementDef = JSONSerialiser.deserialise(json, ViewElementDefinition.class);

        assertEquals(Sets.newHashSet(TestPropertyNames.COUNT, TestPropertyNames.DATE), deserialisedElementDef.getProperties());
        assertNull(deserialisedElementDef.getExcludeProperties());

        final List<TupleAdaptedPredicate<String, ?>> preFilterComponents = deserialisedElementDef.getPreAggregationFilter().getComponents();
        assertThat(preFilterComponents).hasSize(1);
        assertArrayEquals(new String[] {TestPropertyNames.COUNT}, preFilterComponents.get(0).getSelection());
        assertEquals(new IsMoreThan(5), preFilterComponents.get(0).getPredicate());

        final List<TupleAdaptedBinaryOperator<String, ?>> aggComponents = deserialisedElementDef.getAggregator().getComponents();
        assertThat(aggComponents).hasSize(1);
        assertArrayEquals(new String[] {TestPropertyNames.COUNT}, aggComponents.get(0).getSelection());
        assertEquals(new Max(), aggComponents.get(0).getBinaryOperator());

        final List<TupleAdaptedPredicate<String, ?>> postFilterComponents = deserialisedElementDef.getPostAggregationFilter().getComponents();
        assertThat(postFilterComponents).hasSize(1);
        assertArrayEquals(new String[] {TestPropertyNames.COUNT}, postFilterComponents.get(0).getSelection());
        assertEquals(new IsLessThan(10), postFilterComponents.get(0).getPredicate());

        final List<TupleAdaptedFunction<String, ?, ?>> transformComponents = deserialisedElementDef.getTransformer().getComponents();
        assertThat(transformComponents).hasSize(1);
        assertArrayEquals(new String[] {TestPropertyNames.COUNT}, transformComponents.get(0).getSelection());
        assertEquals(new TestTransform(), transformComponents.get(0).getFunction());
        assertArrayEquals(new String[] {TestPropertyNames.PROP_1}, transformComponents.get(0).getProjection());

        final List<TupleAdaptedPredicate<String, ?>> postTransformFilterComponents = deserialisedElementDef.getPostTransformFilter().getComponents();
        assertThat(postTransformFilterComponents).hasSize(1);
        assertArrayEquals(new String[] {TestPropertyNames.PROP_1}, postTransformFilterComponents.get(0).getSelection());
        assertEquals(new IsEqual("9"), postTransformFilterComponents.get(0).getPredicate());

    }

    public static final class TestTransform extends KorypheFunction<Integer, String> {
        @Override
        public String apply(final Integer integer) {
            return Integer.toString(integer);
        }
    }

    @Test
    public void shouldFailToBuildElementDefinitionWhenPreAggregationFilterSpecifiedTwice() {
        final ElementTransformer transformer = mock(ElementTransformer.class);
        final ElementFilter filter = mock(ElementFilter.class);

        assertThatIllegalArgumentException().isThrownBy(() -> new ViewElementDefinition.Builder()
                .transientProperty(TestPropertyNames.PROP_1, String.class)
                .transientProperty(TestPropertyNames.PROP_2, String.class)
                .transformer(transformer)
                .preAggregationFilter(filter)
                .preAggregationFilter(filter)
                .build());
    }

    @Test
    public void shouldFailToBuildElementDefinitionWhenPostAggregationFilterSpecifiedTwice() {
        final ElementTransformer transformer = mock(ElementTransformer.class);
        final ElementFilter filter = mock(ElementFilter.class);

        assertThatIllegalArgumentException().isThrownBy(() -> new ViewElementDefinition.Builder()
                .transientProperty(TestPropertyNames.PROP_1, String.class)
                .transientProperty(TestPropertyNames.PROP_2, String.class)
                .transformer(transformer)
                .postAggregationFilter(filter)
                .postAggregationFilter(filter)
                .build());
    }

    @Test
    public void shouldFailToBuildElementDefinitionWhenPostTransformFilterSpecifiedTwice() {
        final ElementTransformer transformer = mock(ElementTransformer.class);
        final ElementFilter postFilter = mock(ElementFilter.class);

        assertThatIllegalArgumentException().isThrownBy(() -> new ViewElementDefinition.Builder()
                .transientProperty(TestPropertyNames.PROP_1, String.class)
                .transientProperty(TestPropertyNames.PROP_2, String.class)
                .transformer(transformer)
                .postTransformFilter(postFilter)
                .postTransformFilter(postFilter)
                .build());
    }

    @Test
    public void shouldFailToBuildElementDefinitionWhenPropertiesAndExcludePropertiesSet() {
        final ViewElementDefinition.Builder builder = new ViewElementDefinition.Builder();

        builder.properties(TestPropertyNames.PROP_1);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.excludeProperties(TestPropertyNames.PROP_1))
                .withMessage("You cannot set both properties and excludeProperties");
    }

    @Test
    public void shouldFailToBuildElementDefinitionWhenExcludePropertiesAndPropertiesSet() {
        final ViewElementDefinition.Builder builder = new ViewElementDefinition.Builder();

        builder.excludeProperties(TestPropertyNames.PROP_1);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> builder.properties(TestPropertyNames.PROP_1))
                .withMessage("You cannot set both properties and excludeProperties");
    }
}
