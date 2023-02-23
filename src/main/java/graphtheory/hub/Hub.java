package graphtheory.hub;

import java.util.ArrayList;

public class Hub {
    int type_length;

    int[] cursor;
    int[] fcursor;
    int[] size;

    // Array length from the scope of adding element
    static final int array_length = 100000000;
    // To keep a hub in the same array, we need some extra space. It depends on the largest hub(60000 now).
    // (If extra_space is too small, it will return a trace back. So don't worry we may ignore it.)
    static final int extra_space=200000;
    // Real Array length, add some space to keep the Hub in the same array.
    static final int true_array_length = array_length + extra_space;
    final int frame;
    // pointer, frame pointer
    int p;
    int[] fp;
    int frame_id;
    ArrayList<int[]> buffer = new ArrayList<>(1);

    /**
     * @param n    number of vetrex
     * @param k    number of hub
     * @param type a type sample of hub
     * @throws Exception neither int nor double
     */
    public Hub(int n, long k, Object[] type) throws Exception {
        int m = type.length;
        for (int i = 0; i < m; i++) {
            Class<?> c = type[i].getClass();
            if (c == Integer.class) {
                type_length++;
            } else if (c == Double.class) {
                type_length += 2;
            } else {
                throw new Exception("not implemented");
            }
        }
        cursor = new int[n];
        fcursor = new int[n];
        size = new int[n];
        final int num = array_length / type_length;
        frame = num * type_length;
        while (k > num) {
            buffer.add(new int[true_array_length]);
            k -= num;
        }
        buffer.add(new int[(int) k * type_length + extra_space]);
        fp = buffer.get(0);
        frame_id = 0;
    }

    public int cnt = -1;

    public void nextcursor() {
        check_frame();
        cursor[++cnt] = p;
        fcursor[cnt] = frame_id;
    }

    public void setCursor(int s) {
        check_frame();
        cnt = s;
        cursor[cnt] = p;
        fcursor[cnt] = frame_id;
    }

    public void addsize() {
        size[cnt]++;
    }

    void check_frame() {
        if (p >= frame) {
            p = 0;
            fp = buffer.get(++frame_id);
        }
    }

    public void addInt(int t) {
//        check_frame();
        fp[p++] = t;
    }

    public void addDouble(double t) {
//        check_frame();
        long bit = Double.doubleToLongBits(t);
        fp[p++] = (int) (bit >> 32);
        fp[p++] = (int) (bit);
    }
}
