package com.github.stuxuhai.jcron;

import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Jayer
 * @date 2017-03-31
 */
public class LastWeekdayOfMonthParser extends AbstractParser {

    public LastWeekdayOfMonthParser(Range<Integer> range, DurationField type) {
        super(range, type);
    }

    @Override
    public boolean matches(String cronFieldExp) {
        return "LW".equals(cronFieldExp);
    }

    @Override
    public Set<Integer> parse(DateTime dateTime) {
        Set<Integer> set = new HashSet<>();
        MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
        while (mdt.getDayOfWeek() > 5) {
            mdt.addDays(-1);
        }

        set.add(mdt.getDayOfMonth());
        return set;
    }

}
