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
public class AsteriskParser extends AbstractParser {

    public AsteriskParser(Range<Integer> range, DurationField type) {
        super(range, type);
    }

    @Override
    public boolean matches(String cronFieldExp) {
        if ("*".equals(cronFieldExp)) {
            return true;
        }

        return false;
    }

    @Override
    public Set<Integer> parse(DateTime dateTime) {
        Set<Integer> set = new HashSet<>();
        if (getType().equals(DurationField.DAY_OF_MONTH)) {
            MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
            int maxDayOfMonth = mdt.getDayOfMonth();
            for (int i = 1; i <= maxDayOfMonth; i++) {
                set.add(i);
            }
        } else {
            int start = getRange().lowerEndpoint();
            int end = getRange().upperEndpoint();
            for (int i = start; i < end + 1; i++) {
                set.add(i);
            }
        }

        return set;
    }

}
