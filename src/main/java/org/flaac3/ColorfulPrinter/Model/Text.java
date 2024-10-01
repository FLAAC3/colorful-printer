package org.flaac3.ColorfulPrinter.Model;

import org.flaac3.ColorfulPrinter.Template;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Text {
    public Color textColor;
    public Color backGroundColor;
    public String textStr;
    public int templateIndex = -1; //未通过模板初始化是 -1，否则从 0 开始递增编号
    private boolean needReset = false; //是否需要先重置颜色，再继续打印

    /**
     * 直接设置文本颜色
     * */
    public Text (Color textColor, Color backGroundColor, Text previousText, Object... text) {
        setInfo(
                checkTextColor(textColor, previousText),
                checkBackGroundColor(backGroundColor, previousText),
                objArrToString(text)
        );
        setNeedReset(previousText);
    }

    /**
     * 通过模板来设置文本颜色
     * */
    public Text (int index, Template template, Text previousText, Object... text) {
        var textColors = template.getTextColor(index);
        setInfo(
                checkTextColor(textColors.textColor(), previousText),
                checkBackGroundColor(textColors.backGroundColor(), previousText),
                objArrToString(text)
        );
        this.templateIndex = index;
        setNeedReset(previousText);
    }

    /**
     * （构造方法用）设置文本、文本颜色、背景颜色三个基础信息
     * */
    private void setInfo (Color textColor, Color backGroundColor, String text) {
        this.textColor = textColor;
        this.backGroundColor = backGroundColor;
        this.textStr = text;
    }

    /**
     * 对于 Color.DEFAULT 的处理
     * */
    private static Color checkTextColor (Color textColor, Text previousText) {
        if (Color.DEFAULT.equals(textColor)) {
            if (previousText == null) return null;
            else return previousText.textColor;
        } else return textColor;
    }
    private static Color checkBackGroundColor (Color backGroundColor, Text previousText) {
        if (Color.DEFAULT.equals(backGroundColor)) {
            if (previousText == null) return null;
            else return previousText.backGroundColor;
        } else return backGroundColor;
    }

    /**
     * 根据当前 Text 和上一个 Text 的差别判断出是否需要重置颜色
     * */
    public Text setNeedReset (Text previousText) {
        needReset = previousText != null
                && (textColor == null && previousText.textColor != null
                        || backGroundColor == null && previousText.backGroundColor != null
                );
        return this;
    }

    /**
     * 通过模板来修改 Text 的文本颜色和背景色
     * */
    public void setColorByTemplate (Template template, Text previousText) {
        var textColors = template.getTextColor(templateIndex); //传递当前 Text 的编号
        textColor = checkTextColor(textColors.textColor(), previousText);
        backGroundColor = checkBackGroundColor(textColors.backGroundColor(), previousText);
        setNeedReset(previousText);
    }

    /**
     * 带颜色转义的字符串
     * */
    public String toColorString() {
        var s = new StringBuilder();
        if (needReset) s.append(TextColors.ResetStr);
        if (backGroundColor != null) s.append(TextColors.getBackGroundColorStr(backGroundColor));
        if (textColor != null) s.append(TextColors.getTextColorStr(textColor));
        if (textStr != null) s.append(textStr); else s.append("null");
        return s.toString();
    }

    @Override
    public String toString() {
        return textStr;
    }

    /**
     * 把任意对象数组都转换并且合并成 String
     * */
    public static String objArrToString (Object... objects) {
        return Arrays.stream(objects).map(Object::toString).collect(Collectors.joining());
    }
}