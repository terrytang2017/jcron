package com.github.stuxuhai.jcron;

import com.google.common.collect.Range;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jayer
 * @date 2017-03-31
 */
public class MonthAbbreviationParser extends AbstractParser {

    private static final String[] MONTH_ABBREVIATIONS = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    private int index = -1;

    public MonthAbbreviationParser(Range<Integer> range, DurationField type) {
        super(range, type);
    }

    @Override
    public boolean matches(String cronFieldExp) throws ParseException {
        int i = 0;
        String cronFieldExpUpperCase = cronFieldExp.toUpperCase();
        for (String abbr : MONTH_ABBREVIATIONS) {
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
            result.add(index);
        }

        return result;
    }

}
