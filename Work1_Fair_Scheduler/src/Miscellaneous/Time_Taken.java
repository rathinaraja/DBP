package Miscellaneous;

import java.util.Date;

public class Time_Taken {
	static Date date;  
	public  int  getTime(){
		date = new Date();
		//System.out.println("enters timer");
	    long t1 = date.getTime();
	    int seconds=(int)(t1/1000)%60; 
	    int minutes=(int)(t1 / 1000 / 60) % 60; 
	   return minutes*60+seconds;  
	}
}
 