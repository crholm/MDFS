package mdfs.namenode.repositories;

/**
 * Package: mdfs.namenode.repositories
 * Created: 2012-07-12
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class ACL {
    public boolean operationAllowed(ACLEnum operation, UserDataRepositoryNode user, MetaDataRepositoryNode file){
        boolean isOwner = false;
        boolean inGroup = false;

        if(file == null)
            return false;

        if(user != null)
            isOwner = user.getUid() == file.getUid() ? true : false;


        int permission = file.getPermission()%778;

        if(permission < 0)
            permission *= -1;

        int u = (permission/100) %8;
        int a = (permission %10) %8;
        int g = ((permission % 100 - a) / 10) %8 ;



        switch (operation){
            case READ:
                if(isOwner && (u >> 2) == 1)
                     return true;

                if((a >> 2) == 1)
                    return true;

                if(user != null)
                    break;


                GroupDataRepositoryNode group = GroupDataRepository.getInstance().get(file.getGid());
                if(group == null)
                    break;

                inGroup = group.containsUser(user);
                if(inGroup && (g >> 2) == 1)
                    return true;

                break;

            case WRITE:

                if(isOwner && (u & 2) == 2)
                    return true;

                if((a & 2) == 2)
                    return true;

                if(user != null)
                    break;

                inGroup = GroupDataRepository.getInstance().get(file.getGid()).containsUser(user);
                if(inGroup && (g & 2) == 2)
                    return true;

                break;

            case EXECUTE:
                if(isOwner && (u & 1) == 1)
                    return true;

                if((a & 1) == 1)
                    return true;

                if(user != null)
                    break;

                inGroup = GroupDataRepository.getInstance().get(file.getGid()).containsUser(user);
                if(inGroup && (g & 1) == 1)
                    return true;

                break;
            default:
        }
        return false;
    }
}
