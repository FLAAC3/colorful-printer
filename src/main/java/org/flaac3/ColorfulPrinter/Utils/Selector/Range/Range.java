package org.flaac3.ColorfulPrinter.Utils.Selector.Range;

import java.util.function.Consumer;

public class Range {
    protected final int left; //左边界，用 final 修饰，这样就不用考虑浅拷贝的问题了
    /**
     * 这个类表示一个范围，但是只有一个数
     * */
    public Range (int left) { //只有一个数
        this.left = left;
    }
    /**
     * 获取元素数量
     * */
    public int size () {
        return 1;
    }
    /**
     * 区间最大值
     * */
    public int max () {
        return left;
    }
    /**
     * 区间最小值
     * */
    public int min () {
        return left;
    }
    /**
     * 左边界
     * */
    public int left () {
        return left;
    }
    /**
     * 右边界
     * */
    public int right () {
        return left;
    }
    /**
     * 是不是 left 小于等于 right
     * */
    public boolean isLMin() {
        return true;
    }
    /**
     * 遍历区间每一个元素，并对每个元素执行给定的操作，其中 isForward 设置是否正向遍历
     * */
    public void forEach (boolean isForward, Consumer<Integer> rangeWork) {
        rangeWork.accept(left);
    }
    public void forEach (Consumer<Integer> rangeWork) {
        forEach(true, rangeWork);
    }
}
