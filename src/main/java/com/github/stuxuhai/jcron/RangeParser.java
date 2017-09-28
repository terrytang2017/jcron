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
public class RangeParser extends AbstractParser {

    private static final Pattern RANGE_PATTERN = Pattern.compile("(\\d+)-(\\d+)");
    private Set<Integer> set = new HashSet<>();

    public RangeParser(Range<Integer> range, DurationField type) {
        super(range, type);
    }

    @Override
    public boolean matches(String cronFieldExp) throws ParseException {
        Matcher m = RANGE_PATTERN.matcher(cronFieldExp);
        if (m.matches()) {
            int from = Integer.parseInt(m.group(1));
            int to = Integer.parseInt(m.group(2));
            if (from <= to && getRange().contains(from) && getRange().contains(to)) {
                for (int i = from; i <= to; i++) {
                    set.add(i);
                }

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
