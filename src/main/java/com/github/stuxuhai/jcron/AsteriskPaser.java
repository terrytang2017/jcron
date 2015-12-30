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

public class AsteriskPaser extends AbstractPaser {

    private Set<int[]> set;
    private Set<Integer> resultSet;
    private Range<Integer> range;
    private DurationField type;
    private static final Pattern ASTERISK_PATTERN = Pattern.compile("(\\d+)#(\\d+)");

    protected AsteriskPaser(Range<Integer> range, DurationField type) {
        super(range, type);
        this.range = range;
        this.type = type;
    }

    @Override
    protected boolean matches(String cronFieldExp) throws ParseException {
        Matcher m = ASTERISK_PATTERN.matcher(cronFieldExp);
        if (m.matches()) {
            int dayOfWeek = Integer.parseInt(m.group(1));
            int sequence = Integer.parseInt(m.group(2));
            if (range.contains(dayOfWeek) && Range.closed(1, 5).contains(sequence)) {
                if (set == null) {
                    set = new HashSet<int[]>();
                }

                int[] value = { dayOfWeek, sequence };
                set.add(value);
                return true;
            } else {
                throw new ParseException(String.format("Invalid value of %s: %s, out of range.", type.name, cronFieldExp), -1);
            }
        }

        return false;
    }

    @Override
    protected Set<Integer> parse(DateTime dateTime) {
        MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
        int maxDayOfMonth = mdt.getDayOfMonth();
        mdt.setDayOfMonth(1);
        int firstDayOfWeek = mdt.getDayOfWeek();

        if (set != null) {
            if (resultSet == null) {
                resultSet = new HashSet<Integer>();
            }
            resultSet.clear();

            for (int[] value : set) {
                int dayOfWeek = value[0];
                int sequence = value[1];
                int expectDay = 0;
                if (dayOfWeek >= firstDayOfWeek) {
                    expectDay = dayOfWeek - firstDayOfWeek + 7 * (sequence - 1) + 1;
                } else {
                    expectDay = dayOfWeek - firstDayOfWeek + 7 * sequence + 1;
                }

                if (expectDay <= maxDayOfMonth) {
                    resultSet.add(expectDay);
                }
            }

            return resultSet;
        }

        return null;
    }
}
