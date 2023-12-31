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

package uk.gov.gchq.gaffer.jsonserialisation.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.List;

/**
 * Custom deserialisation class for any classes implementing the {@link java.lang.Iterable} interface. This class is
 * required in order to extend Jackson's deserialisation behaviour for Collections to a non-Collections class.
 */
public class IterableDeserializer extends JsonDeserializer<Iterable<?>> implements ContextualDeserializer {

    /**
     * Type variable to store information about the contained type at runtime.
     */
    private JavaType valueType;

    public static SimpleModule getModule() {
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(Iterable.class, new IterableDeserializer());
        return module;
    }

    @Override
    public Iterable<?> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        final JavaType typeReference = ctxt.getTypeFactory()
                .constructCollectionType(List.class, valueType);

        return ctxt.<Iterable<?>>readValue(jp, typeReference);
    }

    @Override
    public JsonDeserializer<?> createContextual(final DeserializationContext deserializationContext,
            final BeanProperty property) throws JsonMappingException {
        final JavaType valueType = deserializationContext.getContextualType()
                .containedType(0);

        final IterableDeserializer deserializer = new IterableDeserializer();
        deserializer.valueType = valueType;

        return deserializer;
    }
}
