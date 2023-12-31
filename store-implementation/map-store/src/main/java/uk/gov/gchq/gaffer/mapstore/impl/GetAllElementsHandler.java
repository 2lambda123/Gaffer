/*
 * Copyright 2017-2020 Crown Copyright
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
package uk.gov.gchq.gaffer.mapstore.impl;

import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.elementdefinition.view.ViewUtil;
import uk.gov.gchq.gaffer.mapstore.MapStore;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.impl.get.GetAllElements;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;
import uk.gov.gchq.gaffer.store.StoreTrait;
import uk.gov.gchq.gaffer.store.operation.handler.OutputOperationHandler;
import uk.gov.gchq.gaffer.store.schema.Schema;
import uk.gov.gchq.gaffer.user.User;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * An {@link OutputOperationHandler} for the {@link GetAllElements} operation on the {@link MapStore}.
 */
public class GetAllElementsHandler implements OutputOperationHandler<GetAllElements, Iterable<? extends Element>> {

    @Override
    public Iterable<? extends Element> doOperation(final GetAllElements operation,
                                                   final Context context,
                                                   final Store store)
            throws OperationException {
        return doOperation(operation, context, (MapStore) store);
    }

    private Iterable<Element> doOperation(final GetAllElements operation,
                                          final Context context,
                                          final MapStore mapStore) {
        return new AllElementsIterable(mapStore.getMapImpl(), operation, mapStore, context.getUser());
    }

    private static class AllElementsIterable implements Iterable<Element> {
        private final MapImpl mapImpl;
        private final GetAllElements getAllElements;
        private final Schema schema;
        private final User user;
        private final boolean supportsVisibility;

        AllElementsIterable(final MapImpl mapImpl,
                            final GetAllElements getAllElements,
                            final MapStore mapStore,
                            final User user) {
            this.mapImpl = mapImpl;
            this.getAllElements = getAllElements;
            this.schema = mapStore.getSchema();
            this.user = user;
            this.supportsVisibility = mapStore.getTraits().contains(StoreTrait.VISIBILITY);
        }

        @Override
        public Iterator<Element> iterator() {
            Stream<Element> elements = mapImpl.getAllElements(getAllElements.getView().getGroups());
            if (this.supportsVisibility) {
                elements = GetElementsUtil.applyVisibilityFilter(elements, schema, user);
            }
            elements = GetElementsUtil.applyDirectedTypeFilter(elements, getAllElements.getView().hasEdges(), getAllElements.getDirectedType());
            elements = elements.map(element -> mapImpl.cloneElement(element, schema));
            elements = GetElementsUtil.applyView(elements, schema, getAllElements.getView());
            elements = elements.map(element -> {
                ViewUtil.removeProperties(getAllElements.getView(), element);
                return element;
            });
            return elements.iterator();
        }
    }
}
