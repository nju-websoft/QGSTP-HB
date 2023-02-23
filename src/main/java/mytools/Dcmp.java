package mytools;

public class Dcmp {
    static final double eps = 1e-8;

    public static boolean lt(final double a, final double b) {
        return a + eps < b;
    }

    public static boolean le(final double a, final double b) {
        return a < b + eps;
    }

    public static boolean gt(final double a, final double b) {
        return a > eps + b;
    }

    public static boolean ge(final double a, final double b) {
        return a + eps > b;
    }

    public static boolean eq(final double a, final double b) {
        return a < b + eps && b < a + eps;
    }
}
