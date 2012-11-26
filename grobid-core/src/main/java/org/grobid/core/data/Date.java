package org.grobid.core.data;

//import java.util.Date;

/**
 * Class for representing a date.
 * We use our own representation of dates for having a comparable which prioritize the most fully specified
 * dates first, then the earliest date, i.e.:
 * 10.2010 < 2010
 * 20.10.2010 < 10.2010
 * 19.10.2010 < 20.10.2010
 * 1999 < 10.2000
 * 10.1999 < 2000
 * which is not the same as a comparison in term of the time flow only.
 * For comparing dates in term of strict time flow, please use java.util.Date + java.util.Calendar
 *
 * @author Patrice Lopez
 */
public class Date implements Comparable {
    private int day = -1;
    private int month = -1;
    private int year = -1;
    private String rawDate = null;
    private String dayString = null;
    private String monthString = null;
    private String yearString = null;

    public int getDay() {
        return day;
    }

    public void setDay(int d) {
        day = d;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int d) {
        month = d;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int d) {
        year = d;
    }

    public String getRawDate() {
        return rawDate;
    }

    public void setRawDate(String s) {
        rawDate = s;
    }

    public String getDayString() {
        return dayString;
    }

    public void setDayString(String d) {
        dayString = d;
    }

    public String getMonthString() {
        return monthString;
    }

    public void setMonthString(String d) {
        monthString = d;
    }

    public String getYearString() {
        return yearString;
    }

    public void setYearString(String d) {
        yearString = d;
    }

    /*public java.util.Date getJavaDate() {
         java.util.Calendar cal = new java.util.Calendar();
         cal.set(year, month, day);
     }*/

    /**
     * The lowest date always win.
     */
    public int compareTo(Date another) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (another.getYear() == -1) {
            return BEFORE;
        } else if (year == -1) {
            return AFTER;
        } else if (year < another.getYear()) {
            return BEFORE;
        } else if (year > another.getYear()) {
            return AFTER;
        } else {
            // years are identical
            if (another.getMonth() == -1) {
                return BEFORE;
            } else if (month == -1) {
                return AFTER;
            } else if (month < another.getMonth()) {
                return BEFORE;
            } else if (month > another.getMonth()) {
                return AFTER;
            } else {
                // months are identical
                if (another.getDay() == -1) {
                    return BEFORE;
                } else if (day == -1) {
                    return AFTER;
                } else if (day < another.getDay()) {
                    return BEFORE;
                } else if (day > another.getDay()) {
                    return AFTER;
                }
            }
        }

        return EQUAL;
    }

    public int compareTo(Object another) {
        return compareTo(((Date) another));
    }

    public boolean notNull() {
        if ((rawDate == null) &
                (dayString == null) &
                (monthString == null) &
                (yearString == null) &
                (day == -1) &
                (month == -1) &
                (year == -1))
            return false;
        else
            return true;
    }

    public boolean isAmbiguous() {
        return false;
    }

    public String toString() {
        String theDate = "";
        if (day != -1) {
            theDate += day + "-";
        }
        if (month != -1) {
            theDate += month + "-";
        }
        if (year != -1) {
            theDate += year;
        }

        theDate += " / ";

        if (dayString != null) {
            theDate += dayString + "-";
        }
        if (monthString != null) {
            theDate += monthString + "-";
        }
        if (yearString != null) {
            theDate += yearString;
        }

        return theDate;
    }

    public String toTEI() {
		// TEI uses ISO 8601 for date encoding
        String theDate = "<date when=\"";
        if (year != -1) {
            theDate += year;
        }
        if (month != -1) {
            theDate += "-" + month;
        }
        if (day != -1) {
            theDate += "-" + day;
        }

		if (rawDate != null) {
        	theDate += "\">"+rawDate+"</date>";
		}
		else {
			theDate += "\" />";
		}
			
        return theDate;
    }

    public String toXML() {
        String theDate = "<date>";
        if (day != -1) {
            theDate += "<day>" + day + "</day>";
        }
        if (month != -1) {
            theDate += "<month>" + month + "</month>";
        }
        if (year != -1) {
            theDate += "<year>" + year + "</year>";
        }

        theDate += "</date>";

        return theDate;
    }

}