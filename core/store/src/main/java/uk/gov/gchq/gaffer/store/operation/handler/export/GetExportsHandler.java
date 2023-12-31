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

package uk.gov.gchq.gaffer.store.operation.handler.export;

import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.OperationChain;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.export.GetExport;
import uk.gov.gchq.gaffer.operation.impl.export.GetExports;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;
import uk.gov.gchq.gaffer.store.operation.handler.OutputOperationHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handler for {@link GetExports} operations.
 */
public class GetExportsHandler implements OutputOperationHandler<GetExports, Map<String, Iterable<?>>> {
    @Override
    public Map<String, Iterable<?>> doOperation(final GetExports getExports, final Context context, final Store store) throws OperationException {
        final Map<String, Iterable<?>> exports = new LinkedHashMap<>();
        for (final GetExport getExport : getExports.getGetExports()) {
            final Iterable<?> export = (Iterable<?>) store.execute(new OperationChain((Operation) getExport), context);
            exports.put(getExport.getClass().getName() + ": " + getExport.getKeyOrDefault(), export);
        }

        return exports;
    }
}
