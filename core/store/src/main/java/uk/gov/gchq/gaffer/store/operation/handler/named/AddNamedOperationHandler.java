/*
 * Copyright 2016-2023 Crown Copyright
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

package uk.gov.gchq.gaffer.store.operation.handler.named;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.gchq.gaffer.cache.exception.CacheOperationException;
import uk.gov.gchq.gaffer.named.operation.AddNamedOperation;
import uk.gov.gchq.gaffer.named.operation.NamedOperation;
import uk.gov.gchq.gaffer.named.operation.NamedOperationDetail;
import uk.gov.gchq.gaffer.named.operation.ParameterDetail;
import uk.gov.gchq.gaffer.operation.Operation;
import uk.gov.gchq.gaffer.operation.OperationChain;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.store.Context;
import uk.gov.gchq.gaffer.store.Store;
import uk.gov.gchq.gaffer.store.operation.handler.OperationHandler;
import uk.gov.gchq.gaffer.store.operation.handler.named.cache.NamedOperationCache;

import java.util.Map;

import static java.util.Objects.nonNull;

/**
 * Operation handler for AddNamedOperation which adds a Named Operation to the cache.
 */
public class AddNamedOperationHandler implements OperationHandler<AddNamedOperation> {

    private final NamedOperationCache cache;

    @JsonCreator
    public AddNamedOperationHandler(@JsonProperty("suffixCacheName") final String suffixCacheName) {
        this(new NamedOperationCache(suffixCacheName));
    }

    public AddNamedOperationHandler(final NamedOperationCache cache) {
        this.cache = cache;
    }

    @JsonGetter("suffixCacheName")
    public String getSuffixCacheName() {
        return cache.getSuffixCacheName();
    }

    /**
     * Adds a NamedOperation to a cache which must be specified in the operation declarations file. An
     * NamedOperationDetail is built using the fields on the AddNamedOperation. The operation name and operation chain
     * fields must be set and cannot be left empty, or the build() method will fail and a runtime exception will be
     * thrown. The handler then adds/overwrites the NamedOperation according toa an overwrite flag.
     *
     * @param operation the {@link Operation} to be executed
     * @param context   the operation chain context, containing the user who executed the operation
     * @param store     the {@link Store} the operation should be run on
     * @return null (since the output is void)
     * @throws OperationException if the operation on the cache fails
     */
    @Override
    public Void doOperation(final AddNamedOperation operation, final Context context, final Store store) throws OperationException {
        try {
            final NamedOperationDetail namedOperationDetail = new NamedOperationDetail.Builder()
                    .operationChain(operation.getOperationChainAsString())
                    .operationName(operation.getOperationName())
                    .labels(operation.getLabels())
                    .creatorId(context.getUser().getUserId())
                    .readers(operation.getReadAccessRoles())
                    .writers(operation.getWriteAccessRoles())
                    .description(operation.getDescription())
                    .parameters(operation.getParameters())
                    .score(operation.getScore())
                    .readAccessPredicate(operation.getReadAccessPredicate())
                    .writeAccessPredicate(operation.getWriteAccessPredicate())
                    .build();

            validate(namedOperationDetail.getOperationChainWithDefaultParams(), namedOperationDetail);

            cache.addNamedOperation(namedOperationDetail, operation.isOverwriteFlag(), context.getUser(), store.getProperties().getAdminAuth());
        } catch (final CacheOperationException e) {
            throw new OperationException(e.getMessage(), e);
        }
        return null;
    }

    private void validate(final OperationChain<?> operationChain, final NamedOperationDetail namedOperationDetail) throws OperationException {
        for (final Operation op : operationChain.getOperations()) {
            if (op instanceof NamedOperation) {
                throw new OperationException("NamedOperations can not be nested within NamedOperations");
            }
        }

        if (nonNull(namedOperationDetail.getParameters())) {
            final String operationString = namedOperationDetail.getOperations();
            for (final Map.Entry<String, ParameterDetail> parameterDetail : namedOperationDetail.getParameters().entrySet()) {
                final String varName = String.format("${%s}", parameterDetail.getKey());
                if (!operationString.contains(varName)) {
                    throw new OperationException(String.format("Parameter specified in NamedOperation doesn't occur in OperationChain string for %s", varName));
                }
            }
        }
    }
}
