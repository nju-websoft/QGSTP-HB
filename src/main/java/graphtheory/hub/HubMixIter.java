package graphtheory.hub;

public class HubMixIter extends HubIter{
    public HubMixIter(final Hub hub, final int u) {
        super(hub, u);
    }

    public int v;
    public double w;
    @Override
    public void next() {
        v=getInt();
        w=getDouble();
    }
}
