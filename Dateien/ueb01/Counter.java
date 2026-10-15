package Dateien.ueb01;

public class Counter {
    private int c = 0;

    public void increment() {
        int b = c;
        b = b + 1;
        c = b;
    }

    public void decrement() {
        int d = c;
        d = d - 1;
        c = d;
    }

    public int value() {
        return c;
    }
}
