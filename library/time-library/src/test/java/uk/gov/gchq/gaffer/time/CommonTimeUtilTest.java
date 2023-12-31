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

package uk.gov.gchq.gaffer.time;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static java.time.OffsetDateTime.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.gchq.gaffer.time.CommonTimeUtil.TimeBucket.DAY;
import static uk.gov.gchq.gaffer.time.CommonTimeUtil.TimeBucket.HOUR;
import static uk.gov.gchq.gaffer.time.CommonTimeUtil.TimeBucket.MILLISECOND;
import static uk.gov.gchq.gaffer.time.CommonTimeUtil.TimeBucket.MINUTE;
import static uk.gov.gchq.gaffer.time.CommonTimeUtil.TimeBucket.MONTH;
import static uk.gov.gchq.gaffer.time.CommonTimeUtil.TimeBucket.SECOND;
import static uk.gov.gchq.gaffer.time.CommonTimeUtil.TimeBucket.WEEK;
import static uk.gov.gchq.gaffer.time.CommonTimeUtil.TimeBucket.YEAR;
import static uk.gov.gchq.gaffer.time.CommonTimeUtil.timeToBucket;

public class CommonTimeUtilTest {

    @Test
    public void shouldCorrectlyPlaceTimestampsIntoBuckets() {
        final OffsetDateTime offsetDateTime = OffsetDateTime.parse("2000-01-01T12:34:56.789Z");
        final long time = offsetDateTime.toInstant().toEpochMilli();

        final OffsetDateTime yearOffsetDateTime = OffsetDateTime.parse("2000-02-03T12:34:56.789Z");
        final long yearTime = yearOffsetDateTime.toInstant().toEpochMilli();

        assertEquals(parse("2000-01-01T12:34:56.789Z").toInstant().toEpochMilli(), timeToBucket(time, MILLISECOND));
        assertEquals(parse("2000-01-01T12:34:56.000Z").toInstant().toEpochMilli(), timeToBucket(time, SECOND));
        assertEquals(parse("2000-01-01T12:34:00.000Z").toInstant().toEpochMilli(), timeToBucket(time, MINUTE));
        assertEquals(parse("2000-01-01T12:00:00.000Z").toInstant().toEpochMilli(), timeToBucket(time, HOUR));
        assertEquals(parse("2000-01-01T00:00:00.000Z").toInstant().toEpochMilli(), timeToBucket(time, DAY));
        assertEquals(parse("1999-12-27T00:00:00.000Z").toInstant().toEpochMilli(), timeToBucket(time, WEEK));
        assertEquals(parse("2000-01-01T00:00:00.000Z").toInstant().toEpochMilli(), timeToBucket(time, MONTH));
        assertEquals(parse("2000-01-01T00:00:00.000Z").toInstant().toEpochMilli(), timeToBucket(yearTime, YEAR));
    }
}
