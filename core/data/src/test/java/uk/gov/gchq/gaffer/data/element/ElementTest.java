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

package uk.gov.gchq.gaffer.data.element;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.commonutil.StringUtil;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.jsonserialisation.JSONSerialiser;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class ElementTest {

    @Test
    public abstract void shouldReturnTrueForEqualsWhenAllCoreFieldsAreEqual();

    protected abstract Element newElement(final String group);

    protected abstract Element newElement();

    @Test
    public void shouldSetAndGetFields() {
        // Given
        final String group = "group";
        final Properties properties = new Properties();
        final Element element = newElement();

        // When
        element.setGroup(group);
        element.setProperties(properties);

        // Then
        assertEquals(group, element.getGroup());
        assertSame(properties, element.getProperties());
    }

    @Test
    public void shouldCreateElementWithUnknownGroup() {
        // Given
        // When
        final Element element = newElement();

        // Then
        assertEquals(Element.DEFAULT_GROUP, element.getGroup());
    }

    @Test
    public void shouldCreateElementWithGroup() {
        // Given
        final String group = "group";

        // When
        final Element element = newElement(group);

        // Then
        assertEquals("group", element.getGroup());
    }

    @Test
    public void shouldReturnTrueForEqualsWithTheSameInstance() {
        // Given
        final Element element = newElement("group");

        // When
        boolean isEqual = element.equals(element);

        // Then
        assertTrue(isEqual);
        assertEquals(element.hashCode(), element.hashCode());
    }

    @Test
    public void shouldReturnFalseForEqualsWhenGroupIsDifferent() {
        // Given
        final Element element1 = newElement("group");
        final Object element2 = newElement("a different group");

        // When
        boolean isEqual = element1.equals(element2);

        // Then
        assertFalse(isEqual);
        assertFalse(element1.hashCode() == element2.hashCode());
    }

    @Test
    public void shouldReturnFalseForEqualsWithNullObject() {
        final Element element1 = newElement("group");

        boolean isEqual = element1.equals((Object) null);

        assertFalse(isEqual);
    }

    @Test
    public void shouldReturnFalseForEqualsWithNullElement() {
        final Element element1 = newElement("group");

        boolean isEqual = element1.equals(null);

        assertFalse(isEqual);
    }

    @Test
    public void shouldReturnItselfForGetElement() {
        final Element element = newElement("group");

        final Element result = element.getElement();

        assertSame(element, result);
    }

    @Test
    public void shouldCopyProperties() {
        // Given
        final Element element1 = newElement("group");
        final Properties newProperties = new Properties("property1", "propertyValue1");

        // When
        element1.copyProperties(newProperties);

        // Then
        assertEquals(1, element1.getProperties().size());
        assertEquals("propertyValue1", element1.getProperty("property1"));
    }

    @Test
    public void shouldRemoveProperty() {
        // Given
        final Element element1 = newElement("group");
        element1.putProperty("property1", "propertyValue1");
        element1.putProperty("property2", "propertyValue2");

        // When
        element1.removeProperty("property1");

        // Then
        assertEquals(1, element1.getProperties().size());
        assertThat(element1.getProperty("property1")).isNull();
        assertEquals("propertyValue2", element1.getProperty("property2"));

    }

    @Test
    public void shouldSerialiseAndDeserialiseProperties() throws SerialisationException {
        // Given
        final Element element = newElement("group");
        final Properties properties = new Properties();
        properties.put("property1", 1L);
        properties.put("property2", 2);
        properties.put("property3", (double) 3);
        properties.put("property4", "4");
        properties.put("property5", new Date(5L));
        element.setProperties(properties);

        // When
        final byte[] serialisedElement = JSONSerialiser.serialise(element);
        final Element deserialisedElement = JSONSerialiser.deserialise(serialisedElement, element.getClass());

        // Then
        assertTrue(StringUtil.toString(serialisedElement).contains("{\"java.util.Date\":5}"));
        assertEquals(element, deserialisedElement);
    }

    @Test
    public abstract void shouldSerialiseAndDeserialiseIdentifiers() throws SerialisationException;
}
