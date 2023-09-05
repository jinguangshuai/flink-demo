package com.example.demo.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @Auther：jinguangshuai
 * @Data：2023/8/3 - 08 - 03 - 10:44
 * @Description:com.example.demo.utils
 * @version:1.0
 */
@Slf4j
public class DateUtil {
    private static final String DATE_PATTERN = "[0-9]{4}-[0-9]{2}-[0-9]{2}";

    private static final String DATETIME_PATTERN =  "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}";

    //日期转换
    public static Date processInUseDate(Object obj) {
        if (obj instanceof Date) {
            return (Date) obj;
        }
        if (obj instanceof String) {
            Pattern datePattern = Pattern.compile(DATE_PATTERN);
            Pattern dateTimePattern = Pattern.compile(DATETIME_PATTERN);
            String dt = (String) obj;
            DateFormat fmt;
            if (datePattern.matcher(dt).matches()) {
                fmt = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date retDate = fmt.parse(dt);
                    return retDate;
                } catch (ParseException e) {
                    log.error("日期转换错误： {}", e.getMessage());
                }
            }
            if (dateTimePattern.matcher(dt).matches()) {
                fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date retDate = fmt.parse(dt);
                    return retDate;
                } catch (ParseException e) {
                    log.error("日期转换错误： {}", e.getMessage());
                }
            }
        }
        if (obj instanceof Long) {
            return new Date((long) obj);
        }
        return null;
    }
    /**
     * 返回相差多少天
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public static long getDistanceDays(String startTime,String endTime){
        DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
        Date one;
        Date two;
        long days=0;
        try{
            one=df.parse(startTime);
            two=df.parse(endTime);
            long time1=one.getTime();
            long time2=one.getTime();
            long diff;
            if(time1<time2){
                diff=time2-time1;
            }else{
                diff=time1-time2;
            }
            days =diff/(1000*60*60*24);
        }catch(ParseException e){
            e.getStackTrace();
        }
        return days;
    }
    /**
     * 返回相差多少时间
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public static long getDistanceHour(String startTime,String endTime){
        DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date one;
        Date two;
        long days=0;
        long hour=0;
        try{
            one=df.parse(startTime);
            two=df.parse(endTime);
            long time1=one.getTime();
            long time2=one.getTime();
            long diff;
            if(time1<time2){
                diff=time2-time1;
            }else{
                diff=time1-time2;
            }
            hour =(diff/(60*60*1000)-days*24);
        }catch(ParseException e){
            e.getStackTrace();
        }
        return hour;
    }
    /**
     * 返回相差多少天
     *zdy
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public static String getDistanceDaysNew(String startTime, String endTime) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date one;
        Date two;
        try {
            one = df.parse(startTime);
            two = df.parse(endTime);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff;
            diff = time1 - time2;
            long days=1000 * 60 * 60 * 24;
            long dff=days*31;
            if(diff <=dff){
                return "1";
            }else{
                return "0";
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
        return null;
    }
    /**
     * 返回相差多少时间
     *zdy
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public static String getDistanceHourNew(String startTime, String endTime) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date one;
        Date two;
        try {
            one = df.parse(startTime);
            two = df.parse(endTime);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff;
            diff = time1 - time2;
            long hour= 60 * 60 * 1000;
            long dff=hour*24;
            if(diff<=dff) {
                return "1";
            }else{
                return "0";
            }
        } catch (ParseException e) {
            e.getStackTrace();
        }
        return null;
    }
    public static String parseDate(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(date);
        return time;
    }
    public static long parseDateToDigital(Object date, String formatType) {
        DateFormat format = new SimpleDateFormat(formatType);
        long time = 0l;
        try {
            time = format.parse(StringUtils.join(date)).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
    public static int diffData(Date from, Date to) {
        String diffDataStr = DurationFormatUtils.formatPeriod(getDateTime(from).getTime(),
                getDateTime(to).getTime(), "y-M-d-H-m-s-S");
        int period = getPeriodYear(diffDataStr);
        return period;
    }
    public static int paseDate(Date from, Date to) {
        if (from == null || to == null) {
            return -1;
        }
        if (from.after(to)) {
            return -1;
        }
        String diffDataStr = DurationFormatUtils.formatPeriod(getDateTimeToDefect(from).getTime(),
                getDateTimeToDefect(to).getTime(), "y-M-d-H-m-s-S");
        int period = getPeriodYear(diffDataStr);
        return period;
    }
    /**
     * <p>
     * 描述：计算运行年限的时间差：多一毫秒及以上都算多一年;传参使用y-M-d-H-m-s-S格式的formatPeriod值
     * </p>
     *
     * @param diffDate
     * @return year
     * @author:司刘勇 2018年10月8日 下午1:45:47
     */
    public static int getPeriodYear(String diffDate) {
        String[] dt = new String[7];
        dt = diffDate.split("-");
        int year = Integer.parseInt(dt[0]);
        if (!StringUtils.isEmpty(dt[1]) && Integer.parseInt(dt[1]) > 0) {
            year++;
        } else if (!StringUtils.isEmpty(dt[2]) && Integer.parseInt(dt[2]) > 0) {
            year++;
        } else if (!StringUtils.isEmpty(dt[3]) && Integer.parseInt(dt[3]) > 0) {
            year++;
        } else if (!StringUtils.isEmpty(dt[4]) && Integer.parseInt(dt[4]) > 0) {
            year++;
        } else if (!StringUtils.isEmpty(dt[5]) && Integer.parseInt(dt[5]) > 0) {
            year++;
        } else if (!StringUtils.isEmpty(dt[6]) && Integer.parseInt(dt[6]) > 0) {
            year++;
        }
        return year;
    }
    /**
     * <p>
     * 描述：计算运行年限的时间差：多一毫秒及以上都算多一年;传参使用y-M-d-H-m-s-S格式的formatPeriod值
     * </p>
     *
     * @param diffDate
     * @return year
     * @author:司刘勇 2018年10月8日 下午1:45:47
     */
    public static int getPeriodDate(String diffDate) {
        String[] dt = new String[7];
        dt = diffDate.split("-");
        int date = Integer.parseInt(dt[2]);
        if (!StringUtils.isEmpty(dt[1]) && Integer.parseInt(dt[1]) > 0) {
            date++;
        } else if (!StringUtils.isEmpty(dt[2]) && Integer.parseInt(dt[2]) > 0) {
            date++;
        } else if (!StringUtils.isEmpty(dt[3]) && Integer.parseInt(dt[3]) > 0) {
            date++;
        } else if (!StringUtils.isEmpty(dt[4]) && Integer.parseInt(dt[4]) > 0) {
            date++;
        } else if (!StringUtils.isEmpty(dt[5]) && Integer.parseInt(dt[5]) > 0) {
            date++;
        } else if (!StringUtils.isEmpty(dt[6]) && Integer.parseInt(dt[6]) > 0) {
            date++;
        }
        return date;
    }
    protected static Date getDateTime(Date date) {
        Calendar calendar = DateUtils.toCalendar(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    protected static Date getDateTimeToDefect(Date date) {
        Calendar calendar = DateUtils.toCalendar(date);
        return calendar.getTime();
    }
    /**
     * 获取某年第一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getYearFirst(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        Date currYearFirst = calendar.getTime();
        return currYearFirst;
    }
    /**
     * 转换指定格式日期
     */
    public static String parseDate(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }
    /**
     * String类型的时间转成Date类型
     */
    public static Date parseStrToDate(String time, String format) {
        Date date = new Date();
        try {
            DateFormat fmt = new SimpleDateFormat(format);
            date = fmt.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    /**
     * 负荷分析使用较多
     * 用于将"202001190200"类型的数据转换成时间
     */
    public static Date parseSpecialFormatToTime(String time) {
        DateFormat fmt = new SimpleDateFormat("yyyyMMddHHmm");
        Date parsedTime = null;
        try {
            parsedTime = fmt.parse(time);
        } catch (ParseException e) {
            log.error("用于将\"202001190200\"类型的数据转换成时间异常{}", e.getMessage());
        }
        return parsedTime;
    }
    /**
     * 负荷分析使用较多
     * 将时间转换成yyyy-MM-dd
     */
    public static String ymdFormat(Date time) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        return fmt.format(time);
    }
    public static String strFormat(String string) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date date=null;
        try{
            date=fmt.parse(string);
        }catch (Exception e){
        }
        return fmt.format(date);
    }
    /**
     * 获取往前推n天时间
     */
    public static Date getTimeBeforeNDays(Date date, int n) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setTime(date);
        c.add(Calendar.DAY_OF_YEAR, -1 * n);
        return c.getTime();
    }
    /**
     * 获取往前推n天时间
     */
    public static Date addDay(Date date, int n) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setTime(date);
        c.add(Calendar.DAY_OF_YEAR, n);
        return c.getTime();
    }
    /**
     * 获取日期，该日期为某周的第一天，根据{@code date}获取所在的周。
     *
     * @param date
     * @return
     */
    public static Date getFirstDateInWeek(Date date) {
        assert date != null;
        Calendar c = Calendar.getInstance();
        Calendar first = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        first.setFirstDayOfWeek(Calendar.MONDAY);
        c.clear();
        c.setTime(date);
        first.clear();
        first.setWeekDate(c.getWeekYear(), c.get(Calendar.WEEK_OF_YEAR), Calendar.MONDAY);
        return first.getTime();
    }
    /**
     * 获取日期，该日期为某周的最后一天，根据{@code date}获取所在的周。
     *
     * @param date
     * @return
     */
    public static Date getLastDateInWeek(Date date) {
        assert date != null;
        Calendar c = Calendar.getInstance();
        Calendar last = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        last.setFirstDayOfWeek(Calendar.MONDAY);
        c.clear();
        c.setTime(date);
        last.clear();
        last.setWeekDate(c.getWeekYear(), c.get(Calendar.WEEK_OF_YEAR), Calendar.SUNDAY);
        last.set(Calendar.HOUR, 23);
        last.set(Calendar.MINUTE, 59);
        last.set(Calendar.SECOND, 59);
        last.set(Calendar.MILLISECOND, 999);
        return last.getTime();
    }
    /**
     * 获取日期，该日期为当前周的第一天
     *
     * @return
     */
    public static Date getFirstDateInWeek() {
        return getFirstDateInWeek(new Date());
    }
    /**
     * 获取日期，该日期为当前周的最后一天
     *
     * @return
     */
    public static Date getLastDateInWeek() {
        return getLastDateInWeek(new Date());
    }
    /**
     * 获取日期，该日期为某月的第一天，根据{@code date}获取所在的月份。
     *
     * @param date
     * @return
     */
    public static Date getFirstDateInMonth(Date date) {
        assert date != null;
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setTime(date);
        Calendar first = Calendar.getInstance();
        first.clear();
        first.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1);
        return first.getTime();
    }
    /**
     * 获取日期，该日期为某月的最后一天，根据{@code date}获取所在的月份。
     *
     * @param date
     * @return
     */
    public static Date getLastDateInMonth(Date date) {
        assert date != null;
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setTime(date);
        Calendar last = Calendar.getInstance();
        last.clear();
        last.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, 0);
        last.set(Calendar.HOUR, 23);
        last.set(Calendar.MINUTE, 59);
        last.set(Calendar.SECOND, 59);
        last.set(Calendar.MILLISECOND, 999);
        return last.getTime();
    }
    /**
     * 获取当前月份第一天的日期
     *
     * @return
     */
    public static Date getFirstDateInMonth() {
        return getFirstDateInMonth(new Date());
    }
    /**
     * 获取当前月份最后一天的日期
     *
     * @return
     */
    public static Date getLastDateInMonth() {
        return getLastDateInMonth(new Date());
    }
    /**
     * 获取日期，该日期为某年的第一天，根据{@code date}获取所在的月份。
     *
     * @param date
     * @return
     */
    public static Date getFirstDateInYear(Date date) {
        assert date != null;
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setTime(date);
        Calendar first = Calendar.getInstance();
        first.clear();
        first.set(c.get(Calendar.YEAR), 0, 1);
        return first.getTime();
    }
    /**
     * 获取日期，该日期为某年的最后一天，根据{@code date}获取所在的月份。
     *
     * @param date
     * @return
     */
    public static Date getLastDateInYear(Date date) {
        assert date != null;
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setTime(date);
        Calendar last = Calendar.getInstance();
        last.clear();
        last.set(c.get(Calendar.YEAR) + 1, 0, 0);
        last.set(Calendar.HOUR, 23);
        last.set(Calendar.MINUTE, 59);
        last.set(Calendar.SECOND, 59);
        last.set(Calendar.MILLISECOND, 999);
        return last.getTime();
    }
    /**
     * 获取当前年份第一天的日期
     *
     * @return
     */
    public static Date getFirstDateInYear() {
        return getFirstDateInYear(new Date());
    }
    /**
     * 获取当前年份最后一天的日期
     *
     * @return
     */
    public static Date getLastDateInYear() {
        return getLastDateInYear(new Date());
    }
    public static Date getStartTimeOfDate(Date date) {
        assert date != null;
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setTime(date);
        Calendar start = Calendar.getInstance();
        start.clear();
        start.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
        start.set(Calendar.HOUR, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        return start.getTime();
    }
    public static Date getEndTimeOfDate(Date date) {
        assert date != null;
        Calendar c = Calendar.getInstance();
        c.clear();
        c.setTime(date);
        Calendar end = Calendar.getInstance();
        end.clear();
        end.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
        end.set(Calendar.HOUR, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        return end.getTime();
    }
    public static Date processInUseDate1(String obj) {
        StringBuilder sb = new StringBuilder();
        if (obj != null && !StringUtils.isEmpty(obj) &&!obj.equals("null")) {
            String[] day = obj.split("-");
            if(day.length>2){
                String month=day[1];
                if (month != null) {
                    switch (month.trim()) {
                        case "Jan":
                            month = "01";
                            break;
                        case "Feb":
                            month = "02";
                            break;
                        case "Mar":
                            month = "03";
                            break;
                        case "Apr":
                            month = "04";
                            break;
                        case "May":
                            month = "05";
                            break;
                        case "Jun":
                            month = "06";
                            break;
                        case "Jul":
                            month = "07";
                            break;
                        case "Aug":
                            month = "01";
                            break;
                        case "Sep":
                            month = "09";
                            break;
                        case "Oct":
                            month = "10";
                            break;
                        case "Nov":
                            month = "11";
                            break;
                        case "Dec":
                            month = "12";
                            break;
                    }
                    String parseDate = sb.append(day[2]).append("-").append(month).append("-").append(day[0]).toString();
                    DateFormat fmt;
                    fmt = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date retDate = fmt.parse(parseDate);
                        return retDate;
                    } catch (ParseException e) {
                        log.error("日期转换错误： {}", e.getMessage());
                    }
                }
            }else{
                DateFormat fmt;
                fmt = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date retDate = fmt.parse(day[0]);
                    System.out.println(day[0]);
                    return retDate;
                } catch (ParseException e) {
                    log.error("日期转换错误： {}", e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * 返回相差多少天
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public static long getDistanceDays(Date startTime, Date endTime){
        long days = 0l;
        long time1=startTime.getTime();
        long time2=endTime.getTime();
        long diff;
        if(time1<time2){
            diff=time2-time1;
        }else{
            diff=time1-time2;
        }
        days =diff/(1000*60*60*24);
        return days;
    }
    public static String parseTimeToSpecialPattern(Integer time){
        if(null == time){
            return null;
        }
        StringBuilder sb = new StringBuilder(32);
        long days    =  time /(24*60);
        long hours   = (time - days*(24*60))/60;
        long minutes = time - days*(24*60) - hours*60;
        if(days>0){
            sb.append(days).append("天");
        }
        if(hours>0){
            sb.append(hours).append("小时");
        }
        if(minutes>0){
            sb.append(minutes).append("分");
        }
        return sb.toString();
    }

    /**
     * 验证日期是不含时分秒否合法
     * @param  date
     * @return
     * 2022-08-31
     */
    public static boolean isDate(String date) {
        StringBuffer reg = new StringBuffer(
                "^(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29)");
        Pattern p = Pattern.compile(reg.toString());
        return p.matcher(date).matches();
    }

    /**
     * 验证日期含有时分秒是否合法
     * @param  date
     * @return
     * 2022-08-31
     */
    public static boolean isDateHasTime(String date) {
        StringBuffer reg = new StringBuffer(
                "((([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))|((([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|[2468][048]|[3579][26])00))-02-29))([ ])([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])");
        Pattern p = Pattern.compile(reg.toString());
        return p.matcher(date).matches();
    }
}
