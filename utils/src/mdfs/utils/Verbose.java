package mdfs.utils;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple class that in a Structured way of printing text
 * @author Rasmus Holm
 *
 */
public class Verbose {
	private static ReentrantLock lock = new ReentrantLock(true);
	
	/**
	 * Loglevel: 	1 - only message
	 * 				2 - message and the object the message comes from
	 * 				3 - message, the object the message comes from and what thread is holding the object
	 * @param msg that is to be printed
	 * @param location the object from where the printing occers
	 * @param logLevel the amount of information thar is printed 
	 */
	public static void print(String msg, Object location, int logLevel){
		lock.lock();
			try{
			switch (logLevel) {
			case 12:
			case 11:
			case 10:
			case 9:
			case 8:
			case 7:
			case 6:
			case 5:
			case 4:
			case 3:
				System.out.print(Thread.currentThread().getName() + "@");
			case 2:
				System.out.print(location.toString() + " ---> ");
			case 1: 
				System.out.println(msg);
				break;
			default:
			}
		}finally{
			lock.unlock();
		}
	}
}
