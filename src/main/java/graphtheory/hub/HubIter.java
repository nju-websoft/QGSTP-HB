package graphtheory.hub;


/**
 * Pay attention!!! Please call @hasNext before calling @next even if you are sure the return value of @hasNext
 * @author qkoqhh
 */
abstract public class HubIter {
    int p, cnt;
    int frame_id;
    int[] buffer;
    final Hub hub;

    HubIter(final Hub hub, final int u) {
        frame_id = hub.fcursor[u];
        p = hub.cursor[u];
        cnt = hub.size[u] * hub.type_length;
        this.hub = hub;
        buffer = hub.buffer.get(frame_id);
        next();
    }

    int getInt() {
        cnt--;
        return buffer[p++];
    }

    double getDouble() {
        cnt -= 2;
        final long x = buffer[p++];
        final long y = buffer[p++] & 0xffffffffL;
        return Double.longBitsToDouble(x << 32 | y);
    }


    public boolean hasNext() {
        return cnt >= 0;
    }

    abstract public void next();
}
