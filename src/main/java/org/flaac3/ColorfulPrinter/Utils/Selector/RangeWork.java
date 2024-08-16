package org.flaac3.ColorfulPrinter.Utils.Selector;

/**
 * 这个接口用来定义 Selector 根据范围得出具体数字后要干什么
 * */
public interface RangeWork {
    /**
     * 如果当前是一个范围，则会调用一次这个方法，传入开始值和结束值（范围）<br/>
     * 注意针对正向和反向遍历，start 和 end 的数字会交换
     * */
    void ifRange (int start, int end);
    /**
     * 如果当前是单个数字，则会调用一次这个方法，传入数字的值
     * */
    void ifOne (int num);
}
