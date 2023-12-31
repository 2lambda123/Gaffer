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

package uk.gov.gchq.gaffer.data.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.commonutil.iterable.TransformOneToManyIterable;
import uk.gov.gchq.gaffer.data.element.Element;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OneToManyElementGeneratorTest {

    private Element elm1a;
    private Element elm1b;
    private Element elm2a;
    private Element elm2b;

    private final String obj1 = "object 1";
    private final String obj2 = "object 2";

    @BeforeEach
    public void setup() {
        elm1a = mock(Element.class);
        elm1b = mock(Element.class);
        elm2a = mock(Element.class);
        elm2b = mock(Element.class);
    }

    @Test
    public void getElementsWithIterableObjectsShouldReturnGeneratedElementTransformIterable() {
        final OneToManyElementGenerator<String> generator = new OneToManyElementGeneratorImpl();

        final TransformOneToManyIterable<String, Element> result =
                (TransformOneToManyIterable<String, Element>) generator.apply(Arrays.asList(obj1, obj2));

        assertThat(result).containsExactly(elm1a, elm1b, elm2a, elm2b);
    }

    private class OneToManyElementGeneratorImpl implements OneToManyElementGenerator<String> {
        /**
         * Note this is a strange way of generating elements - but it makes the testing easier.
         *
         * @param domainObject the domain object to convert
         * @return an {@link Iterable} of elements
         */
        @Override
        public Iterable<Element> _apply(final String domainObject) {
            if (obj1.equals(domainObject)) {
                return Arrays.asList(elm1a, elm1b);
            }

            if (obj2.equals(domainObject)) {
                return Arrays.asList(elm2a, elm2b);
            }

            throw new IllegalArgumentException("Unknown domain object type");
        }
    }
}
