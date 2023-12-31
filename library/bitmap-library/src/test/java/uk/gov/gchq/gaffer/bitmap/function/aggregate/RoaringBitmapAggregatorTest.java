/*
 * Copyright 2017-2021 Crown Copyright
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
package uk.gov.gchq.gaffer.bitmap.function.aggregate;

import org.junit.jupiter.api.Test;
import org.roaringbitmap.RoaringBitmap;

import uk.gov.gchq.koryphe.binaryoperator.BinaryOperatorTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.BinaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RoaringBitmapAggregatorTest extends BinaryOperatorTest {

    @Test
    public void aggregatorDealsWithNullInput() {
        RoaringBitmapAggregator roaringBitmapAggregator = new RoaringBitmapAggregator();
        final RoaringBitmap state = roaringBitmapAggregator.apply(null, null);
        assertNull(state);
    }

    @Test
    public void emptyInputBitmapGeneratesEmptyOutputBitmap() {
        RoaringBitmap bitmap1 = new RoaringBitmap();
        RoaringBitmap bitmap2 = new RoaringBitmap();
        RoaringBitmapAggregator roaringBitmapAggregator = new RoaringBitmapAggregator();
        final RoaringBitmap result = roaringBitmapAggregator.apply(bitmap1, bitmap2);
        assertEquals(0, result.getCardinality());
    }

    @Test
    public void singleInputBitmapGeneratesIdenticalOutputBitmap() {
        RoaringBitmap inputBitmap = new RoaringBitmap();
        int input1 = 123298333;
        int input2 = 342903339;
        inputBitmap.add(input1);
        inputBitmap.add(input2);

        RoaringBitmapAggregator roaringBitmapAggregator = new RoaringBitmapAggregator();
        final RoaringBitmap result = roaringBitmapAggregator.apply(inputBitmap, null);

        assertEquals(2, result.getCardinality());
        assertEquals(inputBitmap, result);
    }

    @Test
    public void threeOverlappingInputBitmapsProducesSingleSortedBitmap() {
        int[] inputs = new int[6];
        RoaringBitmap inputBitmap1 = new RoaringBitmap();
        int input1 = 23615000;
        int input2 = 23616440;
        inputBitmap1.add(input1);
        inputBitmap1.add(input2);
        inputs[0] = input1;
        inputs[1] = input2;

        RoaringBitmapAggregator roaringBitmapAggregator = new RoaringBitmapAggregator();
        RoaringBitmap state = roaringBitmapAggregator.apply(inputBitmap1, null);
        assertEquals(state, inputBitmap1);

        RoaringBitmap inputBitmap2 = new RoaringBitmap();
        int input3 = 23615003;
        int input4 = 23615018;
        inputBitmap2.add(input3);
        inputBitmap2.add(input4);
        inputs[2] = input3;
        inputs[3] = input4;
        state = roaringBitmapAggregator.apply(state, inputBitmap2);

        RoaringBitmap inputBitmap3 = new RoaringBitmap();
        int input5 = 23615002;
        int input6 = 23615036;
        inputBitmap3.add(input5);
        inputBitmap3.add(input6);
        inputs[4] = input5;
        inputs[5] = input6;
        state = roaringBitmapAggregator.apply(state, inputBitmap3);

        Arrays.sort(inputs);
        int outPutBitmapSize = state.getCardinality();
        assertEquals(6, outPutBitmapSize);
        int i = 0;
        for (final Integer value : state) {
            assertEquals((Integer) inputs[i], value);
            i++;
        }
    }

    @Override
    protected RoaringBitmapAggregator getInstance() {
        return new RoaringBitmapAggregator();
    }

    @Override
    protected Iterable<RoaringBitmapAggregator> getDifferentInstancesOrNull() {
        return null;
    }

    @Override
    protected Class<? extends BinaryOperator> getFunctionClass() {
        return RoaringBitmapAggregator.class;
    }

    @Test
    @Override
    public void shouldJsonSerialiseAndDeserialise() throws IOException {

    }
}
