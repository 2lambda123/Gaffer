/*
 * Copyright 2016 Crown Copyright
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

package uk.gov.gchq.gaffer.jsonSerialisation;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Test;
import uk.gov.gchq.gaffer.commonutil.CommonConstants;
import uk.gov.gchq.gaffer.commonutil.JsonAssert;
import uk.gov.gchq.gaffer.commonutil.pair.Pair;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.gaffer.serialisation.ParameterisedTestObject;
import uk.gov.gchq.gaffer.serialisation.SimpleTestObject;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JSONSerialiserTest {

    private final Pair<Object, byte[]>[] historicSerialisationPairs;

    @SuppressWarnings("unchecked")
    public JSONSerialiserTest() {
        ParameterisedTestObject<Object> paramTest = new ParameterisedTestObject<>();
        paramTest.setX("Test");
        paramTest.setK(2);
        SimpleTestObject simpleTestObject = new SimpleTestObject();
        simpleTestObject.setX("Test");

        this.historicSerialisationPairs = new Pair[]{
                new Pair(simpleTestObject, new byte[]{123, 34, 120, 34, 58, 34, 84, 101, 115, 116, 34, 125}),
                new Pair(paramTest, new byte[]{123, 34, 120, 34, 58, 34, 84, 101, 115, 116, 34, 44, 34, 107, 34, 58, 50, 125})
        };
    }

    @Test
    public void testPrimitiveSerialisation() throws IOException {
        byte[] b = JSONSerialiser.serialise(2);
        Object o = JSONSerialiser.deserialise(b, Object.class);
        assertEquals(Integer.class, o.getClass());
        assertEquals(2, o);
    }

    @Test
    public void canHandleUnParameterisedDAO() throws SerialisationException {
        assertTrue(JSONSerialiser.canHandle(SimpleTestObject.class));
    }

    @Test
    public void testDAOSerialisation() throws SerialisationException {
        SimpleTestObject test = new SimpleTestObject();
        test.setX("Test");
        byte[] b = JSONSerialiser.serialise(test);
        Object o = JSONSerialiser.deserialise(b, SimpleTestObject.class);
        assertEquals(SimpleTestObject.class, o.getClass());
        assertEquals("Test", ((SimpleTestObject) o).getX());
    }

    @Test
    public void shouldNotPrettyPrintByDefaultWhenSerialising() throws SerialisationException {
        SimpleTestObject test = new SimpleTestObject();
        test.setX("TestValue1");
        byte[] bytes = JSONSerialiser.serialise(test);
        assertEquals("{\"x\":\"TestValue1\"}", new String(bytes));
    }

    @Test
    public void shouldPrettyPrintWhenSerialisingAndSetToPrettyPrint() throws SerialisationException {
        SimpleTestObject test = new SimpleTestObject();
        test.setX("TestValue1");
        byte[] bytes = JSONSerialiser.serialise(test, true);
        JsonAssert.assertEquals(String.format("{%n  \"x\" : \"TestValue1\"%n}"), new String(bytes));
    }

    @Test
    public void canHandleParameterisedDAO() throws SerialisationException {
        assertTrue(JSONSerialiser.canHandle(ParameterisedTestObject.class));
    }

    @Test
    public void testParameterisedDAOSerialisation() throws SerialisationException {
        ParameterisedTestObject<Integer> test = new ParameterisedTestObject<>();
        test.setX("Test");
        test.setK(2);
        byte[] b = JSONSerialiser.serialise(test);
        Object o = JSONSerialiser.deserialise(b, ParameterisedTestObject.class);
        assertEquals(ParameterisedTestObject.class, o.getClass());
        assertEquals("Test", ((ParameterisedTestObject) o).getX());
        assertEquals(Integer.class, ((ParameterisedTestObject) o).getK().getClass());
        assertEquals(2, ((ParameterisedTestObject) o).getK());
    }

    @Test
    public void testParameterisedDAOTypeRefDeserialisation() throws SerialisationException {
        ParameterisedTestObject<Integer> test = new ParameterisedTestObject<>();
        test.setX("Test");
        test.setK(2);
        byte[] b = JSONSerialiser.serialise(test);
        ParameterisedTestObject<Integer> o = JSONSerialiser.deserialise(b, new TypeReference<ParameterisedTestObject<Integer>>() {
        });
        assertEquals("Test", o.getX());
        assertEquals(Integer.valueOf(2), o.getK());
    }

    @Test
    public void testParameterisedDeserialisationOfComplexObject() throws SerialisationException {
        SimpleTestObject test = new SimpleTestObject();
        test.setX("Test");
        byte[] b = JSONSerialiser.serialise(test);
        SimpleTestObject o = JSONSerialiser.deserialise(b, SimpleTestObject.class);
        assertEquals(SimpleTestObject.class, o.getClass());
        assertEquals("Test", o.getX());
    }

    @Test
    public void testParameterisedDeserialisationOfParameterisedComplexObject() throws SerialisationException {
        ParameterisedTestObject<Integer> test = new ParameterisedTestObject<>();
        test.setX("Test");
        test.setK(2);
        byte[] b = JSONSerialiser.serialise(test);
        ParameterisedTestObject o = JSONSerialiser.deserialise(b, ParameterisedTestObject.class);
        assertEquals(ParameterisedTestObject.class, o.getClass());
        assertEquals("Test", o.getX());
        assertEquals(Integer.class, o.getK().getClass());
        assertEquals(2, o.getK());
    }


    @Test(expected = SerialisationException.class)
    public void testParameterisedDeserialisationOfComplexObjectToIncorrectType() throws SerialisationException {
        SimpleTestObject test = new SimpleTestObject();
        test.setX("Test");
        byte[] b = JSONSerialiser.serialise(test);
        JSONSerialiser.deserialise(b, Integer.class);
    }

    @Test
    public void shouldSerialiseObjectWithoutFieldX() throws Exception {
        // Given
        final SimpleTestObject obj = new SimpleTestObject();

        // When
        final String json = new String(JSONSerialiser.serialise(obj, "x"), CommonConstants.UTF_8);

        // Then
        assertFalse(json.contains("x"));
    }

    @Test
    public void shouldSerialiseObjectWithFieldX() throws Exception {
        // Given
        final SimpleTestObject obj = new SimpleTestObject();

        // When
        final String json = new String(JSONSerialiser.serialise(obj), CommonConstants.UTF_8);

        // Then
        assertTrue(json.contains("x"));
    }

    @Test
    public void shouldSerialiseWithHistoricValues() throws Exception {
        assertNotNull(historicSerialisationPairs);
        for (Pair<Object, byte[]> pair : historicSerialisationPairs) {
            serialiseFirst(pair);
            deserialiseSecond(pair);
        }
    }

    protected void deserialiseSecond(final Pair<Object, byte[]> pair) throws SerialisationException {
        assertEquals(pair.getFirst(), JSONSerialiser.deserialise(pair.getSecond(), pair.getFirst().getClass()));
    }

    protected void serialiseFirst(final Pair<Object, byte[]> pair) throws SerialisationException {
        byte[] serialise = JSONSerialiser.serialise(pair.getFirst());
        assertArrayEquals(pair.getSecond(), serialise);
    }

}
