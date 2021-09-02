package Utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


//•A year y is represented by the integer y - 1900. 
//•A month is represented by an integer from 0 to 11; 0 is January, 1 is February, and so forth; thus 11 is December. 
//•A date (day of month) is represented by an integer from 1 to 31 in the usual manner. 
//•An hour is represented by an integer from 0 to 23. Thus, the hour from midnight to 1 a.m. is hour 0, and the hour from noon to 1 p.m. is hour 12. 
//•A minute is represented by an integer from 0 to 59 in the usual manner. 
//•A second is represented by an integer from 0 to 61; the values 60 and 61 occur only for leap seconds and even then only in Java implementations that actually track leap seconds correctly. Because of the manner in which leap seconds are currently introduced, it is extremely unlikely that two leap seconds will occur in the same minute, but this specification follows the date and time conventions for ISO C. 



public class Clock {
	
	String dateString;
	Date date;
	GregorianCalendar calendar;
	boolean  useLeapYear = true ;
	
	int year=0;
	int month=0;
	int day=0;
	int hour=0;
	int minute=0;
	
	public static final int daysBetween1950And1970= 7305;
	public static final int daysBetween19481950 =731;

	public static final int daysInTheMonth[] = {31,28,31,30,31,30,31,31,30,31,30,31};
	
	// 
	public static String[] months= new String[]{
			"January","February","March","April","May","June",
			"July","August","September","October","November","December"};

	

	/**
	 * Constructor. Sets the clock to 1990.
	 */
	public Clock() {
		calendar= new GregorianCalendar(1990, 0, 0);
		update();
//		System.out.println("leap years: "+ numberOfLeapYears());
	}
	
	public Clock(int year) {
		calendar= new GregorianCalendar(year, 0, 0) ;
	}

	/**
	 * Constructor. Using our since 1950 (NORWECOM.E2E "standard")
	 * @param hourSince1950 the number of hours since 1950
	 */
	public Clock(int hourSince1950, boolean useLeap){
		date= new Date(msSince1970(hourSince1950));
		calendar= new GregorianCalendar();
		calendar.setTimeInMillis(msSince1970(hourSince1950));
		update();
//		System.out.println("leap years: "+ numberOfLeapYears());
	}

	/**
	 * Steps one day forward in the calendar
	 * @param dayStep the number of days to step forward in the Calendar
	 */
	public void stepDay(int dayStep){
		calendar.add(Calendar.DAY_OF_MONTH, dayStep);
		update();
	}

	/**
	 * Gives a string representation of the current month in the calendar
	 * @return a string representation of the month
	 */
	public String whichMonth(){
		String m= months[calendar.get(GregorianCalendar.MONTH)];
		return m;
	}
	
	/**
	 * Finds the indexes of the month given the text string
	 * @param s The text string
	 * return an integer between zero and 12
	 */
	public static int indexOfMonth(String s){
		int index= 0;
		for (int i = 0; i < months.length; i++) {
			if (months[i].toLowerCase().contains(s.toLowerCase())) {
				index=i;
				break;
			}
		}
		return index;
	}

	
	/**
	 * Defines the calendar according to number of hours since 1950
	 * @param h1950 the number of hours since 1950 (typically NORWECOM.E2E netCDF dates)
	 */
	public void defineByHour(int h1950){
		calendar.setTimeInMillis(msSince1970(h1950));
		dateString= createADate(h1950);
	}
	
	public int numberOfLeapYears(){
		int n= 0 ;
		for (int y = 1950; y < calendar.get(Calendar.YEAR); y++) {
			n+= (isLeapYear(y)) ? 1: 0; 
		}
		return n;
	} 
	
	public static boolean isLeapYear(int year) {
		  Calendar cal = Calendar.getInstance();
		  cal.set(Calendar.YEAR, year);
		  return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
		}

	/**
	 * Creates a date string given the number of milliseconds since 1970
	 * @param ms the number of milliseconds since 1970
	 * @return a string representation of the date
	 */
	public static String dateStringFrom1970ms(long ms){
		String s="";
		SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd");//yyyy-MM-dd:hh:mm");
		Date d= new Date(ms); 
		s= dateFormat.format(d);
				
		return s;
	}
	
	/**
	 * Converts hours since January 1950 (which is a NORWECOM.E2E model convention)
	 * to "milli seconds" (ms) since January 1970 (which is a Java convention) compatible with the
	 * java.util.Date class
	 * @param hourSince1950 The number of hours since January 1950
	 * @return the number of milliseconds since January 1970
	 */
	static public long msSince1970(int hourSince1950){
		long hSince1970= hourSince1950-daysBetween1950And1970*24; 
		long ms= hSince1970*3600*1000;  
		return ms;
	}
	
	
	/**
	 * Creates a string representation of a date as it is given 
	 * from the number of hours since 1950 without affecting the calendar.
	 * @param hourSince1950 the number of hours since 1950
	 * @return a string representation of a date
	 */
	static public String createADate(int hourSince1950){
		String result="";
		long ms = msSince1970(hourSince1950) ;
		result= dateStringFrom1970ms(ms);
		return result;
	}
	
	/**
	 * Updates the date and the date string according to the calendar for consistency
	 */
	public void update(){
		date= calendar.getTime() ;
		dateString= dateStringFrom1970ms(date.getTime());
		day= getCalendar().get(Calendar.DAY_OF_MONTH);
	}

	public String dayMonthYearString(){
		
		String s="";
		String  month=whichMonth();
		day= getCalendar().get(Calendar.DAY_OF_MONTH);
		year=getCalendar().get(Calendar.YEAR);
	    s= String.format(day+" "+  month+" "+year);
		return s;
	}
	
	/**
	 * Creates a string representation of the form "x January 1999" given the number of hours since 1950
	 * @param hourSince1950 the number of hours since January 1950
	 * @return a string representation of the date represented by the number of hours since 1950
	 */
	static public String dayMonthYearString(int hourSince1950){
		Clock c= new Clock(hourSince1950,true);
		return c.dayMonthYearString();
	} 
	
	/**
	 * Creates a date string by first assuring an update
	 * @return the date string
	 */
	public String createsDateString(){
		update();
		return dateString;
	}

	/** Gets the date string
	 * @return the dateString
	 */
	public String getDateString() {
		return dateString;
	}


	/** Gets the calendar
	 * @return the calendar
	 */
	public GregorianCalendar getCalendar() {
		return calendar;
	}

	/** Sets the calendar
	 * @param calendar the calendar to set
	 */
	public void setCalendar(GregorianCalendar calendar) {
		this.calendar = calendar;
	}


	public static void main(String[] args) {
		Clock clock=new Clock();
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}
}
