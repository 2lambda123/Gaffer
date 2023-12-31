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

package uk.gov.gchq.gaffer.operation.export.set;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.gaffer.operation.OperationTest;
import uk.gov.gchq.gaffer.operation.impl.export.set.ExportToSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class ExportToSetTest extends OperationTest<ExportToSet> {
    @Test
    public void shouldJSONSerialiseAndDeserialise() throws SerialisationException {
        // Given
        final String key = "key";
        final ExportToSet op = new ExportToSet.Builder<>()
                .key(key)
                .build();

        // When
        byte[] json = JSONSerialiser.serialise(op, true);
        final ExportToSet deserialisedOp = JSONSerialiser.deserialise(json, ExportToSet.class);

        // Then
        assertEquals(key, deserialisedOp.getKey());
    }

    @Test
    @Override
    public void builderShouldCreatePopulatedOperation() {
        // When
        final ExportToSet op = new ExportToSet.Builder<>()
                .key("key")
                .build();

        // Then
        assertEquals("key", op.getKey());
    }

    @Test
    @Override
    public void shouldShallowCloneOperation() {
        // Given
        final String key = "key";
        final String input = "input";
        final ExportToSet exportToSet = new ExportToSet.Builder<>()
                .key(key)
                .input(input)
                .build();

        // When
        ExportToSet clone = exportToSet.shallowClone();

        // Then
        assertNotSame(exportToSet, clone);
        assertEquals(key, clone.getKey());
        assertEquals(input, clone.getInput());
    }

    @Test
    public void shouldGetOutputClass() {
        // When
        final Class<?> outputClass = getTestObject().getOutputClass();

        // Then
        assertEquals(Object.class, outputClass);
    }

    @Override
    protected ExportToSet getTestObject() {
        return new ExportToSet();
    }
}
