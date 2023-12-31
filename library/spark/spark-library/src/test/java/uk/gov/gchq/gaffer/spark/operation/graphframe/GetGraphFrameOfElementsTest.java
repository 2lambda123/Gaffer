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

package uk.gov.gchq.gaffer.spark.operation.graphframe;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.commonutil.TestGroups;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.data.elementdefinition.view.ViewElementDefinition;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.OperationTest;
import uk.gov.gchq.koryphe.ValidationResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetGraphFrameOfElementsTest extends OperationTest<GetGraphFrameOfElements> {

    @Test
    public void shouldValidateOperationIfViewDoesNotContainEdgesOrEntities() {
        // Given
        final GetGraphFrameOfElements opWithEdgesOnly = new GetGraphFrameOfElements.Builder()
                .view(new View.Builder()
                        .edge(TestGroups.EDGE)
                        .build())
                .build();

        final GetGraphFrameOfElements opWithEntitiesOnly = new GetGraphFrameOfElements.Builder()
                .view(new View.Builder()
                        .entity(TestGroups.ENTITY)
                        .build())
                .build();

        final GetGraphFrameOfElements opWithEmptyView = new GetGraphFrameOfElements.Builder()
                .view(new View.Builder()
                        .build())
                .build();

        // Then
        assertTrue(opWithEdgesOnly.validate().isValid());
        assertTrue(opWithEntitiesOnly.validate().isValid());
        assertFalse(opWithEmptyView.validate().isValid());
    }

    @Test
    public void shouldValidateOperation() {
        // Given
        final Operation op = new GetGraphFrameOfElements.Builder()
                .view(new View.Builder()
                        .edge(TestGroups.EDGE)
                        .entity(TestGroups.ENTITY)
                        .build())
                .build();

        // When
        final ValidationResult validationResult = op.validate();

        // Then
        assertTrue(validationResult.isValid());
    }

    @Test
    public void shouldValidateOperationWhenViewContainsElementsWithReservedPropertyNames() {
        // Given
        final Operation op = new GetGraphFrameOfElements.Builder()
                .view(new View.Builder()
                        .entity(TestGroups.ENTITY, new ViewElementDefinition.Builder()
                                .properties("vertex")
                                .build())
                        .edge(TestGroups.EDGE)
                        .build())
                .build();

        // When
        final ValidationResult validationResult = op.validate();

        // Then
        assertTrue(validationResult.isValid());
    }

    @Test
    @Override
    public void builderShouldCreatePopulatedOperation() {
        // Given
        final GetGraphFrameOfElements op = new GetGraphFrameOfElements.Builder()
                .view(new View.Builder()
                        .edge(TestGroups.EDGE)
                        .entity(TestGroups.ENTITY)
                        .build())
                .build();

        // Then
        assertThat(op.getView()).isNotNull();
        assertThat(op.getView().getEdgeGroups()).contains(TestGroups.EDGE);
        assertThat(op.getView().getEntityGroups()).contains(TestGroups.ENTITY);
    }

    @Test
    @Override
    public void shouldShallowCloneOperation() {
        // Given
        final GetGraphFrameOfElements op = new GetGraphFrameOfElements.Builder()
                .view(new View.Builder()
                        .edge(TestGroups.EDGE)
                        .entity(TestGroups.ENTITY)
                        .build())
                .build();

        // When
        final GetGraphFrameOfElements clone = op.shallowClone();

        // Then
        assertThat(op).isNotSameAs(clone);
        assertThat(op.getView()).isEqualTo(clone.getView());
    }

    @Override
    protected GetGraphFrameOfElements getTestObject() {
        return new GetGraphFrameOfElements();
    }

    @Override
    protected Set<String> getRequiredFields() {
        return Sets.newHashSet("view");
    }
}
