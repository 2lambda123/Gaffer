/*
 * Copyright 2017-2021 Crown Copyright
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

package uk.gov.gchq.gaffer.store.serialiser;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.data.element.id.EntityId;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.operation.data.EntitySeed;
import uk.gov.gchq.gaffer.serialisation.implementation.StringSerialiser;
import uk.gov.gchq.gaffer.store.schema.Schema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntityIdSerialiserTest {

    private static Schema schema;
    private static EntityIdSerialiser serialiser;

    @BeforeAll
    public static void setUp() {
        schema = new Schema.Builder()
                .vertexSerialiser(new StringSerialiser())
                .build();
        serialiser = new EntityIdSerialiser(schema);
    }

    @Test
    public void testNullSerialiser() {
        // Given
        schema = new Schema.Builder().build();

        // When / Then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new EntityIdSerialiser(schema))
                .withMessage("Vertex serialiser is required");
    }

    @Test
    public void testCanSerialiseEntityId() throws SerialisationException {
        // Given
        final EntityId entityId = new EntitySeed("vertex");

        // When
        final byte[] serialisedEntityId = serialiser.serialise(entityId);
        final EntityId deserialisedEntityId = serialiser.deserialise(serialisedEntityId);

        // Then
        assertEquals(entityId, deserialisedEntityId);
    }

    @Test
    public void testCanSerialiseVertex() throws SerialisationException {
        // Given
        final String testVertex = "TestVertex";

        // When
        final byte[] serialisedVertex = serialiser.serialiseVertex(testVertex);
        final EntityId deserialisedEntityId = serialiser.deserialise(serialisedVertex);

        // Then
        assertEquals(testVertex, deserialisedEntityId.getVertex());

    }

    @Test
    public void testCantSerialiseIntegerClass() throws SerialisationException {
        assertFalse(serialiser.canHandle(Integer.class));
    }

    @Test
    public void testCanSerialiseEntityIdClass() throws SerialisationException {
        assertTrue(serialiser.canHandle(EntityId.class));
    }

    @Test
    public void testDeserialiseEmpty() throws SerialisationException {
        assertThat(serialiser.deserialiseEmpty()).isNull();
    }

    @Test
    public void testPreserveObjectOrdering() throws SerialisationException {
        assertEquals(true, serialiser.preservesObjectOrdering());
    }

    @Test
    public void testIsConsistent() {
        assertEquals(true, serialiser.isConsistent());
    }
}
