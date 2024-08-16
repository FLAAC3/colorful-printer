package org.flaac3.ColorfulPrinter;

import org.flaac3.ColorfulPrinter.Model.Color;
import org.flaac3.ColorfulPrinter.Model.TextColors;

import static org.flaac3.ColorfulPrinter.Model.TextColors.NO_COLOR;

@FunctionalInterface
public interface Template {
    TextColors getTextColor (int index); //通过 Lambda 自定义颜色模板

    Template DEFAULT_MESSAGE = index -> NO_COLOR;
    /**
     * 打印错误消息模板，第一次调用 appendByTemplate 是红色，其余均为默认颜色
     * */
    Template ERROR_MESSAGE = index -> {
        if (index == 0) {
            return new TextColors(Color.RED, null);
        } else return NO_COLOR;
    };
    /**
     * 打印没有问题消息模板，第一次调用 appendByTemplate 是绿色，其余均为默认颜色
     * */
    Template OK_MESSAGE = index -> {
        if (index == 0) {
            return new TextColors(Color.GREEN, null);
        } else return NO_COLOR;
    };
}
