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

/* Equivalent to FUSE fuse_file_info structure. */

public class Fuse_file_info {
    public long flags;
    public long fh_old;
    public long writepage;
    public long direct_io;
    public long keep_cache;
    public long flush;
    public long padding;
    public long fh;
    public long lock_owner;

    public Fuse_file_info() {
        direct_io = 1;
        keep_cache = 1;
        flush = 1;
        padding = 29;
    }

    protected void finalize() {}
}
