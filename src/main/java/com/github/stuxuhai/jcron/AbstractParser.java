package com.github.stuxuhai.jcron;

import com.google.common.collect.Range;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.Set;

/**
 * @author Jayer
 * @date 2017-03-31
 */
public abstract class AbstractParser {

    private Range<Integer> range;
    private DurationField type;

    public AbstractParser(Range<Integer> range, DurationField type) {
        this.range = range;
        this.type = type;
    }

    abstract public boolean matches(String cronFieldExp) throws ParseException;

    abstract public Set<Integer> parse(DateTime dateTime);

    public Range<Integer> getRange() {
        return range;
    }

    public DurationField getType() {
        return type;
    }

    public enum DurationField {

        SECOND(0, "second"), MINUTE(1, "minute"), HOUR(2, "hour"), DAY_OF_MONTH(3, "day-of-month"), MONTH(4, "month"), DAY_OF_WEEK(5,
                "day-of-week"), YEAR(6, "year");

        final int index;
        final String name;

        DurationField(int index, String name) {
            this.index = index;
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }
    }
}
