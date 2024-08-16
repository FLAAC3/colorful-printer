package org.flaac3.ColorfulPrinter.Utils;

/**
 * 一个用于处理两个数字的小工具，顺便集成了找出最大值和最小值的功能
 * */
public class IntPair {
    private Integer a = null, b = null;
    public IntPair() {}
    public IntPair(int a, int b) {
        this.a = a;
        this.b = b;
    }
    @FunctionalInterface
    public interface IntPair_ {
        void work (int a, int b);
    }
    public void let (IntPair_ twoRun) {
        twoRun.work(a, b);
    }
    public int max () {
        if (a == null) return b;
        if (b == null) return a;
        return Math.max(a, b);
    }
    public int min () {
        if (a == null) return b;
        if (b == null) return a;
        return Math.min(a, b);
    }
    public IntPair set (int a, int b) {
        this.a = a;
        this.b = b;
        return this;
    }
    public void setA (int a) {
        this.a = a;
    }
    public void setB (int b) {
        this.b = b;
    }
    public void setAMin (int c) {
        if (a == null) setA(c);
        else setA(Math.min(a, c));
    }
    public void setAMin (int... c) {
        for (int i : c) setAMin(i);
    }
    public void setBMax (int c) {
        if (b == null) setB(c);
        else setB(Math.max(b, c));
    }
    public void setBMax (int... c) {
        for (int i : c) setBMax(i);
    }
    public void setAMinBMax (int c) {
        setAMin(c); setBMax(c);
    }
    public void setAMinBMax (int... c) {
        for (int i : c) setAMinBMax(i);
    }
    public int getA () {
        return a;
    }
    public int getB () {
        return b;
    }
}
