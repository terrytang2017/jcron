/*
 * Author: Jayer
 * Create Date: 2015-01-13 13:24:45
 */
package com.github.stuxuhai.jcron;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import com.google.common.collect.Range;

public class PoundSignParser extends AbstractParser {

    private Set<Integer> set;
    private Range<Integer> range;
    private DurationField type;

    protected PoundSignParser(Range<Integer> range, DurationField type) {
        super(range, type);
        this.range = range;
        this.type = type;
    }

    @Override
    protected boolean matches(String cronFieldExp) {
        if ("*".equals(cronFieldExp)) {
            if (set == null) {
                set = new HashSet<Integer>();
            }

            if (!type.equals(DurationField.DAY_OF_WEEK)) {
                int start = range.lowerEndpoint();
                int end = range.upperEndpoint();
                for (int i = start; i < end + 1; i++) {
                    set.add(i);
                }
            }
            return true;
        }

        return false;
    }

    @Override
    protected Set<Integer> parse(DateTime dateTime) {
        if (type.equals(DurationField.DAY_OF_WEEK)) {
            MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
            int maxDayOfMonth = mdt.getDayOfMonth();
            for (int i = 1; i <= maxDayOfMonth; i++) {
                set.add(i);
            }
        }

        return set;
    }

}
