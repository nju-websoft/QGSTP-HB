package mytools;

import java.util.LinkedList;
import java.util.List;


/**
 * In some scenarios, we write some values into some array where the number of values is far less than the actually space.
 * The array maintain a written tag @v and support clearing the array according to the tag instead of processing on the whole array.
 */
abstract public class DynamicArray<E> {
    final Object[] a;
    List<Integer> visitor = new LinkedList<>();
    final boolean[] v;
    final int n;

    protected abstract E initial();

    /**
     * @param n the maximum length of the array
     */
    protected DynamicArray(int n) {
        this.n = n;
        a = new Object[n];
        v = new boolean[n];
        for (int i = 0; i < n; i++) {
            a[i] = initial();
        }
    }

    public void clear(){
        if (a[0] instanceof DynamicArray) {
            for (Integer i : visitor) {
                a[i] = initial();
                v[i] = false;
            }
        }else{
            for (Integer i : visitor){
                ((DynamicArray)a[i]).clear();
                v[i] = false;
            }
        }
        visitor = new LinkedList<>();
    }


    private void visit(int i) {
        if (!v[i]) {
            v[i] = true;
            visitor.add(i);
        }
    }

    public E get(int i) {
        return (E) a[i];
    }

    public E mod(int i){
        visit(i);
        return get(i);
    }

    public void put(int i, E e){
        visit(i);
        a[i] = e;
    }
}
