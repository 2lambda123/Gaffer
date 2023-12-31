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

import uk.gov.gchq.gaffer.commonutil.TestGroups;
import uk.gov.gchq.gaffer.data.element.Entity;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.serialisation.implementation.StringSerialiser;
import uk.gov.gchq.gaffer.store.schema.Schema;
import uk.gov.gchq.gaffer.store.schema.SchemaEntityDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EntitySerialiserTest {

    private static Schema schema;
    private static EntitySerialiser serialiser;

    @BeforeAll
    public static void setUp() {
        final SchemaEntityDefinition entityDef = new SchemaEntityDefinition.Builder()
                .build();

        schema = new Schema.Builder()
                .vertexSerialiser(new StringSerialiser())
                .entity(TestGroups.ENTITY, entityDef)
                .build();

        serialiser = new EntitySerialiser(schema);
    }

    @Test
    public void testNullSerialiser() {
        // Given
        schema = new Schema.Builder().build();

        // When / Then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new EntitySerialiser(schema))
                .withMessage("Vertex serialiser is required");
    }

    @Test
    public void testCanSerialiseEntity() throws SerialisationException {
        // Given
        final Entity entity = new Entity(TestGroups.ENTITY, "testVertex");

        // When
        final byte[] serialisedEntity = serialiser.serialise(entity);
        final Entity deserialisedEntity = serialiser.deserialise(serialisedEntity);

        // Then
        assertEquals(entity, deserialisedEntity);
    }

    @Test
    public void testCantSerialiseIntegerClass() throws SerialisationException {
        assertFalse(serialiser.canHandle(Integer.class));
    }

    @Test
    public void testCanSerialiseElementClass() throws SerialisationException {
        assertTrue(serialiser.canHandle(Entity.class));
    }

    @Test
    public void testDeserialiseEmpty() throws SerialisationException {
        assertThat(serialiser.deserialiseEmpty()).isNull();
    }

    @Test
    public void testPreserveObjectOrdering() throws SerialisationException {
        assertEquals(false, serialiser.preservesObjectOrdering());
    }
}
