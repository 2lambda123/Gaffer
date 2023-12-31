/*
 * Copyright 2019 Crown Copyright
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
package uk.gov.gchq.gaffer.types.function;

import uk.gov.gchq.gaffer.types.TypeSubTypeValue;
import uk.gov.gchq.koryphe.Since;
import uk.gov.gchq.koryphe.Summary;
import uk.gov.gchq.koryphe.tuple.function.KorypheFunction3;

/**
 * A {@code StringsToTypeSubTypeValue} is a {@link KorypheFunction3} that converts 3 strings: type, sub type and value
 * into a {@link TypeSubTypeValue}.
 */
@Since("1.21.0")
@Summary("Converts 3 strings into a TypeSubTypeValue")
public class StringsToTypeSubTypeValue extends KorypheFunction3<String, String, String, TypeSubTypeValue> {
    @Override
    public TypeSubTypeValue apply(final String type, final String subType, final String value) {
        return new TypeSubTypeValue(type, subType, value);
    }
}
