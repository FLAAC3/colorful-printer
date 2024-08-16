package org.flaac3.ColorfulPrinter.Utils.Selector.Range;

import java.util.function.Consumer;

public class LMaxRangeTwo extends Range {
    private final int right; //右边界，用 final 修饰，这样就不用考虑浅拷贝的问题了
    /**
     * 这个类表示一个范围，包括左边界和右边界，并且左边界一定大于右边界
     * */
    public LMaxRangeTwo (int left, int right) { //一个左闭右闭区间
        super(left);
        if (left <= right) throw new IllegalArgumentException("LMaxRangeTwo 左边界必须大于右边界");
        this.right = right;
    }
    /**
     * 获取元素数量
     * */
    @Override
    public int size () {
        return left - right + 1;
    }
    /**
     * 区间最小值
     * */
    @Override
    public int min () {
        return right;
    }
    /**
     * 右边界
     * */
    @Override
    public int right () {
        return right;
    }
    /**
     * 是不是 left 小于等于 right
     * */
    @Override
    public boolean isLMin() {
        return false;
    }
    /**
     * 遍历区间每一个元素，并对每个元素执行给定的操作，其中 isForward 设置是否正向遍历
     * */
    @Override
    public void forEach (boolean isForward, Consumer<Integer> rangeWork) {
        if (isForward) for (int i = left; i >= right; i--) rangeWork.accept(i);
        else for (int i = right; i <= left; i++) rangeWork.accept(i);
    }
}
