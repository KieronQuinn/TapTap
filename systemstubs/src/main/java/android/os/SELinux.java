package android.os;

public class SELinux {

    /**
     * Restores a file to its default SELinux security context.
     * If the system is not compiled with SELinux, then {@code true}
     * is automatically returned.
     * If SELinux is compiled in, but disabled, then {@code true} is
     * returned.
     *
     * @param pathname The pathname of the file to be relabeled.
     * @return a boolean indicating whether the relabeling succeeded.
     * @exception NullPointerException if the pathname is a null object.
     */
    public static boolean restorecon(String pathname) {
        throw new RuntimeException("Stub!");
    }

}
