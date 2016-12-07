/*
 * Copyright 2016 Crown Copyright
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
package uk.gov.gchq.gaffer.serialisation.simple;

import uk.gov.gchq.gaffer.commonutil.CommonConstants;
import uk.gov.gchq.gaffer.exception.SerialisationException;
import uk.gov.gchq.gaffer.serialisation.AbstractSerialisation;
import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * @deprecated this is not very efficient and should only be used for compatibility
 * reasons. For new properties use {@link uk.gov.gchq.gaffer.serialisation.implementation.raw.RawDateSerialiser}
 * instead.
 */
@Deprecated
public class DateSerialiser extends AbstractSerialisation<Date> {
    private static final long serialVersionUID = 5647756843689779437L;

    @Override
    public boolean canHandle(final Class clazz) {
        return Date.class.equals(clazz);
    }

    @Override
    public byte[] serialise(final Date value) throws SerialisationException {
        try {
            return ((Long) value.getTime()).toString().getBytes(CommonConstants.ISO_8859_1_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new SerialisationException(e.getMessage(), e);
        }
    }

    @Override
    public Date deserialise(final byte[] bytes) throws SerialisationException {
        Long longR;
        try {
            longR = Long.parseLong(new String(bytes, CommonConstants.ISO_8859_1_ENCODING));
        } catch (NumberFormatException | UnsupportedEncodingException e) {
            throw new SerialisationException(e.getMessage(), e);
        }
        return new Date(longR);
    }

    @Override
    public Date deserialiseEmptyBytes() {
        return null;
    }

    @Override
    public boolean isByteOrderPreserved() {
        return true;
    }
}
