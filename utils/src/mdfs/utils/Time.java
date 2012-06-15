package mdfs.utils;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple way of getting timestamps in the form of yyyy-MM-dd HH:mm:ss
 * @author Rasmus Holm
 *
 */
public class Time{
	/**
	 * 
	 * @return the current system timestamp 
	 */
	public static String getTimeStamp(){
		return getTimeStamp(System.currentTimeMillis());
	}
	/**
	 * 
	 * @param time the long that represents the time
	 * @return a time stamp derived from the long time
	 */
	public static String getTimeStamp(long time){
		return getTimeStamp(new Date(time));
	}
	/**
	 * 
	 * @param date the date to be time stampt
	 * @return a time stamp using the date as reference
	 */
	public static String getTimeStamp(Date date){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(date);
	}

}
