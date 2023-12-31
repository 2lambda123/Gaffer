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
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.export.Exporter;
import uk.gov.gchq.gaffer.operation.export.GetExport;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;

/**
 * Abstract class describing how to handle {@link GetExport} operations.
 *
 * @param <EXPORT>   the {@link GetExport} operation
 * @param <EXPORTER> the {@link Exporter} instance
 */
public abstract class GetExportHandler<EXPORT extends GetExport & Operation, EXPORTER extends Exporter>
        extends ExportOperationHandler<EXPORT, EXPORTER> {
    @Override
    public Iterable<?> doOperation(final EXPORT export,
                                   final Context context,
                                   final Store store,
                                   final EXPORTER exporter)
            throws OperationException {
        return getExport(export, exporter);
    }

    protected Iterable<?> getExport(final EXPORT export, final EXPORTER exporter) throws OperationException {
        return exporter.get(export.getKeyOrDefault());
    }
}
