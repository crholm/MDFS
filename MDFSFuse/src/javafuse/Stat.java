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

package javafuse;

/* Equivalent to POSIX stat structure. */

public class Stat {
    public long st_dev;
    public long st_ino;
    public long st_mode;
    public long st_nlink;
    public long st_uid;
    public long st_gid;
    public long st_rdev;
    public long st_size;
    public long st_blksize;
    public long st_blocks;
    public long st_atime;
    public long st_mtime;
    public long st_ctime;

    public Stat() {}

    protected void finalize() {}
}
