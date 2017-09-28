package com.github.stuxuhai.jcron;

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
public class WeekAbbreviationParser extends AbstractParser {

    private static final String[] WEEK_ABBREVIATIONS = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
    private int index = -1;

    public WeekAbbreviationParser(Range<Integer> range, DurationField type) {
        super(range, type);
    }

    @Override
    public boolean matches(String cronFieldExp) throws ParseException {
        int i = 0;
        String cronFieldExpUpperCase = cronFieldExp.toUpperCase();
        for (String abbr : WEEK_ABBREVIATIONS) {
            i++;
            if (abbr.equals(cronFieldExpUpperCase)) {
                index = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<Integer> parse(DateTime dateTime) {
        Set<Integer> result = new HashSet<>();
        if (index != -1) {
            MutableDateTime mdt = dateTime.dayOfMonth().withMaximumValue().toMutableDateTime();
            int maxDayOfMonth = mdt.getDayOfMonth();
            for (int i = 1; i <= maxDayOfMonth; i++) {
                mdt.setDayOfMonth(i);
                if (index == mdt.getDayOfWeek()) {
                    result.add(mdt.getDayOfMonth());
                }
            }
        }

        return result;
    }

}
