import mdfs.namenode.repositories.GroupDataRepository;
import mdfs.namenode.repositories.GroupDataRepositoryNode;
import mdfs.namenode.repositories.UserDataRepository;
import mdfs.namenode.repositories.UserDataRepositoryNode;
import mdfs.utils.Config;

/**
 * Package: PACKAGE_NAME
 * Created: 2012-07-17
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class DefaultData {
    public static void load(){

        //Load root

        UserDataRepositoryNode root = UserDataRepository.getInstance().get(0);
        if(root == null){
            root = new UserDataRepositoryNode(0, "root");
            root.setPwdHash(Config.getString("roothash"));
            UserDataRepository.getInstance().add(root);
        }else{
            root.setPwdHash(Config.getString("roothash"));
            root.commit();
        }

        //Load sudo group
        GroupDataRepositoryNode sudo = GroupDataRepository.getInstance().get(1);
        if(sudo == null)
            sudo = GroupDataRepository.getInstance().add(1, "sudo");

        sudo.addUser(UserDataRepository.getInstance().get(0));


        //Test users
        UserDataRepository.getInstance().add("raz", "qwerty");
        UserDataRepository.getInstance().add("test1", "test1");
        UserDataRepository.getInstance().add("test2", "test2");

        GroupDataRepository.getInstance().get("test1").addUser(UserDataRepository.getInstance().get("raz"));


    }
}
