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
public class LastDayOfMonthParser extends AbstractParser {

    private static final Pattern LAST_DAY_OF_MONTH_PATTERN = Pattern.compile("(\\d+)?L");
    private Set<Integer> set = new HashSet<>();

    public LastDayOfMonthParser(Range<Integer> range, DurationField type) {
        super(range, type);
    }

    @Override
    public boolean matches(String cronFieldExp) throws ParseException {
        Matcher m = LAST_DAY_OF_MONTH_PATTERN.matcher(cronFieldExp);
        if (m.matches()) {
            if ("L".equals(cronFieldExp)) {
                if (getType().equals(DurationField.DAY_OF_MONTH)) {
                    set.add(1);
                    return true;
                }
            } else {
                int value = Integer.parseInt(m.group(1));
                if (getRange().contains(value)) {
                    set.add(value);
                    return true;
                } else {
                    throw new ParseException(
                            String.format("Invalid value of %s: %s, out of range %s", getType().name, cronFieldExp, getRange().toString().replace("‥", ", ")),
                            -1);
                }
            }
        }

        return false;
    }

    @Override
    public Set<Integer> parse(DateTime dateTime) {
        if (set != null) {
            Set<Integer> result = new HashSet<>();

            MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
            int maxDayOfMonth = mdt.getDayOfMonth();
            if (getType() == DurationField.DAY_OF_MONTH) {
                for (Integer value : set) {
                    result.add(maxDayOfMonth - value + 1);
                }
            }

            if (getType() == DurationField.DAY_OF_WEEK) {
                for (int i = 0; i < 7; i++) {
                    mdt.setDayOfMonth(maxDayOfMonth - i);
                    if (set.contains((mdt.getDayOfWeek() + 1) % 7)) {
                        result.add(mdt.getDayOfMonth());
                    }
                }
            }

            return result;
        }

        return null;
    }

}
