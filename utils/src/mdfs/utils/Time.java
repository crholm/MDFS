package mdfs.utils;
import mdfs.utils.io.SocketFactory;
import mdfs.utils.io.SocketFunctions;
import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;
import org.json.JSONException;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Simple way of getting timestamps in the form of yyyy-MM-dd HH:mm:ss
 * @author Rasmus Holm
 *
 */
public class Time{

    private static long offset = 0;

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

    public static long getOffset() {
        return offset;
    }

    public static void setOffset(long offset) {
        Time.offset = offset;
    }

    public static long currentTimeMillis(){
        return  System.currentTimeMillis()+getOffset();
    }

    public static void syncTime(String host, int port){

        SocketFactory socketFactory = new SocketFactory();
        SocketFunctions socketFunctions = new SocketFunctions();

        MDFSProtocolHeader request = new MDFSProtocolHeader();
        request.setStage(Stage.REQUEST);
        request.setType(Type.INFO);
        request.setMode(Mode.PING);

        setOffset(0);

        for(int i = 0; i < 10; i++){
            Socket socket = socketFactory.createSocket(host, port);
            MDFSProtocolHeader response;

            long comTime1 = System.currentTimeMillis();
            socketFunctions.sendText(socket, request.toString());
            comTime1 = System.currentTimeMillis() - comTime1;

            try {

                long comTime2 = System.currentTimeMillis();
                String txt = socketFunctions.receiveText(socket);
                comTime2 = System.currentTimeMillis() - comTime2;


                long dataNodeTime = System.currentTimeMillis();

                response = new MDFSProtocolHeader(txt);

                long nameNodeTime = response.getInfo().getLocalTime();


                if(i == 0)
                    setOffset(nameNodeTime- dataNodeTime + comTime1 + comTime2/2);
                else
                    setOffset((nameNodeTime- dataNodeTime + comTime1 + comTime2/2 + Time.getOffset())/2);



            } catch (JSONException e) {

            }

        }
    }

}
