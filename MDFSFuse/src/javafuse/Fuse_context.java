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

/* Equivalent to FUSE fuse_context structure. */

public class Fuse_context {
    //public fuse *fuse;
    public long uid;
    public long gid;
    public long pid;
    public long private_data;

    public Fuse_context() {
        System.out.println("CONTEXT CREATED!");
    }

    public void get() {
        System.out.println("GETTING!!");
    }

    protected void finalize() {}

    public native void copyContextIn();
}
