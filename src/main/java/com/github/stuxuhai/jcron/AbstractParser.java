/*
 * Author: Jayer
 * Create Date: 2015-01-13 13:24:45
 */
package com.github.stuxuhai.jcron;

import java.text.ParseException;
import java.util.Set;

import org.joda.time.DateTime;

import com.google.common.collect.Range;

public abstract class AbstractParser {

    protected enum DurationField {

        SECOND(0, "second"), MINUTE(1, "minute"), HOUR(2, "hour"), DAY_OF_MONTH(3, "day-of-month"), MONTH(4, "month"), DAY_OF_WEEK(5,
                "day-of-week"), YEAR(6, "year");

        final int index;
        final String name;

        DurationField(int index, String name) {
            this.index = index;
            this.name = name;
        }
    }

    protected AbstractParser(Range<Integer> range, DurationField type) {
    }

    abstract protected boolean matches(String cronFieldExp) throws ParseException;

    abstract protected Set<Integer> parse(DateTime dateTime);

}
