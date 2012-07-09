package mdfs.client.tests;

import mdfs.client.api.FileQueryImpl;
import mdfs.utils.Config;

/**
 * Package: mdfs.client.tests
 * Created: 2012-07-09
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class Ping {

    static int pings = 1000;
    static int bytes = 10;

    public static void main(String args[]){




        Config.getInt("UseEncryption");
        System.out.println("-- Unencrypted --");
        Config.setProperty("UseEncryption", new Integer(0));

        long time = System.currentTimeMillis();
        for(int i = 0; i < pings; i++)
            if(!FileQueryImpl.ping(Config.getString("NameNode.address"), Config.getInt("NameNode.port"), bytes))
                System.out.println(i + ": FAILED");

        time = System.currentTimeMillis() - time;

        System.out.println("Pings: " + pings);
        System.out.println("Load: " + bytes + "b/ping");
        System.out.println("Time: " + time/pings + "ms/ping");
        System.out.println("Total Time: " + time + "ms\n");






        System.out.println("-- SSL --");
        Config.setProperty("UseEncryption", new Integer(1));

        time = System.currentTimeMillis();
        for(int i = 0; i < pings; i++)
            if(!FileQueryImpl.ping(Config.getString("NameNode.address"), Config.getInt("NameNode.port"), bytes))
                System.out.println(i + ": FAILED");

        time = System.currentTimeMillis() - time;

        System.out.println("Pings: " + pings);
        System.out.println("Load: " + bytes + "b/ping");
        System.out.println("Time: " + time/pings + "ms/ping");
        System.out.println("Total Time: " + time + "ms\n");









        System.out.println("-- Diffie Hellman --");
        Config.setProperty("UseEncryption", new Integer(2));

        time = System.currentTimeMillis();
        for(int i = 0; i < pings; i++)
            if(!FileQueryImpl.ping(Config.getString("NameNode.address"), Config.getInt("NameNode.port"), bytes))
                System.out.println(i + ": FAILED");

        time = System.currentTimeMillis() - time;

        System.out.println("Pings: " + pings);
        System.out.println("Load: " + bytes + "b/ping");
        System.out.println("Time: " + time/pings + "ms/ping");
        System.out.println("Total Time: " + time + "ms\n");





    }
}
