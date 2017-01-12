/*
 * Copyright 2017 Crown Copyright
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
package uk.gov.gchq.gaffer.sketches.datasketches.sampling.serialisation;

import com.yahoo.memory.NativeMemory;
import com.yahoo.sketches.sampling.ReservoirLongsUnion;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.serialisation.AbstractSerialisation;

/**
 * A <code>ReservoirLongsUnionSerialiser</code> serialises a {@link ReservoirLongsUnion} using its
 * <code>toByteArray()</code> method.
 */
public class ReservoirLongsUnionSerialiser extends AbstractSerialisation<ReservoirLongsUnion> {
    private static final long serialVersionUID = 2492278033004791488L;

    @Override
    public boolean canHandle(final Class clazz) {
        return ReservoirLongsUnion.class.equals(clazz);
    }

    @Override
    public byte[] serialise(final ReservoirLongsUnion union) throws SerialisationException {
        return union.toByteArray();
    }

    @Override
    public ReservoirLongsUnion deserialise(final byte[] bytes) throws SerialisationException {
        return ReservoirLongsUnion.getInstance(new NativeMemory(bytes));
    }

    @Override
    public ReservoirLongsUnion deserialiseEmptyBytes() throws SerialisationException {
        return null;
    }

    @Override
    public boolean preservesObjectOrdering() {
        return false;
    }
}

