package mdfs.utils.tests;

import mdfs.utils.io.protocol.enums.Mode;
import mdfs.utils.io.protocol.enums.Stage;

/**
 * Package: mdfs.utils.tests
 * Created: 2012-06-18
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class EnumTest {
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


    }
}


