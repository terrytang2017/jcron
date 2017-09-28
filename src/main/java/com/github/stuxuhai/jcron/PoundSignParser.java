package com.github.stuxuhai.jcron;

import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jayer
 * @date 2017-03-31
 */
public class PoundSignParser extends AbstractParser {

    private static final Pattern ASTERISK_PATTERN = Pattern.compile("(\\d+)#(\\d+)");
    private Set<int[]> set = new HashSet<>();

    public PoundSignParser(Range<Integer> range, DurationField type) {
        super(range, type);
    }

    @Override
    public boolean matches(String cronFieldExp) throws ParseException {
        Matcher m = ASTERISK_PATTERN.matcher(cronFieldExp);
        if (m.matches()) {
            int dayOfWeek = Integer.parseInt(m.group(1));
            int sequence = Integer.parseInt(m.group(2));
            if (getRange().contains(dayOfWeek) && Range.closed(1, 5).contains(sequence)) {
                int[] value = {dayOfWeek, sequence};
                set.add(value);
                return true;
            } else {
                throw new ParseException(String.format("Invalid value of %s: %s, out of range.", getType().name, cronFieldExp), -1);
            }
        }

        return false;
    }

    @Override
    public Set<Integer> parse(DateTime dateTime) {
        MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
        int maxDayOfMonth = mdt.getDayOfMonth();
        mdt.setDayOfMonth(1);
        int firstDayOfWeek = mdt.getDayOfWeek();

        if (set != null) {
            Set<Integer> resultSet = new HashSet<>();
            for (int[] value : set) {
                int dayOfWeek = value[0] - 1;
                int sequence = value[1];
                int expectDay;
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
