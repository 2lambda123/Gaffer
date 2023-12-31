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
package uk.gov.gchq.gaffer.serialisation;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.commonutil.pair.Pair;
import uk.gov.gchq.gaffer.exception.SerialisationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AvroSerialiserTest extends ToBytesSerialisationTest<Object> {

    @Test
    public void testCanHandleObjectClass() {
        assertTrue(serialiser.canHandle(Object.class));
    }

    @Test
    public void testPrimitiveSerialisation() throws SerialisationException {
        // When
        final byte[] b = serialiser.serialise(2);
        final Object o = serialiser.deserialise(b);

        // Then
        assertEquals(Integer.class, o.getClass());
        assertEquals(2, o);
    }

    @Override
    public Serialiser<Object, byte[]> getSerialisation() {
        return new AvroSerialiser();
    }

    @Override
    public Pair<Object, byte[]>[] getHistoricSerialisationPairs() {
        return null;
    }

    @Test
    @Override
    public void shouldSerialiseWithHistoricValues() {
        //fail( "This has a byte value that changes, timestamp within the Avro?");
    }
}
