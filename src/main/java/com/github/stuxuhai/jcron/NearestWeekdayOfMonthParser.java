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
public class NearestWeekdayOfMonthParser extends AbstractParser {

    private static final Pattern NEAREST_WEEKDAY_OF_MONTH_PATTERN = Pattern.compile("(\\d+)W");
    private Set<Integer> set = new HashSet<>();

    public NearestWeekdayOfMonthParser(Range<Integer> range, DurationField type) {
        super(range, type);
    }

    @Override
    public boolean matches(String cronFieldExp) throws ParseException {
        Matcher m = NEAREST_WEEKDAY_OF_MONTH_PATTERN.matcher(cronFieldExp);
        if (m.matches()) {
            int value = Integer.parseInt(m.group(1));
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
        MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
        int maxDayOfMonth = mdt.getDayOfMonth();

        if (set != null) {
            Set<Integer> resultSet = new HashSet<>();

            for (Integer value : set) {
                if (value <= maxDayOfMonth) {
                    mdt.setDayOfMonth(value);
                    if (mdt.getDayOfWeek() < 6) {
                        resultSet.add(mdt.getDayOfMonth());
                        continue;
                    }

                    if (value + 1 <= maxDayOfMonth) {
                        mdt.setDayOfMonth(value + 1);
                        if (mdt.getDayOfWeek() < 6) {
                            resultSet.add(mdt.getDayOfMonth());
                            continue;
                        }
                    }

                    if (value - 1 > 0) {
                        mdt.setDayOfMonth(value - 1);
                        if (mdt.getDayOfWeek() < 6) {
                            resultSet.add(mdt.getDayOfMonth());
                            continue;
                        }
                    }
                }
            }

            return resultSet;
        }

        return null;
    }

}
