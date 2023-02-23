package graphtheory.hub;

public class HubHopIter extends HubIter{
    public HubHopIter(final Hub hub, final int u) {
        super(hub, u);
    }
    public int v,w;

    @Override
    public void next() {
        v=getInt();
        w=getInt();
    }
}
