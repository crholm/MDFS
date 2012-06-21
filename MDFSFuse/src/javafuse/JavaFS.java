/*
 * JavaFuse - Java bindings for using FUSE.
 * Copyright (C) 2009 SRI International
 * 
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later 
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Java methods to be called by FUSE library. Look at FUSE API for proper
 * return values.
 */

package javafuse;

public interface JavaFS
{
    public int getattr_pre(String path, Stat buf);
    public int getattr_post(String path, Stat buf, int result);

    public int readlink_pre(String path, String buf, int bufsize);
    public int readlink_post(String path, String buf, int bufsize, int result);

    public int mknod_pre(String path, int mode, int dev);
    public int mknod_post(String path, int mode, int dev, int result);

    public int mkdir_pre(String path, int mode);
    public int mkdir_post(String path, int mode, int result);

    public int unlink_pre(String path);
    public int unlink_post(String path, int result);

    public int rmdir_pre(String path);
    public int rmdir_post(String path, int result);

    public int symlink_pre(String oldpath, String newpath);
    public int symlink_post(String oldpath, String newpath, int result);

    public int rename_pre(String oldpath, String newpath);
    public int rename_post(String oldpath, String newpath, int result);

    public int link_pre(String oldpath, String newpath);
    public int link_post(String oldpath, String newpath, int result);

    public int chmod_pre(String path, int mod);
    public int chmod_post(String path, int mod, int result);

    public int chown_pre(String path, int owner, int group);
    public int chown_post(String path, int owner, int group, int result);

    public int truncate_pre(String path, int length);
    public int truncate_post(String path, int length, int result);

    public int utime_pre(String filename, Utimbuf buf);
    public int utime_post(String filename, Utimbuf buf, int result);

    public int open_pre(String path, Fuse_file_info info);
    public int open_post(String path, Fuse_file_info info, int result);

    public int read_pre(String path, String buf, int size, int offset, Fuse_file_info info);
    public int read_post(String path, String buf, int size, int offset, Fuse_file_info info, int result);

    public int write_pre(String path, String buf, int count, int offset, Fuse_file_info info);
    public int write_post(String path, String buf, int count, int offset, Fuse_file_info info, int result);

    public int statfs_pre(String path, Statvfs buf);
    public int statfs_post(String path, Statvfs buf, int result);

    public int flush_pre(String path, Fuse_file_info info);
    public int flush_post(String path, Fuse_file_info info, int result);

    public int release_pre(String path, Fuse_file_info info);
    public int release_post(String path, Fuse_file_info info, int result);

    public int fsync_pre(String path, int datasync, Fuse_file_info info);
    public int fsync_post(String path, int datasync, Fuse_file_info info, int result);

    public int setxattr_pre(String path, String name, String value, int size, int flags);
    public int setxattr_post(String path, String name, String value, int size, int flags, int result);

    public int getxattr_pre(String path, String name, String value, int size);
    public int getxattr_post(String path, String name, String value, int size, int result);

    public int listxattr_pre(String path, String list, int size);
    public int listxattr_post(String path, String list, int size, int result);

    public int removexattr_pre(String path, String name);
    public int removexattr_post(String path, String name, int result);

    public int opendir_pre(String path, Fuse_file_info info);
    public int opendir_post(String path, Fuse_file_info info, int result);

    public int readdir_pre(String path, long buf, long filler, int offset, Fuse_file_info info);
    public int readdir_post(String path, long buf, long filler, int offset, Fuse_file_info info, int result);

    public int releasedir_pre(String path, Fuse_file_info info);
    public int releasedir_post(String path, Fuse_file_info info, int result);

    public int fsyncdir_pre(String path, int datasync, Fuse_file_info info);
    public int fsyncdir_post(String path, int datasync, Fuse_file_info info, int result);

    public void init(Fuse_conn_info conn);

    public void destroy(long arg);

    public int access_pre(String path, int mode);
    public int access_post(String path, int mode, int result);

    public int create_pre(String path, int mode, Fuse_file_info info);
    public int create_post(String path, int mode, Fuse_file_info info, int result);

    public int ftruncate_pre(String path, int length, Fuse_file_info info);
    public int ftruncate_post(String path, int length, Fuse_file_info info, int result);

    public int fgetattr_pre(String path, Stat st, Fuse_file_info info);
    public int fgetattr_post(String path, Stat st, Fuse_file_info info, int result);
    /*
    public int lock_pre(String path, Fuse_file_info info, int cmd, struct flock *lock);
    public int lock_post(String path, Fuse_file_info info, int cmd, struct flock *lock, int result);

    public int utimens_pre(String path, const struct timespec tv[2]);
    public int utimens_post(String path, const struct timespec tv[2], int result);

    public int bmap_pre(String path, int blocksize, uint64_t *idx);
    public int bmap_post(String path, int blocksize, uint64_t *idx, int result);
    */
}
