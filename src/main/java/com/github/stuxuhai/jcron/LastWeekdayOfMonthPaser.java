/*
 * Author: Jayer
 * Create Date: 2015-01-13 13:24:45
 */
package com.github.stuxuhai.jcron;

import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import com.google.common.collect.Range;

public class LastWeekdayOfMonthPaser extends AbstractPaser {

    private Set<Integer> set;

    protected LastWeekdayOfMonthPaser(Range<Integer> range, DurationField type) {
        super(range, type);
    }

    @Override
    protected boolean matches(String cronFieldExp) {
        return "LW".equals(cronFieldExp);
    }

    @Override
    protected Set<Integer> parse(DateTime dateTime) {
        MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
        while (mdt.getDayOfWeek() > 5) {
            mdt.addDays(-1);
        }

        set.add(mdt.getDayOfMonth());
        return set;
    }

}
