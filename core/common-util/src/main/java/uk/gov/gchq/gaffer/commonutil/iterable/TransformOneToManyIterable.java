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

package uk.gov.gchq.gaffer.commonutil.iterable;

import uk.gov.gchq.gaffer.commonutil.CloseableUtil;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import static java.util.Objects.isNull;

/**
 * A {@code TransformToMultiIterable} allows {@link Iterable}s to be lazily validated and transformed without
 * loading the entire iterable into memory. The easiest way to use this class is to create an anonymous inner class.
 * This class is very similar to {@link TransformOneToManyIterable} except that this class transforms one to many
 * items.
 *
 * @param <I> The input iterable type.
 * @param <O> the output iterable type.
 */
public abstract class TransformOneToManyIterable<I, O> implements Closeable, Iterable<O> {
    private final Iterable<? extends I> input;
    private final Validator<I> validator;
    private final boolean skipInvalid;
    private final boolean autoClose;

    /**
     * Constructs an {@code TransformOneToManyIterable} with the given input
     * {@link Iterable} and no validation.
     *
     * @param input the input {@link Iterable}
     */
    public TransformOneToManyIterable(final Iterable<? extends I> input) {
        this(input, new AlwaysValid<>(), false);
    }

    /**
     * Constructs an {@code TransformOneToManyIterable} with the given input
     * {@link Iterable} and
     * {@link Validator}. Invalid items will throw an
     * {@link IllegalArgumentException} to be thrown.
     *
     * @param input     the input {@link Iterable}
     * @param validator the {@link Validator}
     */
    public TransformOneToManyIterable(final Iterable<? extends I> input, final Validator<I> validator) {
        this(input, validator, false);
    }

    /**
     * Constructs an {@code TransformOneToManyIterable} with the given input {@link Iterable},
     * {@link Validator} and a skipInvalid flag to determine whether invalid items should be skipped.
     *
     * @param input       the input {@link Iterable}
     * @param validator   the {@link Validator}
     * @param skipInvalid if true invalid items should be skipped
     */
    public TransformOneToManyIterable(final Iterable<? extends I> input, final Validator<I> validator, final boolean skipInvalid) {
        this(input, validator, skipInvalid, true);
    }

    /**
     * Constructs an {@code TransformOneToManyIterable} with the given inputs
     *
     * @param input       the input {@link Iterable}
     * @param validator   the {@link Validator}
     * @param skipInvalid if true invalid items should be skipped
     * @param autoClose   if true then the input iterable will be closed when any iterators reach the end.
     */
    public TransformOneToManyIterable(final Iterable<? extends I> input, final Validator<I> validator, final boolean skipInvalid,
                                      final boolean autoClose) {
        this.input = input;
        this.validator = validator;
        this.skipInvalid = skipInvalid;
        this.autoClose = autoClose;
    }

    @Override
    public void close() {
        CloseableUtil.close(input);
    }

    /**
     * @return an {@link java.util.Iterator} that lazy transforms the I items to O items
     */
    @Override
    public Iterator<O> iterator() {
        return new TransformOneToManyIterator(input.iterator());
    }

    /**
     * Transforms the I item into an O iterable.
     *
     * @param item the I item to be transformed
     * @return the transformed O iterable
     */
    protected abstract Iterable<O> transform(final I item);

    /**
     * Handles an invalid item. Simply throws an {@link IllegalArgumentException} explaining that the item is
     * invalid. Override this method to handle invalid items differently.
     *
     * @param item the invalid I item
     * @throws IllegalArgumentException always thrown unless this method is overridden.
     */
    protected void handleInvalidItem(final I item) {
        final String itemDescription = null != item ? item.toString() : "<unknown>";
        throw new IllegalArgumentException("Next " + itemDescription + " in iterable is not valid.");
    }

    private Class<?> getIterableClass() {
        return getClass();
    }

    private final class TransformOneToManyIterator implements Closeable, Iterator<O> {

        private final Iterator<? extends I> iterator;

        private Iterator<O> nextElements;
        private Boolean hasNext;

        private TransformOneToManyIterator(final Iterator<? extends I> iterator) {
            this.iterator = iterator;
        }

        @Override
        public void close() {
            CloseableUtil.close(iterator);
        }

        @Override
        public boolean hasNext() {
            if (isNull(hasNext)) {
                if (iterator.hasNext()) {
                    final I possibleNext = iterator.next();
                    if (validator.validate(possibleNext)) {
                        final Iterable<O> nextElementsIterable = transform(possibleNext);
                        if (Objects.nonNull(nextElementsIterable)) {
                            nextElements = nextElementsIterable.iterator();
                            if (nextElements.hasNext()) {
                                hasNext = true;
                            } else {
                                nextElements = null;
                                hasNext();
                            }
                        } else {
                            hasNext();
                        }
                    } else if (skipInvalid) {
                        hasNext();
                    } else {
                        handleInvalidItem(possibleNext);
                    }
                } else {
                    hasNext = false;
                    nextElements = null;
                }
            }

            final boolean hasNextResult = _hasNext();
            if (autoClose && !hasNextResult) {
                close();
            }

            return hasNextResult;
        }

        @Override
        public O next() {
            if (!_hasNext() && !hasNext()) {
                throw new NoSuchElementException("Reached the end of the iterator");
            }

            final O elementToReturn = nextElements.next();
            if (!nextElements.hasNext()) {
                nextElements = null;
                hasNext = null;
            }

            return elementToReturn;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(String.format("Cannot call remove on a %s iterator",
                    getIterableClass().getSimpleName()));
        }

        private boolean _hasNext() {
            return Boolean.TRUE.equals(hasNext) && Objects.nonNull(nextElements) && nextElements.hasNext();
        }
    }
}
