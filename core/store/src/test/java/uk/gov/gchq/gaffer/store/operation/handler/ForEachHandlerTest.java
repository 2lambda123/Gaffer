/*
 * Copyright 2018-2021 Crown Copyright
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

package uk.gov.gchq.gaffer.store.operation.handler;

import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.impl.ForEach;
import uk.gov.gchq.gaffer.operation.impl.get.GetElements;
import uk.gov.gchq.gaffer.operation.io.InputOutput;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;
import uk.gov.gchq.gaffer.user.User;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ForEachHandlerTest {

    @Test
    public void shouldThrowExceptionWithNullOperation() {
        // Given
        final Store store = mock(Store.class);
        final Context context = new Context(new User());
        final ForEach op = new ForEach.Builder<>()
                .operation(null)
                .input(Arrays.asList("1", "2"))
                .build();
        final ForEachHandler handler = new ForEachHandler();

        // When / Then
        assertThatExceptionOfType(OperationException.class).isThrownBy(() -> handler.doOperation(op, context, store)).withMessage("Operation cannot be null");
    }

    @Test
    public void shouldThrowExceptionWithNullInput() {
        // Given
        final Store store = mock(Store.class);
        final Context context = new Context(new User());
        final ForEach op = new ForEach.Builder<>()
                .operation(new GetElements())
                .build();
        final ForEachHandler handler = new ForEachHandler();

        // When / Then
        assertThatExceptionOfType(OperationException.class).isThrownBy(() -> handler.doOperation(op, context, store)).withMessage("Inputs cannot be null");
    }

    @Test
    public void shouldExecuteAndReturnExpected() throws OperationException {
        // Given
        final Store store = mock(Store.class);
        final Context context = new Context(new User());

        final InputOutput op = mock(InputOutput.class);
        final InputOutput opClone = mock(InputOutput.class);
        given(op.shallowClone()).willReturn(opClone);

        final Object input = mock(Object.class);
        final Object output = mock(Object.class);

        final ForEach forEach = new ForEach.Builder<>()
                .input(input)
                .operation(op)
                .build();

        final ForEachHandler handler = new ForEachHandler();

        given(store.execute(opClone, context)).willReturn(output);

        // When
        final List<Object> result = (List<Object>) handler.doOperation(forEach, context, store);

        // Then
        verify(opClone).setInput(input);
        assertThat(result).hasSize(1);
        assertSame(output, result.get(0));
    }
}
