package mdfs.fuse;

import javafuse.*;

/**
 * Package: mdfs.client.api
 * Created: 2012-06-20
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class MDFSFuse implements JavaFS{
    @Override
    public int getattr_pre(String path, Stat buf) {
        return 0;  
    }

    @Override
    public int getattr_post(String path, Stat buf, int result) {
        return 0;  
    }

    @Override
    public int readlink_pre(String path, String buf, int bufsize) {
        return 0;  
    }

    @Override
    public int readlink_post(String path, String buf, int bufsize, int result) {
        return 0;  
    }

    @Override
    public int mknod_pre(String path, int mode, int dev) {
        return 0;  
    }

    @Override
    public int mknod_post(String path, int mode, int dev, int result) {
        return 0;  
    }

    @Override
    public int mkdir_pre(String path, int mode) {
        return 0;  
    }

    @Override
    public int mkdir_post(String path, int mode, int result) {
        return 0;  
    }

    @Override
    public int unlink_pre(String path) {
        return 0;  
    }

    @Override
    public int unlink_post(String path, int result) {
        return 0;  
    }

    @Override
    public int rmdir_pre(String path) {
        return 0;  
    }

    @Override
    public int rmdir_post(String path, int result) {
        return 0;  
    }

    @Override
    public int symlink_pre(String oldpath, String newpath) {
        return 0;  
    }

    @Override
    public int symlink_post(String oldpath, String newpath, int result) {
        return 0;  
    }

    @Override
    public int rename_pre(String oldpath, String newpath) {
        return 0;  
    }

    @Override
    public int rename_post(String oldpath, String newpath, int result) {
        return 0;  
    }

    @Override
    public int link_pre(String oldpath, String newpath) {
        return 0;  
    }

    @Override
    public int link_post(String oldpath, String newpath, int result) {
        return 0;  
    }

    @Override
    public int chmod_pre(String path, int mod) {
        return 0;  
    }

    @Override
    public int chmod_post(String path, int mod, int result) {
        return 0;  
    }

    @Override
    public int chown_pre(String path, int owner, int group) {
        return 0;  
    }

    @Override
    public int chown_post(String path, int owner, int group, int result) {
        return 0;  
    }

    @Override
    public int truncate_pre(String path, int length) {
        return 0;  
    }

    @Override
    public int truncate_post(String path, int length, int result) {
        return 0;  
    }

    @Override
    public int utime_pre(String filename, Utimbuf buf) {
        return 0;  
    }

    @Override
    public int utime_post(String filename, Utimbuf buf, int result) {
        return 0;  
    }

    @Override
    public int open_pre(String path, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int open_post(String path, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public int read_pre(String path, String buf, int size, int offset, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int read_post(String path, String buf, int size, int offset, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public int write_pre(String path, String buf, int count, int offset, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int write_post(String path, String buf, int count, int offset, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public int statfs_pre(String path, Statvfs buf) {
        return 0;  
    }

    @Override
    public int statfs_post(String path, Statvfs buf, int result) {
        return 0;  
    }

    @Override
    public int flush_pre(String path, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int flush_post(String path, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public int release_pre(String path, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int release_post(String path, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public int fsync_pre(String path, int datasync, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int fsync_post(String path, int datasync, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public int setxattr_pre(String path, String name, String value, int size, int flags) {
        return 0;  
    }

    @Override
    public int setxattr_post(String path, String name, String value, int size, int flags, int result) {
        return 0;  
    }

    @Override
    public int getxattr_pre(String path, String name, String value, int size) {
        return 0;  
    }

    @Override
    public int getxattr_post(String path, String name, String value, int size, int result) {
        return 0;  
    }

    @Override
    public int listxattr_pre(String path, String list, int size) {
        return 0;  
    }

    @Override
    public int listxattr_post(String path, String list, int size, int result) {
        return 0;  
    }

    @Override
    public int removexattr_pre(String path, String name) {
        return 0;  
    }

    @Override
    public int removexattr_post(String path, String name, int result) {
        return 0;  
    }

    @Override
    public int opendir_pre(String path, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int opendir_post(String path, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public int readdir_pre(String path, long buf, long filler, int offset, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int readdir_post(String path, long buf, long filler, int offset, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public int releasedir_pre(String path, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int releasedir_post(String path, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public int fsyncdir_pre(String path, int datasync, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int fsyncdir_post(String path, int datasync, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public void init(Fuse_conn_info conn) {
        
    }

    @Override
    public void destroy(long arg) {
        
    }

    @Override
    public int access_pre(String path, int mode) {
        return 0;  
    }

    @Override
    public int access_post(String path, int mode, int result) {
        return 0;  
    }

    @Override
    public int create_pre(String path, int mode, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int create_post(String path, int mode, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public int ftruncate_pre(String path, int length, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int ftruncate_post(String path, int length, Fuse_file_info info, int result) {
        return 0;  
    }

    @Override
    public int fgetattr_pre(String path, Stat st, Fuse_file_info info) {
        return 0;  
    }

    @Override
    public int fgetattr_post(String path, Stat st, Fuse_file_info info, int result) {
        return 0;  
    }
}
