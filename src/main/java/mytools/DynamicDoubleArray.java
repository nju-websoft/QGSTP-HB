package mytools;

import java.util.LinkedList;
import java.util.List;

import static graphtheory.Structure.inf;
import static java.lang.Math.min;


/**
 * Variant from DynamicArray but simplify some thing to speed up
 * Can only used in our algorithm
 * @author qkoqhh
 * @Date 2022-4-1
 */
public class DynamicDoubleArray {
    final double[] a;
    List<Integer> visitor = new LinkedList<>();
    final int n;


    /**
     * @param n the maximum length of the array
     */
    public DynamicDoubleArray(int n) {
        this.n = n;
        a = new double[n];
        for (int i = 0; i < n; i++) {
            a[i] = inf;
        }
    }

    public void clear(){
        for (Integer i : visitor) {
            a[i] = inf;
        }
        visitor = new LinkedList<>();
    }


    private void visit(int i) {
        if (a[i]==inf) {
            visitor.add(i);
        }
    }

    public double get(int i) {
        return a[i];
    }


    public void put(int i, double e) {
        visit(i);
        a[i] = min(a[i], e);
    }
}
