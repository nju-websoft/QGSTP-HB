package graphtheory.hub;

public class HubConIter extends HubIter{
    public HubConIter(Hub hub, int u) {
        super(hub, u);
    }

    public int v,hop,pre;
    public double a,b;
    @Override
    public void next() {
        v=getInt();
        a=getDouble();
        b=getDouble();
        hop=getInt();
        pre=getInt();
    }
}
