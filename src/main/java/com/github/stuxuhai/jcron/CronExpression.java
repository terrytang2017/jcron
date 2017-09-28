package com.github.stuxuhai.jcron;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.text.ParseException;
import java.util.*;

/**
 * Provides a parser and evaluator for unix-like cron expressions, such as "0 0 12 * * ?".
 * Expression format: "seconds minutes hours dayOfMonth month dayOfWeek [years]"
 * <p>
 *
 * @author Jayer
 * @date 2017-03-31
 */
public class CronExpression {

    private static final Range<Integer> SECOND_RANGE = Range.closed(0, 59);
    private static final Range<Integer> MINUTE_RANGE = Range.closed(0, 59);
    private static final Range<Integer> HOUR_RANGE = Range.closed(0, 23);
    private static final Range<Integer> DAY_OF_MONTH_RANGE = Range.closed(1, 31);
    private static final Range<Integer> MONTH_RANGE = Range.closed(1, 12);
    private static final Range<Integer> DAY_OF_WEEK_RANGE = Range.closed(1, 7);
    private static final Range<Integer> YEAR_RANGE = Range.closed(1970, 2500);
    private List<AbstractParser> secondParsers;
    private List<AbstractParser> minuteParsers;
    private List<AbstractParser> hourParsers;
    private List<AbstractParser> dayOfMonthParsers;
    private List<AbstractParser> monthParsers;
    private List<AbstractParser> dayOfWeekParsers;
    private List<AbstractParser> yearParsers;
    private String expression;

    public CronExpression(String expression) {
        this.expression = expression;
        secondParsers = new ArrayList<>();
        secondParsers.add(new AsteriskParser(SECOND_RANGE, AbstractParser.DurationField.SECOND));
        secondParsers.add(new RangeParser(SECOND_RANGE, AbstractParser.DurationField.SECOND));
        secondParsers.add(new StepParser(SECOND_RANGE, AbstractParser.DurationField.SECOND));
        secondParsers.add(new SingleParser(SECOND_RANGE, AbstractParser.DurationField.SECOND));

        minuteParsers = new ArrayList<>();
        minuteParsers.add(new AsteriskParser(MINUTE_RANGE, AbstractParser.DurationField.MINUTE));
        minuteParsers.add(new RangeParser(MINUTE_RANGE, AbstractParser.DurationField.MINUTE));
        minuteParsers.add(new StepParser(MINUTE_RANGE, AbstractParser.DurationField.MINUTE));
        minuteParsers.add(new SingleParser(MINUTE_RANGE, AbstractParser.DurationField.MINUTE));

        hourParsers = new ArrayList<>();
        hourParsers.add(new AsteriskParser(HOUR_RANGE, AbstractParser.DurationField.HOUR));
        hourParsers.add(new RangeParser(HOUR_RANGE, AbstractParser.DurationField.HOUR));
        hourParsers.add(new StepParser(HOUR_RANGE, AbstractParser.DurationField.HOUR));
        hourParsers.add(new SingleParser(HOUR_RANGE, AbstractParser.DurationField.HOUR));

        dayOfMonthParsers = new ArrayList<>();
        dayOfMonthParsers.add(new AsteriskParser(DAY_OF_MONTH_RANGE, AbstractParser.DurationField.DAY_OF_MONTH));
        dayOfMonthParsers.add(new RangeParser(DAY_OF_MONTH_RANGE, AbstractParser.DurationField.DAY_OF_MONTH));
        dayOfMonthParsers.add(new StepParser(DAY_OF_MONTH_RANGE, AbstractParser.DurationField.DAY_OF_MONTH));
        dayOfMonthParsers.add(new LastDayOfMonthParser(DAY_OF_MONTH_RANGE, AbstractParser.DurationField.DAY_OF_MONTH));
        dayOfMonthParsers.add(new NearestWeekdayOfMonthParser(DAY_OF_MONTH_RANGE, AbstractParser.DurationField.DAY_OF_MONTH));
        dayOfMonthParsers.add(new SingleParser(DAY_OF_MONTH_RANGE, AbstractParser.DurationField.DAY_OF_MONTH));
        dayOfMonthParsers.add(new LastWeekdayOfMonthParser(DAY_OF_MONTH_RANGE, AbstractParser.DurationField.DAY_OF_MONTH));

        monthParsers = new ArrayList<>();
        monthParsers.add(new AsteriskParser(MONTH_RANGE, AbstractParser.DurationField.MONTH));
        monthParsers.add(new RangeParser(MONTH_RANGE, AbstractParser.DurationField.MONTH));
        monthParsers.add(new StepParser(MONTH_RANGE, AbstractParser.DurationField.MONTH));
        monthParsers.add(new SingleParser(MONTH_RANGE, AbstractParser.DurationField.MONTH));
        monthParsers.add(new MonthAbbreviationParser(MONTH_RANGE, AbstractParser.DurationField.MONTH));

        dayOfWeekParsers = new ArrayList<>();
        dayOfWeekParsers.add(new AsteriskParser(DAY_OF_WEEK_RANGE, AbstractParser.DurationField.DAY_OF_WEEK));
        dayOfWeekParsers.add(new RangeParser(DAY_OF_WEEK_RANGE, AbstractParser.DurationField.DAY_OF_WEEK));
        dayOfWeekParsers.add(new StepParser(DAY_OF_WEEK_RANGE, AbstractParser.DurationField.DAY_OF_WEEK));
        dayOfWeekParsers.add(new LastDayOfMonthParser(DAY_OF_WEEK_RANGE, AbstractParser.DurationField.DAY_OF_WEEK));
        dayOfWeekParsers.add(new PoundSignParser(DAY_OF_WEEK_RANGE, AbstractParser.DurationField.DAY_OF_WEEK));
        dayOfWeekParsers.add(new SingleParser(DAY_OF_WEEK_RANGE, AbstractParser.DurationField.DAY_OF_WEEK));
        dayOfWeekParsers.add(new WeekAbbreviationParser(DAY_OF_WEEK_RANGE, AbstractParser.DurationField.DAY_OF_WEEK));

        yearParsers = new ArrayList<>();
        yearParsers.add(new AsteriskParser(YEAR_RANGE, AbstractParser.DurationField.YEAR));
        yearParsers.add(new RangeParser(YEAR_RANGE, AbstractParser.DurationField.YEAR));
        yearParsers.add(new StepParser(YEAR_RANGE, AbstractParser.DurationField.YEAR));
        yearParsers.add(new SingleParser(YEAR_RANGE, AbstractParser.DurationField.YEAR));
    }

    private void validate(String[] exp) throws ParseException {
        if (exp.length != 7) {
            throw new ParseException("Unexpected end of expression.", -1);
        } else if ("?".equals(exp[AbstractParser.DurationField.DAY_OF_MONTH.getIndex()]) && "?".equals(exp[AbstractParser.DurationField.DAY_OF_WEEK.getIndex()])) {
            throw new ParseException("'?' can only be specified for day-of-month or day-of-week.", -1);
        } else if (!"?".equals(exp[AbstractParser.DurationField.DAY_OF_MONTH.getIndex()]) && !"?".equals(exp[AbstractParser.DurationField.DAY_OF_WEEK.getIndex()])) {
            throw new ParseException("Support for specifying both a day-of-week and a day-of-month parameter is not implemented.", -1);
        } else if ("2".equals(exp[AbstractParser.DurationField.MONTH.getIndex()]) && CharMatcher.digit().matchesAllOf(exp[AbstractParser.DurationField.DAY_OF_MONTH.getIndex()])) {
            int dayOfMonth = Integer.parseInt(exp[AbstractParser.DurationField.DAY_OF_MONTH.getIndex()]);
            if (dayOfMonth > 29) {
                throw new ParseException("When month is 2, day-of-month should be in range [1, 29].", -1);
            }
        }
    }

    private String[] appendYearField(String[] exp) {
        if (exp.length == 6) {
            String[] newExp = new String[7];
            System.arraycopy(exp, 0, newExp, 0, exp.length);
            newExp[AbstractParser.DurationField.YEAR.getIndex()] = "*";
            return newExp;
        }

        return exp;
    }

    private int searchNotLessThanIndex(List<Integer> sortedList, int value) {
        for (int i = 0, len = sortedList.size(); i < len; i++) {
            if (sortedList.get(i) >= value) {
                return i;
            }
        }

        return 0;
    }

    private int searchNotGreaterThanIndex(List<Integer> sortedList, int value) {
        for (int i = sortedList.size() - 1; i >= 0; i--) {
            if (sortedList.get(i) <= value) {
                return i;
            }
        }

        return sortedList.size() - 1;
    }

    private List<Integer> parse(List<AbstractParser> parsers, String partCronExp, DateTime dateTime, AbstractParser.DurationField type) throws ParseException {
        Set<Integer> result = new HashSet<>();
        for (String str : Splitter.on(",").omitEmptyStrings().split(partCronExp)) {
            boolean isMatch = false;
            for (AbstractParser parser : parsers) {
                if (parser.matches(str)) {
                    Set<Integer> value = parser.parse(dateTime);
                    if (value != null) {
                        result.addAll(value);
                    }
                    isMatch = true;
                    break;
                }
            }

            if (!isMatch) {
                throw new ParseException(String.format("Invalid value of %s: %s.", type.getName(), str), -1);
            }
        }

        return Ordering.natural().sortedCopy(result);
    }

    private List<Integer> parseDayValueList(String[] fixedCronExp, DateTime dateTime) throws ParseException {
        List<Integer> dayValues;
        if ("?".equals(fixedCronExp[AbstractParser.DurationField.DAY_OF_MONTH.getIndex()])) {
            dayValues = parse(dayOfWeekParsers, fixedCronExp[AbstractParser.DurationField.DAY_OF_WEEK.getIndex()], dateTime, AbstractParser.DurationField.DAY_OF_WEEK);
        } else {
            dayValues = parse(dayOfMonthParsers, fixedCronExp[AbstractParser.DurationField.DAY_OF_MONTH.getIndex()], dateTime, AbstractParser.DurationField.DAY_OF_MONTH);
        }

        return dayValues;
    }

    public DateTime getTimeAfter(DateTime dateTime) {
        try {
            String[] fixedCronExp = appendYearField(expression.split("\\s+"));
            validate(fixedCronExp);

            MutableDateTime mdt = dateTime.toMutableDateTime();
            mdt.setMillisOfSecond(0);

            List<Integer> secondValues = parse(secondParsers, fixedCronExp[AbstractParser.DurationField.SECOND.getIndex()], dateTime, AbstractParser.DurationField.SECOND);
            List<Integer> minuteValues = parse(minuteParsers, fixedCronExp[AbstractParser.DurationField.MINUTE.getIndex()], dateTime, AbstractParser.DurationField.MINUTE);
            List<Integer> hourValues = parse(hourParsers, fixedCronExp[AbstractParser.DurationField.HOUR.getIndex()], dateTime, AbstractParser.DurationField.HOUR);
            List<Integer> monthValues = parse(monthParsers, fixedCronExp[AbstractParser.DurationField.MONTH.getIndex()], dateTime, AbstractParser.DurationField.MONTH);
            List<Integer> yearValues = parse(yearParsers, fixedCronExp[AbstractParser.DurationField.YEAR.getIndex()], dateTime, AbstractParser.DurationField.YEAR);

            int yearStartIndex = searchNotLessThanIndex(yearValues, mdt.getYear());
            for (int yearIndex = yearStartIndex, yearLen = yearValues.size(); yearIndex < yearLen; yearIndex++) {
                int year = yearValues.get(yearIndex);
                mdt.setYear(year);
                int monthStartIndex = (year == dateTime.getYear()) ? searchNotLessThanIndex(monthValues, dateTime.getMonthOfYear()) : 0;

                for (int monthIndex = monthStartIndex, monthLen = monthValues.size(); monthIndex < monthLen; monthIndex++) {
                    int month = monthValues.get(monthIndex);
                    mdt.setMonthOfYear(month);
                    List<Integer> dayValues = parseDayValueList(fixedCronExp, mdt.toDateTime());
                    int dayStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear())
                            ? searchNotLessThanIndex(dayValues, dateTime.getDayOfMonth()) : 0;

                    int maxDayOfMonth = mdt.toDateTime().dayOfMonth().withMaximumValue().toLocalDate().getDayOfMonth();
                    for (int dayIndex = dayStartIndex, dayLen = dayValues.size(); dayIndex < dayLen; dayIndex++) {
                        int day = dayValues.get(dayIndex);
                        if (day > maxDayOfMonth) {
                            break;
                        }
                        mdt.setDayOfMonth(day);
                        int hourStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear() && day == dateTime.getDayOfMonth())
                                ? searchNotLessThanIndex(hourValues, dateTime.getHourOfDay()) : 0;

                        for (int hourIndex = hourStartIndex, hourLen = hourValues.size(); hourIndex < hourLen; hourIndex++) {
                            int hour = hourValues.get(hourIndex);
                            mdt.setHourOfDay(hour);
                            int minuteStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear()
                                    && day == dateTime.getDayOfMonth() && hour == dateTime.getHourOfDay())
                                    ? searchNotLessThanIndex(minuteValues, dateTime.getMinuteOfHour()) : 0;

                            for (int minuteIndex = minuteStartIndex, minuteLen = minuteValues.size(); minuteIndex < minuteLen; minuteIndex++) {
                                int minute = minuteValues.get(minuteIndex);
                                int secondStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear()
                                        && day == dateTime.getDayOfMonth() && hour == dateTime.getHourOfDay() && minute == dateTime.getMinuteOfHour())
                                        ? searchNotLessThanIndex(secondValues, dateTime.getSecondOfMinute()) : 0;
                                mdt.setMinuteOfHour(minute);
                                for (int secondIndex = secondStartIndex, secondLen = secondValues.size(); secondIndex < secondLen; secondIndex++) {
                                    int second = secondValues.get(secondIndex);
                                    mdt.setSecondOfMinute(second);
                                    if (mdt.isAfter(dateTime)) {
                                        return mdt.toDateTime();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public DateTime getTimeBefore(DateTime dateTime) {
        try {
            String[] fixedCronExp = appendYearField(expression.split("\\s+"));
            validate(fixedCronExp);

            MutableDateTime mdt = dateTime.toMutableDateTime();
            mdt.setMillisOfSecond(0);

            List<Integer> secondValues = parse(secondParsers, fixedCronExp[AbstractParser.DurationField.SECOND.getIndex()], dateTime, AbstractParser.DurationField.SECOND);
            List<Integer> minuteValues = parse(minuteParsers, fixedCronExp[AbstractParser.DurationField.MINUTE.getIndex()], dateTime, AbstractParser.DurationField.MINUTE);
            List<Integer> hourValues = parse(hourParsers, fixedCronExp[AbstractParser.DurationField.HOUR.getIndex()], dateTime, AbstractParser.DurationField.HOUR);
            List<Integer> monthValues = parse(monthParsers, fixedCronExp[AbstractParser.DurationField.MONTH.getIndex()], dateTime, AbstractParser.DurationField.MONTH);
            List<Integer> yearValues = parse(yearParsers, fixedCronExp[AbstractParser.DurationField.YEAR.getIndex()], dateTime, AbstractParser.DurationField.YEAR);

            int yearStartIndex = searchNotGreaterThanIndex(yearValues, mdt.getYear());
            for (int yearIndex = yearStartIndex; yearIndex >= 0; yearIndex--) {
                int year = yearValues.get(yearIndex);
                mdt.setYear(year);
                int monthStartIndex = (year == dateTime.getYear()) ? searchNotGreaterThanIndex(monthValues, dateTime.getMonthOfYear())
                        : monthValues.size() - 1;

                for (int monthIndex = monthStartIndex; monthIndex >= 0; monthIndex--) {
                    int month = monthValues.get(monthIndex);
                    mdt.setMonthOfYear(month);
                    List<Integer> dayValues = parseDayValueList(fixedCronExp, mdt.toDateTime());
                    int dayStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear())
                            ? searchNotGreaterThanIndex(dayValues, dateTime.getDayOfMonth()) : dayValues.size() - 1;

                    int maxDayOfMonth = mdt.toDateTime().dayOfMonth().withMaximumValue().toLocalDate().getDayOfMonth();
                    for (int dayIndex = dayStartIndex; dayIndex >= 0; dayIndex--) {
                        int day = dayValues.get(dayIndex);
                        if (day > maxDayOfMonth) {
                            break;
                        }
                        mdt.setDayOfMonth(day);
                        int hourStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear() && day == dateTime.getDayOfMonth())
                                ? searchNotGreaterThanIndex(hourValues, dateTime.getHourOfDay()) : hourValues.size() - 1;

                        for (int hourIndex = hourStartIndex; hourIndex >= 0; hourIndex--) {
                            int hour = hourValues.get(hourIndex);
                            mdt.setHourOfDay(hour);
                            int minuteStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear()
                                    && day == dateTime.getDayOfMonth() && hour == dateTime.getHourOfDay())
                                    ? searchNotGreaterThanIndex(minuteValues, dateTime.getMinuteOfHour()) : minuteValues.size() - 1;

                            for (int minuteIndex = minuteStartIndex; minuteIndex >= 0; minuteIndex--) {
                                int minute = minuteValues.get(minuteIndex);
                                mdt.setMinuteOfHour(minute);
                                int secondStartIndex = (year == dateTime.getYear() && month == dateTime.getMonthOfYear()
                                        && day == dateTime.getDayOfMonth() && hour == dateTime.getHourOfDay() && minute == dateTime.getMinuteOfHour())
                                        ? searchNotGreaterThanIndex(secondValues, dateTime.getSecondOfMinute()) : secondValues.size() - 1;

                                for (int secondIndex = secondStartIndex; secondIndex >= 0; secondIndex--) {
                                    int second = secondValues.get(secondIndex);
                                    mdt.setSecondOfMinute(second);
                                    if (mdt.isBefore(dateTime)) {
                                        return mdt.toDateTime();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public List<DateTime> getTimeAfter(DateTime dateTime, int n) throws ParseException {
        if (n < 1) {
            throw new IllegalArgumentException("n should be > 0, but given " + n);
        }

        List<DateTime> list = null;
        MutableDateTime mdt = dateTime.toMutableDateTime();
        for (int i = 0; i < n; i++) {
            DateTime value = getTimeAfter(mdt.toDateTime());
            if (value != null) {
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(value);
                mdt.setMillis(value.getMillis());
            } else {
                break;
            }
        }

        return list;
    }

    public List<DateTime> getTimeBefore(DateTime dateTime, int n) throws ParseException {
        if (n < 1) {
            throw new IllegalArgumentException("n should be > 0, but given " + n);
        }

        List<DateTime> list = null;
        MutableDateTime mdt = dateTime.toMutableDateTime();
        for (int i = 0; i < n; i++) {
            DateTime value = getTimeBefore(mdt.toDateTime());
            if (value != null) {
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(value);
                mdt.setMillis(value.getMillis());
            } else {
                break;
            }
        }

        return list;
    }

    public boolean isValid() {
        DateTime dateTime = new DateTime(1970, 1, 1, 0, 0, 0);
        try {
            return getTimeAfter(dateTime) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String evaluate() {
        DateTime dateTime = new DateTime(1970, 1, 1, 0, 0, 0);
        try {
            getTimeAfter(dateTime);
        } catch (Exception e) {
            return e.getMessage().replace("java.text.ParseException: ", "");
        }

        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        CronExpression other = (CronExpression) obj;
        return Objects.equals(expression, other.expression);
    }

    @Override
    public String toString() {
        return expression;
    }
}
