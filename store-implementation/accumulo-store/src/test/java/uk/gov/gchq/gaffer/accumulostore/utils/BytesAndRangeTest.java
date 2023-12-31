/*
 * Copyright 2015-2021 Crown Copyright
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
package uk.gov.gchq.gaffer.accumulostore.utils;

import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class BytesAndRangeTest {

    private byte[] emptyBytes = new byte[]{};

    @Test
    public void shouldThrowNoException() throws Exception {
        new BytesAndRange(emptyBytes, 1, 1);
    }

    @Test
    public void shouldThrowExceptionForOffset() throws Exception {
        assertThatExceptionOfType(InvalidParameterException.class).isThrownBy(
                () -> new BytesAndRange(emptyBytes, -1, 1));
    }

    @Test
    public void shouldThrowExceptionForLength() throws Exception {
        assertThatExceptionOfType(InvalidParameterException.class).isThrownBy(
                () -> new BytesAndRange(emptyBytes, 1, -1));
    }


}
