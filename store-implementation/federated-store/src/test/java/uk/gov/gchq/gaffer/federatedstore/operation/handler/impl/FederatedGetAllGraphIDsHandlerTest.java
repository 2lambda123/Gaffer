/*
 * Copyright 2017-2022 Crown Copyright
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

package uk.gov.gchq.gaffer.federatedstore.operation.handler.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import uk.gov.gchq.gaffer.federatedstore.FederatedStore;
import uk.gov.gchq.gaffer.federatedstore.operation.GetAllGraphIds;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.user.User;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.gchq.gaffer.user.StoreUser.testUser;

public class FederatedGetAllGraphIDsHandlerTest {


    private User testUser;

    @BeforeEach
    public void setUp() throws Exception {
        testUser = testUser();
    }

    @Test
    public void shouldGetGraphIds() throws Exception {

        FederatedGetAllGraphIDHandler federatedGetAllGraphIDHandler = new FederatedGetAllGraphIDHandler();

        GetAllGraphIds op = Mockito.mock(GetAllGraphIds.class);
        Context context = Mockito.mock(Context.class);
        BDDMockito.given(context.getUser()).willReturn(testUser);
        FederatedStore store = Mockito.mock(FederatedStore.class);
        List<String> expected = singletonList("value1");
        BDDMockito.given(store.getAllGraphIds(testUser, false)).willReturn(expected);

        Iterable<? extends String> actual = federatedGetAllGraphIDHandler.doOperation(op, context, store);

        assertEquals(expected, actual);

        Mockito.verify(store).getAllGraphIds(testUser, false);
    }
}
