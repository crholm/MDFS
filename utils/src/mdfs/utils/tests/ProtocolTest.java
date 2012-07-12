package mdfs.utils.tests;

import mdfs.utils.io.protocol.MDFSProtocolHeader;
import mdfs.utils.io.protocol.MDFSProtocolMetaData;
import mdfs.utils.io.protocol.enums.MetadataType;
import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;
import mdfs.utils.io.protocol.enums.Type;
import org.json.JSONException;

/**
 * Package: mdfs.utils.tests
 * Created: 2012-06-18
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class ProtocolTest {
    public static void main(String args[]){
        System.out.println(Stage.REQUEST);
        System.out.println(Stage.RESPONSE);
        System.out.println(Stage.INFO);

        try{
            Mode mode = Mode.valueOf("EDIT".trim());
            System.out.println(mode);
        }catch (Exception e){
            System.out.println("failed");
        }


        MDFSProtocolMetaData metaData = new MDFSProtocolMetaData();
        MDFSProtocolHeader header = new MDFSProtocolHeader();

        metaData.setPath("/test/esa");
        metaData.setType(MetadataType.DIR);
        metaData.setSize(2345);
        metaData.setPermission(764);
        metaData.setOwner("raz");
        metaData.setGroup("raz");
        metaData.setCreated(125161);
        metaData.setLastEdited(1234135);
        metaData.setLastTouched(12352);


        System.out.println(metaData);

        header.setFrom("tea");
        header.setTo("sdf");
        header.setStage(Stage.REQUEST);
        header.setType(Type.FILE);
        header.setMode(Mode.EDIT);

        header.setMetadata(metaData);

        String hs = header.toString();
           System.out.println(header);
        try {
            MDFSProtocolHeader h1 = new MDFSProtocolHeader(hs);
            MDFSProtocolHeader h2 = new MDFSProtocolHeader(h1.toString());
            MDFSProtocolHeader h3 = new MDFSProtocolHeader(h2.toString());
            System.out.println(h1);
            System.out.println(h2);
            System.out.println(h3);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}


