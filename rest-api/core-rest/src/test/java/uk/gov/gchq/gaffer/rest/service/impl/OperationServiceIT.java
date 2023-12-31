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

package uk.gov.gchq.gaffer.rest.service.impl;

import org.glassfish.jersey.client.ChunkedInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.commonutil.TestGroups;
import uk.gov.gchq.gaffer.data.GroupCounts;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.Entity;
import uk.gov.gchq.gaffer.named.operation.AddNamedOperation;
import uk.gov.gchq.gaffer.named.operation.GetAllNamedOperations;
import uk.gov.gchq.gaffer.named.operation.NamedOperationDetail;
import uk.gov.gchq.gaffer.operation.OperationChain;
import uk.gov.gchq.gaffer.operation.impl.CountGroups;
import uk.gov.gchq.gaffer.operation.impl.add.AddElements;
import uk.gov.gchq.gaffer.operation.impl.get.GetAllElements;
import uk.gov.gchq.gaffer.rest.AbstractRestApiIT;
import uk.gov.gchq.gaffer.rest.SystemProperty;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class OperationServiceIT extends AbstractRestApiIT {

    @BeforeEach
    @AfterEach
    public void clearUserFactoryClassSystemProperty() {
        System.clearProperty(SystemProperty.USER_FACTORY_CLASS);
    }

    @Test
    public void shouldReturnNamedOpDetailWithLabelWhenLabelIsAddedToNamedOp() throws Exception {
        // Given
        final AddNamedOperation namedOperation = new AddNamedOperation.Builder()
                .name("My Operation With Label")
                .labels(Arrays.asList("test label"))
                .operationChain("{\"operations\":[{\"class\":\"uk.gov.gchq.gaffer.operation.impl.add.AddElements\",\"skipInvalidElements\":false,\"validate\":true}]}")
                .build();
        client.executeOperation(namedOperation);

        // When
        final Response response = client.executeOperation(new GetAllNamedOperations());
        final List<NamedOperationDetail> namedOperationDetails =
                response.readEntity(new GenericType<List<NamedOperationDetail>>() { });

        // Then
        final NamedOperationDetail expected = new NamedOperationDetail.Builder()
                .operationName("My Operation With Label")
                .labels(Arrays.asList("test label"))
                .operationChain("{\"operations\":[{\"class\":\"uk.gov.gchq.gaffer.operation.impl.add.AddElements\",\"skipInvalidElements\":false,\"validate\":true}]}")
                .inputType("uk.gov.gchq.gaffer.data.element.Element[]")
                .creatorId("UNKNOWN")
                .parameters(null)
                .build();

        assertThat(namedOperationDetails.iterator().next()).isEqualTo(expected);
    }

    @Test
    public void shouldReturnAllElements() throws IOException {
        // Given
        client.addElements(DEFAULT_ELEMENTS);

        // When
        final Response response = client.executeOperation(new GetAllElements());

        // Then
        final List<Element> results = response.readEntity(new GenericType<List<Element>>() { });

        verifyElements(DEFAULT_ELEMENTS, results);
    }

    @Test
    public void shouldReturnGroupCounts() throws IOException {
        // Given
        client.addElements(DEFAULT_ELEMENTS);

        // When
        final Response response = client.executeOperationChunked(new OperationChain.Builder()
                .first(new GetAllElements())
                .then(new CountGroups())
                .build());

        // Then
        final GroupCounts groupCounts = response.readEntity(new GenericType<GroupCounts>() { });

        verifyGroupCounts(groupCounts);
    }

    @Test
    public void shouldReturnChunkedOperationChainElements() throws IOException {
        // Given
        client.addElements(DEFAULT_ELEMENTS);

        // When
        final Response response =
                client.executeOperationChainChunked(new OperationChain<>(new GetAllElements()));

        // Then
        final List<Element> results = readChunkedElements(response);

        verifyElements(DEFAULT_ELEMENTS, results);
    }

    @Test
    public void shouldReturnAllChunkedOperationElements() throws IOException {
        // Given
        client.addElements(DEFAULT_ELEMENTS);

        // When
        final Response response = client.executeOperationChunked(new GetAllElements());

        // Then
        final List<Element> results = readChunkedElements(response);

        verifyElements(DEFAULT_ELEMENTS, results);
    }

    @Test
    public void shouldReturnChunkedOperationChainGroupCounts() throws IOException {
        // Given
        client.addElements(DEFAULT_ELEMENTS);

        // When
        final Response response = client.executeOperationChainChunked(new OperationChain.Builder()
                .first(new GetAllElements())
                .then(new CountGroups())
                .build());

        // Then
        final List<GroupCounts> results =
                readChunkedResults(response, new GenericType<ChunkedInput<GroupCounts>>() { });

        assertThat(results).hasSize(1);

        final GroupCounts groupCounts = results.get(0);

        verifyGroupCounts(groupCounts);
    }

    @Test
    public void shouldReturnNoChunkedOperationChainElementsWhenNoElementsInGraph() throws IOException {
        // When
        final Response response = client.executeOperationChainChunked(new OperationChain<>(new GetAllElements()));

        // Then
        final List<Element> results = readChunkedElements(response);

        assertThat(results).isEmpty();
    }

    @Test
    public void shouldReturnNoChunkedOperationElementsWhenNoElementsInGraph() throws IOException {
        // When
        final Response response = client.executeOperationChunked(new GetAllElements());

        // Then
        final List<Element> results = readChunkedElements(response);

        assertThat(results).isEmpty();
    }

    @Test
    public void shouldThrowErrorOnAddElements() throws IOException {
        // Given
        client.addElements(DEFAULT_ELEMENTS);

        // When
        final Response response = client.executeOperation(new AddElements.Builder()
                .input(new Entity("wrong_group", "object"))
                .build());

        assertEquals(500, response.getStatus());
    }

    @Test
    public void shouldThrowErrorOnEmptyOperationChain() throws IOException {
        // When
        final Response response = client.executeOperationChain(new OperationChain());

        assertEquals(500, response.getStatus());
    }

    private List<Element> readChunkedElements(final Response response) {
        return readChunkedResults(response, new GenericType<ChunkedInput<Element>>() { });
    }

    private <T> List<T> readChunkedResults(final Response response, final GenericType<ChunkedInput<T>> genericType) {
        try {
            // Sleep for a short amount of time to ensure that all results are collected
            Thread.sleep(5000);
        } catch (final InterruptedException e) {
            fail("Issue while waiting for chunked response.");
        }

        final ChunkedInput<T> input = response.readEntity(genericType);

        final List<T> results = new ArrayList<>();
        for (T result = input.read(); result != null; result = input.read()) {
            results.add(result);
        }
        return results;
    }

    private void verifyGroupCounts(final GroupCounts groupCounts) {
        assertEquals(2, (int) groupCounts.getEntityGroups().get(TestGroups.ENTITY));
        assertEquals(1, (int) groupCounts.getEdgeGroups().get(TestGroups.EDGE));
        assertFalse(groupCounts.isLimitHit());
    }
}
