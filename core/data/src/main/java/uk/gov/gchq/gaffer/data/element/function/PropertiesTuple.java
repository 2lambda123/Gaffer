/*
 * Copyright 2016-2020 Crown Copyright
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

package uk.gov.gchq.gaffer.data.element.function;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.gaffer.commonutil.ToStringBuilder;
import uk.gov.gchq.gaffer.data.element.Properties;
import uk.gov.gchq.koryphe.tuple.Tuple;

import java.io.Serializable;

/**
 * A {@code PropertiesTuple} implements {@link Tuple} wrapping a
 * {@link Properties} and providing a getter and setter for the element's property values.
 * This class allows Properties to be used with the function module whilst minimising dependencies.
 */
public class PropertiesTuple implements Tuple<String>, Serializable {
    public static final String PROPERTIES = "PROPERTIES";

    private Properties properties;

    public PropertiesTuple() {
    }

    public PropertiesTuple(final Properties properties) {
        this.properties = properties;
    }

    @Override
    public Object get(final String propertyName) {
        if (PROPERTIES.equals(propertyName)) {
            return properties;
        }
        return properties.get(propertyName);
    }

    @Override
    public Iterable<Object> values() {
        return properties.values();
    }

    @Override
    public void put(final String propertyName, final Object value) {
        properties.put(propertyName, value);
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (null == obj || getClass() != obj.getClass()) {
            return false;
        }

        final PropertiesTuple objects = (PropertiesTuple) obj;

        return new EqualsBuilder()
                .append(properties, objects.properties)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(61, 3)
                .append(properties)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("properties", properties)
                .build();
    }


}
