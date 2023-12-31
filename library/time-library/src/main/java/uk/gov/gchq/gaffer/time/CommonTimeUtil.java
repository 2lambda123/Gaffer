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

package uk.gov.gchq.gaffer.time;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjuster;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

/**
 * Utility methods for dates and times.
 */
public final class CommonTimeUtil {
    public static final long MILLISECONDS_IN_SECOND = 1000L;
    public static final long MILLISECONDS_IN_MINUTE = 60 * MILLISECONDS_IN_SECOND;
    public static final long MILLISECONDS_IN_HOUR = 60 * MILLISECONDS_IN_MINUTE;
    public static final long MILLISECONDS_IN_DAY = 24 * MILLISECONDS_IN_HOUR;
    public static final long MILLISECONDS_IN_WEEK = 7 * MILLISECONDS_IN_DAY;

    private CommonTimeUtil() {
        // Private constructor to prevent instantiation.
    }

    /**
     * Place a time value (a Java {@link Long} representing the number of
     * milliseconds since the start of the Unix epoch) in a {@link TimeBucket}.
     *
     * @param time   the time, in milliseconds since the start of the Unix epoch
     * @param bucket the time bucket to place the time value into
     * @return the value of the time bucket
     */
    public static long timeToBucket(final long time, final TimeBucket bucket) {
        final OffsetDateTime dateTime = Instant.ofEpochMilli(time).atOffset(ZoneOffset.UTC);

        final long timeBucket;

        switch (bucket) {
            case MILLISECOND:
                timeBucket = dateTime.truncatedTo(MILLIS).toInstant().toEpochMilli();
                break;
            case SECOND:
                timeBucket = dateTime.truncatedTo(SECONDS).toInstant().toEpochMilli();
                break;
            case MINUTE:
                timeBucket = dateTime.truncatedTo(MINUTES).toInstant().toEpochMilli();
                break;
            case HOUR:
                timeBucket = dateTime.truncatedTo(HOURS).toInstant().toEpochMilli();
                break;
            case DAY:
                timeBucket = dateTime.truncatedTo(DAYS).toInstant().toEpochMilli();
                break;
            case WEEK:
                timeBucket = dateTime.with(firstDayOfWeek()).truncatedTo(DAYS).toInstant().toEpochMilli();
                break;
            case MONTH:
                timeBucket = dateTime.with(firstDayOfMonth()).truncatedTo(DAYS).toInstant().toEpochMilli();
                break;
            case YEAR:
                timeBucket = dateTime.with(firstDayOfYear()).truncatedTo(DAYS).toInstant().toEpochMilli();
                break;
            default:
                timeBucket = time;
        }

        return timeBucket;
    }

    public static long timeToBucketStart(final long time, final TimeBucket bucket) {
        return timeToBucket(time, bucket);
    }

    public static long timeToBucketEnd(final long time, final TimeBucket bucket) {
        final long startTime = timeToBucketStart(time, bucket);

        final long endTime;
        switch (bucket) {
            case MILLISECOND:
                endTime = startTime;
                break;
            case SECOND:
                endTime = startTime + MILLISECONDS_IN_SECOND - 1;
                break;
            case MINUTE:
                endTime = startTime + MILLISECONDS_IN_MINUTE - 1;
                break;
            case HOUR:
                endTime = startTime + MILLISECONDS_IN_HOUR - 1;
                break;
            case DAY:
                endTime = startTime + MILLISECONDS_IN_DAY - 1;
                break;
            case WEEK:
                endTime = startTime + MILLISECONDS_IN_WEEK - 1;
                break;
            case MONTH:
                endTime = Instant.ofEpochMilli(startTime).atOffset(ZoneOffset.UTC).with(lastDayOfMonth()).toInstant().toEpochMilli() + MILLISECONDS_IN_DAY - 1;
                break;
            case YEAR:
                endTime = Instant.ofEpochMilli(startTime).atOffset(ZoneOffset.UTC).with(lastDayOfYear()).toInstant().toEpochMilli() + MILLISECONDS_IN_DAY - 1;
                break;
            default:
                endTime = time;
        }

        return endTime;
    }

    /**
     * {@link java.time.temporal.TemporalAdjuster} to select the first day of
     * the week from a {@link java.time.OffsetDateTime} object.
     *
     * @return the first day of week adjuster
     */
    private static TemporalAdjuster firstDayOfWeek() {
        return t -> t.with(DAY_OF_WEEK, 1);
    }

    /**
     * Type representing a "bucket" of time. Note that <code>MILLISECOND</code>
     * is at the end as it was added later and some serialisers use the ordinal
     * associated to the enum. Adding it at the end should avoid breaking
     * existing serialisers.
     */
    public enum TimeBucket {
        SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR, MILLISECOND
    }
}
