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
package uk.gov.gchq.gaffer.store.operation;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiser;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetSchemaTest {

    @Test
    public void shouldJSONSerialiseAndDeserialise() throws SerialisationException {
        // Given
        final GetSchema operation = new GetSchema();

        // When
        final byte[] json = JSONSerialiser.serialise(operation, true);
        final GetSchema deserialisedOp = JSONSerialiser.deserialise(json, GetSchema.class);

        // Then
        assertNotNull(deserialisedOp);
    }

    @Test
    public void builderShouldCreatePopulatedOperation() {
        // Given
        final GetSchema operation = new GetSchema.Builder()
                .compact(false)
                .options(new HashMap<>())
                .build();

        // Then
        assertThat(operation.getOptions()).isNotNull();
    }

    @Test
    public void shouldShallowCloneOperation() {
        // Given
        final GetSchema operation = new GetSchema.Builder()
                .compact(true)
                .options(new HashMap<>())
                .build();

        // When
        final GetSchema clone = operation.shallowClone();

        // Then
        assertNotSame(operation, clone);
        assertTrue(clone.isCompact());
        assertNotNull(clone.getOptions());
    }
}
