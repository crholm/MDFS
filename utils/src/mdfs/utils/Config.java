package mdfs.utils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * A class that statically enables access and query the config file mdfs/config/config.cfg for data
 * @author Rasmus Holm
 *
 */
public class Config {
	private static Configuration config = null;
	
	private static void init() throws ConfigurationException{
		config = new PropertiesConfiguration("mdfs/config/config.cfg");
	}
	
	/**
	 * 
	 * @param arg The config part that is requsted
	 * @return the value of the argument as a string
	 */
	public static String getString(String arg){
		if (config == null){
			try {
				init();
			} catch (ConfigurationException e) {
				//return null;
				e.printStackTrace();
			}
		}		
		return config.getString(arg);
	}
	
	/**
	 * 
	 * @param arg The config part that is requsted
	 * @return a String array that holds all the values of the argument
	 */
	public static String[] getStringArray(String arg){
		if (config == null){
			try {
				init();
			} catch (ConfigurationException e) {
				return null;
				//e.printStackTrace();
			}
		}
		
		return config.getStringArray(arg);
	}
	
	/**
	 * 
	 * @param arg The config part that is requsted
	 * @return value of an argument as an int
	 */
	public static int getInt(String arg){
		if (config == null){
			try {
				init();
			} catch (ConfigurationException e) {
				//return null;
				e.printStackTrace();
			}
		}
		
		return config.getInt(arg);
	}
	
}
