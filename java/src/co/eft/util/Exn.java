package co.eft.util;

public class Exn {

    public static RuntimeException  wrap(Exception exn) {
        if (exn instanceof RuntimeException)
            return (RuntimeException) exn;
        else
            return new RuntimeException(exn);
    }
}
