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
package uk.gov.gchq.gaffer.serialisation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.gov.gchq.gaffer.exception.SerialisationException;

import java.io.UnsupportedEncodingException;

/**
 * Abstract serialiser that deserialises the given byte[] via an interim String object.
 * The concrete implementation of this class may use the string in the constructor of the final return type.
 * <br>example :
 * <pre>   @Override
 * public Date deserialiseString(final String value) {
 * return new Date(Long.parseLong(value));
 * }</pre>
 */
public abstract class ToBytesViaStringDeserialiser<T> implements ToBytesSerialiser<T> {

    @JsonIgnore
    private String charset;

    public ToBytesViaStringDeserialiser(final String charsetName) {
        this.charset = charsetName;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(final String charset) {
        this.charset = charset;
    }

    /**
     * Deserialise an array of bytes into the original object.
     *
     * @param bytes the bytes to deserialise
     * @return T the deserialised object
     * @throws SerialisationException if the object fails to deserialise
     * <p>
     * Note that this implementation is less efficient than using deserialise
     * with an offset and a length, but may still be used if necessary.
     * It has been marked deprecated but will not be removed as it is needed
     * in the Serialiser interface.
     * @see #deserialise(byte[], int, int)
     */
    @Deprecated
    @Override
    public final T deserialise(final byte[] bytes) throws SerialisationException {
        return deserialise(bytes, 0, bytes.length);
    }

    @Override
    public final T deserialise(final byte[] allBytes, final int offset, final int length) throws SerialisationException {
        try {
            String valueString = new String(allBytes, offset, length, charset);
            return deserialiseString(valueString);
        } catch (final UnsupportedEncodingException | StringIndexOutOfBoundsException e) {
            throw new SerialisationException(e.getMessage(), e);
        }
    }

    protected abstract T deserialiseString(final String value) throws SerialisationException;

    @Override
    public byte[] serialise(final T object) throws SerialisationException {
        String str;
        try {
            str = serialiseToString(object);
        } catch (final Exception e) {
            throw new SerialisationException("failed to convert object to string for serialisation.", e);
        }
        if (null != str) {
            try {
                return str.getBytes(getCharset());
            } catch (final UnsupportedEncodingException e) {
                throw new SerialisationException("Unable to serialise to bytes using charset: " + getCharset(), e);
            }
        }

        return serialiseNull();
    }

    protected abstract String serialiseToString(final T object) throws SerialisationException;

}
