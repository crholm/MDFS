package mdfs.utils.io.protocol;

/**
 * Package: mdfs.utils.io.protocol
 * Created: 2012-06-21
 *
 * @author Rasmus Holm
 * @version 1.0
 */
public class MDFSErrorCode {

    /**
     * No erros, successful */
    public static final int  NONE      = 0;
    /**
     *  Operation not permitted  */
    public static final int  EPERM     = 1;

    /**
     *  No such file or directory  */
    public static final int  ENOENT    = 2;

    /**
     *  No such process */
    public static final int  ESRCH     = 3;

    /**
     * Interrupted system call */
    public static final int  EINTR     = 4;

    /**
     * I/O error  */
    public static final int  EIO       = 5;

    /**
     *  No such device or address */
    public static final int  ENXIO     = 6;

    /**
     *  Argument list too long */
    public static final int  E2BIG     = 7;

    /**
     *  Exec format error */
    public static final int  ENOEXEC   = 8;

    /**
     *  Bad file number */
    public static final int  EBADF     = 9;

    /**
     *  No child processes */
    public static final int  ECHILD    = 10;

    /**
     * Try again */
    public static final int  EAGAIN    = 11;

    /**
     *  Out of memory             */
    public static final int  ENOMEM    = 12;

    /**
     *  Permission denied         */
    public static final int  EACCES    = 13;

    /**
     *  Bad address               */
    public static final int  EFAULT    = 14;

    /**
     *  Block device required */
    public static final int  ENOTBLK   = 15;

    /**
     *  Device or resource busy */
    public static final int  EBUSY     = 16;

    /**
     *  File exists */
    public static final int  EEXIST    = 17;

    /**
     *  Cross-device link */
    public static final int  EXDEV     = 18;

    /**
     *  No such device */
    public static final int  ENODEV    = 19;

    /**
     *  Not a directory           */
    public static final int  ENOTDIR   = 20;

    /**
     *  Is a directory */
    public static final int  EISDIR    = 21;

    /**
     *  Invalid argument */
    public static final int  EINVAL    = 22;

    /**
     *  File table overflow       */
    public static final int  ENFILE    = 23;

    /**
     *  Too many open files       */
    public static final int  EMFILE    = 24;

    /**
     *  Not a typewriter */
    public static final int  ENOTTY    = 25;

    /**
     *  Text file busy */
    public static final int  ETXTBSY   = 26;

    /**
     *  File too large */
    public static final int  EFBIG     = 27;

    /**
     *  No space left on device */
    public static final int  ENOSPC    = 28;

    /**
     *  Illegal seek */
    public static final int  ESPIPE    = 29;

    /**
     *  Read-only file system */
    public static final int  EROFS     = 30;

    /**
     *  Too many links */
    public static final int  EMLINK    = 31;

    /**
     *  Broken pipe */
    public static final int  EPIPE     = 32;

    /**
     *  Math argument out of domain of func */
    public static final int  EDOM      = 33;

    /**
     *  Math result not representable */
    public static final int  ERANGE    = 34;

    /**
     * Provides texte explainging error ( info[ErrorCode]), 35 Strings in array
     */
    public static final String[] info = {
                                            "No errors",
                                            "Operation not permitted",
                                            "No such file or directory",
                                            "No such process",
                                            "Interrupted system call",
                                            "I/O error",
                                            "No such device or address",
                                            "Argument list too long",
                                            "Exec format error",
                                            "Bad file number",
                                            "No child processes",
                                            "Try again",
                                            "Out of memory",
                                            "Permission denied",
                                            "Bad address",
                                            "Block device required",
                                            "Device or resource busy",
                                            "File exists",
                                            "Cross-device link",
                                            "No such device",
                                            "Not a directory",
                                            "Is a directory",
                                            "Invalid argument",
                                            "File table overflow",
                                            "Too many open files",
                                            "Not a typewriter",
                                            "Text file busy",
                                            "File too large",
                                            "No space left on device",
                                            "Illegal seek",
                                            "Read-only file system",
                                            "Too many links",
                                            "Broken pipe",
                                            "Math argument out of domain of func",
                                            "Math result not representable"
                                        };

}
