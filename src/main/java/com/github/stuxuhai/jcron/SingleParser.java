package com.github.stuxuhai.jcron;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jayer
 * @date 2017-03-31
 */
public class SingleParser extends AbstractParser {

    private Set<Integer> set = new HashSet<>();

    public SingleParser(Range<Integer> range, DurationField type) {
        super(range, type);
    }

    @Override
    public boolean matches(String cronFieldExp) throws ParseException {
        if (CharMatcher.digit().matchesAllOf(cronFieldExp)) {
            int value = Integer.parseInt(cronFieldExp);
            if (getRange().contains(value)) {
                set.add(value);
                return true;
            } else {
                throw new ParseException(
                        String.format("Invalid value of %s: %s, out of range %s", getType().name, cronFieldExp, getRange().toString().replace("‥", ", ")), -1);
            }
        }

        return false;
    }

    @Override
    public Set<Integer> parse(DateTime dateTime) {
        if (getType().equals(DurationField.DAY_OF_WEEK)) {
            if (set != null) {
                Set<Integer> result = new HashSet<>();

                MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
                int maxDayOfMonth = mdt.getDayOfMonth();
                for (int i = 1; i <= maxDayOfMonth; i++) {
                    mdt.setDayOfMonth(i);
                    if (set.contains((mdt.getDayOfWeek() + 1) % 7)) {
                        result.add(mdt.getDayOfMonth());
                    }
                }

                return result;
            }
        }

        return set;
    }
}
