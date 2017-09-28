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
public class StepParser extends AbstractParser {

    private static final Pattern STEP_PATTERN = Pattern.compile("(\\d+|\\*)/(\\d+)");
    private Set<Integer> set = new HashSet<>();

    public StepParser(Range<Integer> range, DurationField type) {
        super(range, type);
    }

    @Override
    public boolean matches(String cronFieldExp) throws ParseException {
        Matcher m = STEP_PATTERN.matcher(cronFieldExp);
        if (m.matches()) {
            int start = m.group(1).equals("*") ? 0 : Integer.parseInt(m.group(1));
            int step = Integer.parseInt(m.group(2));
            if (step > 0 && getRange().contains(step) && getRange().contains(start)) {
                for (int i = start; getRange().contains(i); i += step) {
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
        if (getType() == DurationField.DAY_OF_WEEK) {
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
