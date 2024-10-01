package org.flaac3.ColorfulPrinter;

import org.flaac3.ColorfulPrinter.Model.Color;
import org.flaac3.ColorfulPrinter.Model.TextColors;
import org.flaac3.ColorfulPrinter.Model.Text;
import org.flaac3.ColorfulPrinter.Utils.Selector.Range.Range;
import org.flaac3.ColorfulPrinter.Utils.Selector.RangeWork;
import org.flaac3.ColorfulPrinter.Utils.Selector.SortSelector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Printer {
    private final ArrayList<Text> textResource = new ArrayList<>();
    private Template template = Template.DEFAULT_MESSAGE; //默认颜色模板
    private int index = 0; //ArrayList 中通过模板初始化的 Text 数量

    /**
     * 只选择当前的 Text
     */
    public SortSelector ThisOne () {
        return new SortSelector(new int[][]{{textResource.size() - 1}});
    }
    /**
     * 选择当前全部的 Text
     * */
    public SortSelector All () {
        return new SortSelector(new int[][]{{0, textResource.size() - 1}});
    }

    /**
     * 一系列构造方法
     * */
    public Printer () {}
    public Printer (Template template) {
        setTemplate(template);
    }
    public Printer (Color textColor, Color backGroundColor, Object... text) {
        append(textColor, backGroundColor, text);
    }
    public Printer (Color textColor, Object... text) {
        append(textColor, text);
    }
    public Printer (Object... text) {
        append(text);
    }

    /**
     * 一系列打印方法
     * */
    public Printer print (boolean resetAtEnd) {
        System.out.print(toColorString(resetAtEnd));
        return this;
    }
    public Printer print () {
        return print(true);
    }
    public Printer println (boolean resetAtEnd) {
        print(resetAtEnd); System.out.println();
        return this;
    }
    public Printer println () {
        return println(true);
    }

    /**
     * 通过自定义颜色模板来打印的示例方法，顺便可以调用此方法取色
     * */
    public static Printer printlnColorTable () {
        //通过 Lambda 自定义颜色模板，根据 index 创建新的背景颜色 Color 对象，而文本颜色恒为绿色
        var printer = new Printer( index ->
            new TextColors(Color.GREEN, new Color(index))
        );
        for (int i = 0; i < 256; i++) {
            if (i < 16) {
                if (i < 2 || 3 < i && i < 7 || 8 < i && i < 11 || 12 < i) {
                    printer.appendByTemplate("    ", i, "    ");
                } else {
                    printer.appendByTemplate("    ", i, "   ");
                }
                if (i == 15) printer.append(TextColors.NextLineStr);
            } else if (i < 88) {
                printer.appendByTemplate(" ", i, " ");
                if (i == 51 || i == 87) printer.append(TextColors.NextLineStr);
            } else if (i < 124) {
                if (i < 100) {
                    printer.appendByTemplate(" ", i, " ");
                } else {
                    printer.appendByTemplate(" ", i);
                }
                if (i == 123) printer.append(TextColors.NextLineStr);
            } else if (i < 232) {
                printer.appendByTemplate(" ", i);
                if (i == 159 || i == 195 || i == 231) printer.append(TextColors.NextLineStr);
            } else {
                printer.appendByTemplate("  ", i, " ");
            }
        }
        return printer.println();
    }

    /**
     * 设置当前对象要使用的颜色模板
     * */
    public Printer setTemplate (Template template) {
        this.template = template;
        return this;
    }

    /**
     * 一系列追加字符串方法
     * */
    public Printer append (Color textColor, Color backGroundColor, Object... text) {
        textResource.add(new Text(textColor, backGroundColor, getLastText(), text));
        return this;
    }
    public Printer append (Color textColor, Object... text) {
        return append(textColor, null, text);
    }
    public Printer append (Object... text) {
        return append(null, null, text);
    }
    public Printer appendByTemplate (Object... text) {
        textResource.add(new Text(index, template, getLastText(), text));
        index ++;
        return this;
    }
    public Printer appendAtPre (Object... text) {
        var lastText = getLastText();
        if (lastText == null) return append(text);
        lastText.textStr += Text.objArrToString(text);
        return this;
    }
    public Printer appendAtPreIfSameColor (Color textColor, Color backGroundColor, Object... text) {
        var lastText = getLastText();
        if (lastText != null) {
            if ((Objects.equals(lastText.textColor, textColor) || Color.DEFAULT.equals(textColor))
                    && (Objects.equals(lastText.backGroundColor, backGroundColor) || Color.DEFAULT.equals(backGroundColor))
            ) return appendAtPre(text); else return append(textColor, backGroundColor, text);
        } else return append(textColor, backGroundColor, text);
    }

    public class TextEditor {
        private final SortSelector sortSelector; //textResource 中选择的下标集合
        private int insertSize = 1; //目前插入到 textResource 中的 Text 数量

        public TextEditor (SortSelector sortSelector) {
            this.sortSelector = sortSelector;
        }

        /**
         * 每次更改一个或多个连续的 Text 的颜色，重新设置紧挨其后的 Text（如果有）是否需要重置颜色
         * */
        private void checkNeedResetAtNext (int index) {
            if (index >= textResource.size() - 1) return;
            textResource.get(index + 1).setNeedReset(textResource.get(index));
        }

        /**
         * 每次删除一个或多个连续的 Text，重新设置其两边的 Text（如果有）是否需要重置颜色
         * */
        private void checkNeedReset (int from, int end) {
            if (end >= textResource.size() - 1) return;
            textResource.get(end + 1).setNeedReset(from > 0 ? textResource.get(from - 1) : null);
        }

        /**
         * 默认的设置是否需要重置颜色的方法
         * */
        private void checkWithRange () {
            sortSelector.forEachRange(new RangeWork() {
                @Override
                public void ifRange (int start, int end) { checkNeedResetAtNext(end); }
                @Override
                public void ifOne (int num) { checkNeedResetAtNext(num); }
            });
        }

        /**
         * 读取所有 Text 的文本颜色
         * */
        public ArrayList<Color> getTextColor () {
            var list = new ArrayList<Color>();
            sortSelector.forEach(i ->
                    list.add(textResource.get(i).textColor)
            );
            return list;
        }

        /**
         * 读取所有 Text 的背景颜色
         * */
        public ArrayList<Color> getBackGroundColor () {
            var list = new ArrayList<Color>();
            sortSelector.forEach(i ->
                    list.add(textResource.get(i).backGroundColor)
            );
            return list;
        }

        /**
         * 设置所有 Text 的文本颜色和背景颜色
         * */
        public void setColors (Color textColor, Color backGroundColor) {
            if (Color.DEFAULT.equals(textColor)) {
                if (!Color.DEFAULT.equals(backGroundColor)) {
                    sortSelector.forEach(i -> {
                        var text = textResource.get(i);
                        text.backGroundColor = backGroundColor;
                        text.setNeedReset(i > 0 ? textResource.get(i - 1) : null);
                    });
                    checkWithRange();
                }
            } else {
                if (Color.DEFAULT.equals(backGroundColor)) {
                    sortSelector.forEach(i -> {
                        var text = textResource.get(i);
                        text.textColor = textColor;
                        text.setNeedReset(i > 0 ? textResource.get(i - 1) : null);
                    });
                    checkWithRange();
                } else {
                    sortSelector.forEachRange(new RangeWork() {
                        @Override
                        public void ifRange (int start, int end) {
                            var first = textResource.get(start);

                            first.textColor = textColor; first.backGroundColor = backGroundColor;
                            first.setNeedReset(start > 0 ? textResource.get(start - 1) : null);

                            for (int i = start + 1; i <= end; i++) {
                                var text = textResource.get(i);
                                text.textColor = textColor; text.backGroundColor = backGroundColor;
                                text.setNeedReset(null);
                            }
                            checkNeedResetAtNext(end);
                        }
                        @Override
                        public void ifOne (int num) {
                            var text = textResource.get(num);
                            text.textColor = textColor; text.backGroundColor = backGroundColor;
                            text.setNeedReset(num > 0 ? textResource.get(num - 1) : null);
                            checkNeedResetAtNext(num);
                        }
                    });
                }
            }
        }

        /**
         * 读取和设置文本内容系列方法
         * */
        public ArrayList<String> getText () {
            var list = new ArrayList<String>();
            sortSelector.forEach(i ->
                    list.add(textResource.get(i).textStr)
            );
            return list;
        }
        public void setText (Object... newText) {
            sortSelector.forEach(i ->
                    textResource.get(i).textStr = Text.objArrToString(newText)
            );
        }
        public void replace (String oldStr, String newStr) { //替换 Text 的字符串
            sortSelector.forEach(i -> {
                var str = textResource.get(i).textStr;
                if (str == null) return;
                textResource.get(i).textStr = str.replace(oldStr, newStr);
            });
        }
        public void replaceAll (String regex, String replacement) { //通过正则匹配来替换字符串
            sortSelector.forEach(i -> {
                var str = textResource.get(i).textStr;
                if (str == null) return;
                textResource.get(i).textStr = str.replaceAll(regex, replacement);
            });
        }

        /**
         * 删除 Text 的方法，其中 resetColor 是对于通过模板初始化的 Text，删除之后是否要重新编号并且重新取色
         * */
        public void delete (boolean resetColor) {
            if (resetColor) {
                var ranges = sortSelector.getRanges();
                var lastRange = ranges.get(ranges.size() - 1);
                var removeSize = 0;

                for (int i = 0; i < ranges.size() - 1; i++) {
                    var range = ranges.get(i);
                    removeSize = removeStep1(range, removeSize);
                    removeStep2(range.right() + 2, ranges.get(i + 1).left(), removeSize);
                }

                if (lastRange.right() == textResource.size() - 1) {
                    for (int j = lastRange.left(); j <= lastRange.right(); j++) {
                        if (textResource.get(j).templateIndex > -1) removeSize++;
                    }
                } else {
                    removeSize = removeStep1(lastRange, removeSize);
                    removeStep2(lastRange.right() + 2, textResource.size(), removeSize);
                }
                index -= removeSize;
                sortSelector.forEachRange(false, new RangeWork() {
                    @Override
                    public void ifRange (int start, int end) { textResource.subList(end, start + 1).clear(); }
                    @Override
                    public void ifOne (int num) { textResource.remove(num); }
                });
            } else {
                sortSelector.forEachRange(false, new RangeWork() {
                    @Override
                    public void ifRange (int start, int end) {
                        checkNeedReset(end, start);
                        textResource.subList(end, start + 1).clear();
                    }
                    @Override
                    public void ifOne (int num) {
                        checkNeedReset(num, num);
                        textResource.remove(num);
                    }
                });
            }
        }
        public void delete () { delete(true); }

        /**
         * 统计 Range 对应的 Text 中有几个是通过模板初始化的，并且设置紧挨 Range 之后 Text（如果是通过模板初始化）的颜色
         * */
        private int removeStep1 (Range range, int removeSize) {
            for (int j = range.left(); j <= range.right(); j++) {
                if (textResource.get(j).templateIndex > -1) removeSize++;
            }
            var index = range.right() + 1;
            var text = textResource.get(index);
            if (text.templateIndex > -1) {
                text.templateIndex -= removeSize;
                text.setColorByTemplate(template, range.left() > 0 ? textResource.get(range.left() - 1) : null);
                checkNeedResetAtNext(index);
            } else checkNeedReset(range.left(), range.right());
            return removeSize;
        }

        /**
         * 在 from（含）之后 end 之前的 Text（如果是通过模板初始化），设置它们的颜色，并且检查紧挨其后的 Text 是否需要重置颜色
         * */
        private void removeStep2 (int from, int end, int removeSize) {
            for (int j = from; j < end; j++) {
                var text = textResource.get(j);
                if (text.templateIndex > -1) {
                    text.templateIndex -= removeSize;
                    text.setColorByTemplate(template, textResource.get(j - 1));
                    checkNeedResetAtNext(j);
                }
            }
        }

        /**
         * 插入新 Text 的一系列方法
         * */
        public TextEditor insert (Color textColor, Color backGroundColor, Object... text) {
            var num = new AtomicInteger(1); //记录第几次循环（从左往右依次插入）
            sortSelector.forEach(i -> { //遍历选定的下标集合
                i += insertSize * num.getAndIncrement() - 1; //计算每次插入的位置
                var previousText = i > 0 ? textResource.get(i - 1) : null;
                var nextText = textResource.get(i); //要在每一个选定的 Text 之前插入
                textResource.add(i, new Text(textColor, backGroundColor, previousText, text));
                nextText.setNeedReset(textResource.get(i));
            });
            insertSize ++; return this;
        }
        public TextEditor insert (Color textColor, Object... text) {
            return insert(textColor, Color.DEFAULT, text);
        }
        public TextEditor insert (Object... text) {
            return insert(Color.DEFAULT, Color.DEFAULT, text);
        }
    }

    /**
     * sortSelector 通过下标来指定选取范围，textEditWork 通过对应编辑器来修改 Text
     * */
    public Printer select (SortSelector sortSelector, Consumer<TextEditor> textEditWork) {
        if (sortSelector == null || textEditWork == null)
            throw new IllegalArgumentException("select 函数的参数均不能为空");
        if (!sortSelector.isSubscript(textResource.size()))
            throw new IllegalArgumentException("select 函数的 sortSelector 参数超出范围");
        textEditWork.accept(new TextEditor(sortSelector));
        return this;
    }

    /**
     * 移除所有 Text
     * */
    public Printer clear () {
        textResource.clear(); index = 0; return this;
    }

    /**
     * 带颜色的字符串
     * */
    public String toColorString (boolean resetAtEnd) {
        List<String> stringList = textResource.stream().map(Text::toColorString).collect(Collectors.toList());
        if (resetAtEnd) stringList.add(TextColors.ResetStr);
        return String.join("", stringList);
    }
    public String toColorString () {
        return toColorString(true);
    }

    @Override
    public String toString() {
        return textResource.stream().map(Text::toString).collect(Collectors.joining());
    }

    /**
     * 获取当前 ArrayList 中最后一个 Text ，列表为空就返回 null
     * */
    private Text getLastText() {
        if (textResource.size() == 0) return null;
        else return textResource.get(textResource.size() - 1);
    }
}
