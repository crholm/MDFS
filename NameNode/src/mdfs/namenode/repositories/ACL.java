package mdfs.namenode.repositories;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Package: mdfs.namenode.repositories
 * Created: 2012-07-12
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class ACL {
    private static ReentrantLock lock = new ReentrantLock();

    public static boolean isSU(UserDataRepositoryNode user){
        lock.lock();
        try{
            if(user == null)
                return false;

            if(GroupDataRepository.getInstance().get(1).containsUser(user))
                return true;
            return false;
        }finally {
            lock.unlock();
        }
    }

    public static boolean chmod(UserDataRepositoryNode user, MetaDataRepositoryNode file){
        lock.lock();
        try{
            if(file == null || user == null)
                return false;

            if(file.getUid() == user.getUid())
                return true;

            if(GroupDataRepository.getInstance().get(1).containsUser(user))
                return true;

            return false;
        }finally {
            lock.unlock();
        }
    }

    public static boolean chown(UserDataRepositoryNode user){
        lock.lock();
        try{
            return isSU(user);
        }finally {
            lock.unlock();
        }
    }

    public static boolean chgrp(UserDataRepositoryNode user, MetaDataRepositoryNode file, GroupDataRepositoryNode newGroup){
        lock.lock();
        try{
            if(file == null || user == null || newGroup == null)
                return false;

            if(GroupDataRepository.getInstance().get(1).containsUser(user))
                return true;

            if(user.getUid() == file.getUid() && newGroup.containsUser(user))
                return true;

            return false;
        }finally {
            lock.unlock();
        }
    }


    public static boolean rwxAllowed(ACLEnum operation, UserDataRepositoryNode user, MetaDataRepositoryNode file){
        lock.lock();
        try{
            boolean isOwner = false;
            boolean inGroup = false;

            if(file == null)
                return false;

            if(user != null)
                isOwner = user.getUid() == file.getUid() ? true : false;

            //Checks if user are Super user.
            if(GroupDataRepository.getInstance().get(1).containsUser(user)){
                isOwner = true;
                inGroup = true;
            }

            int permission = file.getPermission()%778;

            if(permission < 0)
                permission *= -1;

            int u = (permission/100) %8;
            int a = (permission %10) %8;
            int g = ((permission % 100 - a) / 10) %8 ;



            GroupDataRepositoryNode group = null;

            switch (operation){
                case READ:

                    if(isOwner && (u >> 2) == 1)
                         return true;

                    if((a >> 2) == 1)
                        return true;

                    group = GroupDataRepository.getInstance().get(file.getGid());
                    if(group == null)
                        break;

                    inGroup = inGroup ? true : group.containsUser(user);
                    if(inGroup && (g >> 2) == 1)
                        return true;

                    break;

                case WRITE:

                    if(isOwner && (u & 2) == 2)
                        return true;

                    if((a & 2) == 2)
                        return true;


                    group = GroupDataRepository.getInstance().get(file.getGid());
                    if(group == null)
                        break;

                    inGroup = inGroup ? true : group.containsUser(user);
                    if(inGroup && (g & 2) == 2)
                        return true;

                    break;

                case EXECUTE:
                    if(isOwner && (u & 1) == 1)
                        return true;

                    if((a & 1) == 1)
                        return true;

                    group = GroupDataRepository.getInstance().get(file.getGid());
                    if(group == null)
                        break;

                    inGroup = inGroup ? true : group.containsUser(user);
                    if(inGroup && (g & 1) == 1)
                        return true;

                    break;
                default:
            }
            return false;
        }finally {
            lock.unlock();
        }
    }
}
