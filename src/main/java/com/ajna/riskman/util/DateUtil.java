package com.ajna.riskman.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

public class DateUtil {

	public  int getLastThursday(int month, int year)
	{
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, 1, 0, 0, 0); // set to first day of the month
		cal.set(Calendar.MILLISECOND, 0);

		int firstDay = cal.get(Calendar.DAY_OF_WEEK);
		int daysOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		switch (firstDay)
		{
		case Calendar.SUNDAY :
			return 26;
		case Calendar.MONDAY :
			return 25;
		case Calendar.TUESDAY :
			if (daysOfMonth == 31) return 31;
			return 24;
		case Calendar.WEDNESDAY :
			if (daysOfMonth >= 30) return 30;
			return 23;
		case Calendar.THURSDAY :
			if (daysOfMonth >= 29) return 29;
			return 22;
		case Calendar.FRIDAY :
			if (daysOfMonth >= 28) return 28;
			return 21;
		case Calendar.SATURDAY :
			return 27;
		}
		throw new RuntimeException("what day of the month?");
	}
	public Vector<String> getNext3Expiries(){
		Vector<String> expiries = new Vector<String>();
		
			Date date = new Date();
			LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			int year  = localDate.getYear();
			int month = localDate.getMonthValue();
			int day   = localDate.getDayOfMonth();
		
			 
			month--;
			
			int thurs = getLastThursday(month, year);
			
			if (day > thurs)
				month++;
			while(expiries.size() < 3){
				
				if(month > 11){
					month = 0;
					year++;
				}
				
				int thur1 = getLastThursday(month, year);
			
				Format formatter = new SimpleDateFormat("ddMMMyyyy");
			
		    	String s = formatter.format(new GregorianCalendar(year, month, thur1).getTime());
		    	
		    	expiries.add(s.toUpperCase());
		    	
		    	month++;
				
			}
		return expiries;
	}
	public static void main(String[] args) throws InterruptedException {
		
		DateUtil du = new DateUtil();
		Vector<String> nexp = du.getNext3Expiries();
		
		for(String s: nexp){
			System.out.println(s);
		}
	}
		
	
}
