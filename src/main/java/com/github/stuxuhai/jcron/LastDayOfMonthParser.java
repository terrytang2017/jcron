/*
 * Author: Jayer
 * Create Date: 2015-01-13 13:24:45
 */
package com.github.stuxuhai.jcron;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import com.google.common.collect.Range;

public class LastDayOfMonthParser extends AbstractParser {

    private Set<Integer> set;
    private Set<Integer> result;
    private Range<Integer> range;
    private DurationField type;
    private static final Pattern LAST_DAY_OF_MONTH_PATTERN = Pattern.compile("(\\d+)?L");

    protected LastDayOfMonthParser(Range<Integer> range, DurationField type) {
        super(range, type);
        this.range = range;
        this.type = type;
    }

    @Override
    protected boolean matches(String cronFieldExp) throws ParseException {
        Matcher m = LAST_DAY_OF_MONTH_PATTERN.matcher(cronFieldExp);
        if (m.matches()) {
            if (set == null) {
                set = new HashSet<Integer>();
            }

            if ("L".equals(cronFieldExp)) {
                if (type.equals(DurationField.DAY_OF_MONTH)) {
                    set.add(1);
                    return true;
                }
            } else {
                int value = Integer.parseInt(m.group(1));
                if (range.contains(value)) {
                    set.add(value);
                    return true;
                } else {
                    throw new ParseException(
                            String.format("Invalid value of %s: %s, out of range %s", type.name, cronFieldExp, range.toString().replace("‥", ", ")),
                            -1);
                }
            }
        }

        return false;
    }

    @Override
    protected Set<Integer> parse(DateTime dateTime) {
        if (set != null) {
            if (result == null) {
                result = new HashSet<Integer>();
            }
            result.clear();

            MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
            int maxDayOfMonth = mdt.getDayOfMonth();
            if (type == DurationField.DAY_OF_MONTH) {
                for (Integer value : set) {
                    result.add(maxDayOfMonth - value + 1);
                }
            }

            if (type == DurationField.DAY_OF_WEEK) {
                for (int i = 0; i < 7; i++) {
                    mdt.setDayOfMonth(maxDayOfMonth - i);
                    if (set.contains(mdt.getDayOfWeek())) {
                        result.add(mdt.getDayOfMonth());
                    }
                }
            }

            return result;
        }

        return null;
    }

}
