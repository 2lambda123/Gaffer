/*
 * Copyright 2016-2021 Crown Copyright
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

package uk.gov.gchq.gaffer.data.element.function;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import uk.gov.gchq.gaffer.commonutil.ToStringBuilder;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.element.ElementTuple;
import uk.gov.gchq.koryphe.Since;
import uk.gov.gchq.koryphe.Summary;
import uk.gov.gchq.koryphe.impl.function.Identity;
import uk.gov.gchq.koryphe.tuple.function.TupleAdaptedFunction;
import uk.gov.gchq.koryphe.tuple.function.TupleAdaptedFunctionComposite;

import java.util.function.Function;

/**
 * An {@code ElementTransformer} is a {@link Function} which applies a series of
 * transformations to an {@link Element}.
 */
@Since("0.3.0")
@Summary("A Function which applies a series of transformations to an Element")
public class ElementTransformer extends TupleAdaptedFunctionComposite<String> {
    private final ElementTuple elementTuple = new ElementTuple();

    public Element apply(final Element element) {
        elementTuple.setElement(element);
        apply(elementTuple);
        return element;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (null == obj || getClass() != obj.getClass()) {
            return false;
        }

        final ElementTransformer that = (ElementTransformer) obj;

        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(elementTuple, that.elementTuple)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(47, 17)
                .appendSuper(super.hashCode())
                .append(elementTuple)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("elementTuple", elementTuple)
                .toString();
    }

    public static class Builder {
        private final ElementTransformer transformer;

        public Builder() {
            this(new ElementTransformer());
        }

        private Builder(final ElementTransformer transformer) {
            this.transformer = transformer;
        }

        public SelectedBuilder select(final String... selection) {
            final TupleAdaptedFunction<String, Object, Object> current = new TupleAdaptedFunction<>();
            current.setSelection(selection);
            return new SelectedBuilder(transformer, current);
        }

        public ElementTransformer build() {
            return transformer;
        }
    }

    public static final class SelectedBuilder {
        private final ElementTransformer transformer;
        private final TupleAdaptedFunction<String, Object, Object> current;

        private SelectedBuilder(final ElementTransformer transformer, final TupleAdaptedFunction<String, Object, Object> current) {
            this.transformer = transformer;
            this.current = current;
        }

        public ExecutedBuilder execute(final Function function) {
            current.setFunction(function);
            return new ExecutedBuilder(transformer, current);
        }

        public Builder project(final String... projection) {
            current.setFunction(new Identity());
            current.setProjection(projection);
            transformer.getComponents().add(current);
            return new Builder(transformer);
        }
    }

    public static final class ExecutedBuilder {
        private final ElementTransformer transformer;
        private final TupleAdaptedFunction<String, Object, Object> current;

        private ExecutedBuilder(final ElementTransformer transformer, final TupleAdaptedFunction<String, Object, Object> current) {
            this.transformer = transformer;
            this.current = current;
        }

        public ElementTransformer.SelectedBuilder select(final String... selection) {
            current.setProjection(current.getSelection().clone());
            transformer.getComponents().add(current);
            final TupleAdaptedFunction<String, Object, Object> newCurrent = new TupleAdaptedFunction<>();
            newCurrent.setSelection(selection);
            return new ElementTransformer.SelectedBuilder(transformer, newCurrent);
        }

        public Builder project(final String... projection) {
            current.setProjection(projection);
            transformer.getComponents().add(current);
            return new Builder(transformer);
        }

        public ElementTransformer build() {
            current.setProjection(current.getSelection().clone());
            return transformer;
        }
    }
}
