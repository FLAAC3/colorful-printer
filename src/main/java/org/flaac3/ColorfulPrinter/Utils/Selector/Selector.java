package org.flaac3.ColorfulPrinter.Utils.Selector;

import org.flaac3.ColorfulPrinter.Model.Color;
import org.flaac3.ColorfulPrinter.Printer;
import org.flaac3.ColorfulPrinter.Utils.IntPair;
import org.flaac3.ColorfulPrinter.Utils.Selector.Range.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Selector {
    protected final ArrayList<Range> ranges; //存储范围的列表
    private ArrayList<Integer> indexList = null; //不能直接调用，得用 getIndexList 方法，表示索引列表
    private int size = -1; //不能直接调用，得用 getSize 方法，表示范围中的元素数量

    /**
     * 要是确定输入的 ranges 没有错误，才可以直接用构造方法实例化，要求不能重复选择，比如：<br/>
     * <pre>{@code new int[][]{{1,3},{2}}} //错误，因为 {@code 2} 已经包含在 {@code 1~3} 里面了</pre>
     * 如果元素重复，我也不知道会有什么效果。这里给一个正确的例子：
     * <pre>{@code new int[][]{{3},{0,2},{6,4}}} //正确，相当于{@code {3,0,1,2,6,5,4}}</pre>
     * */
    public Selector (int[][] ranges) {
        this.ranges = toListRanges(ranges);
    }
    public Selector (List<Range> ranges) { //浅拷贝
        if (ranges instanceof ArrayList) this.ranges = (ArrayList<Range>) ranges;
        else this.ranges = new ArrayList<>(ranges);
    }

    /**
     * 传入一个范围，去重后再实例化 Selector
     * */
    public static Selector of (int[][] ranges) {
        return create(toLinkedHashSet(ranges));
    }
    public static Selector of (List<Range> ranges) {
        return create(toLinkedHashSet(ranges));
    }

    /**
     * 创建一个仅包含了大小信息（size）的 Selector
     * */
    private static Selector create (LinkedHashSet<Integer> linkedHashSet) {
        var listRanges = toListRanges(linkedHashSet.iterator());
        var selector = new Selector(listRanges); //创建一个 Selector
        selector.setSize(linkedHashSet.size()); //设置元素总数
        return selector;
    }

    /**
     * @return 当前 ranges 展开后的元素总数
     * */
    public int size () {
        if (size == -1) {
            size = 0;
            if (indexList == null) {
                ranges.forEach(range -> size += range.size());
            } else if (ranges.size() > 0) {
                var last = indexList.size() - 1;
                size = indexList.get(last) + ranges.get(last).size();
            }
        }
        return size;
    }
    protected void setSize (int size) {
        this.size = size;
    }

    protected ArrayList<Integer> getIndexList () {
        if (indexList == null) {
            var indexList = new ArrayList<>(List.of(0)); //索引列表，起始值为 0
            for (int i = 1; i < ranges.size(); i++) { //计算索引列表
                indexList.add(indexList.get(i - 1) + ranges.get(i - 1).size());
            }
            this.indexList = indexList;
        }
        return indexList;
    }

    public int rangesSize () {
        return ranges.size();
    }

    public ArrayList<Range> getRanges () {
        return ranges;
    }

    /**
     * 用二分法在 indexList 中快速查找 <br/>
     * @param indexNum ranges 展开成一个个元素后的下标
     * @param fromIndex indexList 从后往前遍历的起始下标
     * @return indexList 中对应的下标
     * */
    protected int search (int indexNum, int fromIndex) {
        var indexList = getIndexList(); //索引列表
        var size = indexList.size();
        if (indexNum >= indexList.get(fromIndex)) return fromIndex;
        while (true) {
            size >>= 1;
            var middle = fromIndex - size;
            if (indexNum < indexList.get(middle)) {
                if (size == 1) return middle - 1;
                fromIndex = middle;
            } else if (size == 1) return middle;
        }
    }

    /**
     * 根据 index下标 返回对应的元素
     * */
    public int get (int index) {
        if (index < 0 || index >= size())
            throw new IndexOutOfBoundsException("get 方法的 index 参数超出范围，或者 Selector 本身为空");

        var indexList = getIndexList(); //获取索引列表
        var i = search(index, indexList.size() - 1); //二分法查找 indexList 中对应的下标
        var range = ranges.get(i); //获取对应的 Range

        if (range.isLMin())
            return range.left() + (index - indexList.get(i));
        else
            return range.left() - (index - indexList.get(i));
    }

    /**
     * 这个类用来封装输入 index 来查找对应元素的方法
     * */
    protected class RangeHandler {
        List<Integer> indexList = getIndexList();
        int i; //第一次查找指定 index 时，记录 indexList/ranges 的下标
        Range rangeI; //第一次查找指定 index 时，记录 range
        int j; //第二次查找指定 index 时，记录 indexList/ranges 的下标（小于等于i）
        Range rangeJ; //第二次查找指定 index 时，记录 range

        /**
         * 第一次查找，从 indexList 的最后一项递减查询
         * */
        int first (int index) {
            return get(index, indexList.size() - 1, (num, range) -> {
                i = num;
                rangeI = range;
            });
        }

        /**
         * 第二次查找，在第一次查找位置以及之前开始递减查询
         * */
        int last (int index) {
            return get(index, i, (num, range) -> {
                j = num;
                rangeJ = range;
            });
        }

        /**
         * 根据输入的 index（索引位置）和 from（indexList的起始位置）返回对应元素，通过 Lambda 返回 索引位置 和 Range
         * */
        int get (int index, int from, BiConsumer<Integer, Range> consumer) {
            from = search(index, from); //二分法查找 indexList 中对应的下标
            var range = ranges.get(from);
            consumer.accept(from, range);
            if (range.isLMin())
                return range.left() + (index - indexList.get(from));
            else
                return range.left() - (index - indexList.get(from));
        }
    }
    /**
     * 此时 传入的selector 是本身的下标集合，返回本身的子集，相当于深拷贝
     * */
    public Selector get (Selector selector) {
        if (!selector.isSubscript(size()))
            throw new IndexOutOfBoundsException("get 方法的 selector 参数超出范围，或者 Selector 本身为空");

        var out = new ArrayList<Range>(); //构造返回结果的元素列表

        selector.forEachRange(new RangeWork() {
            @Override
            public void ifRange (int start, int end) {
                var work = new RangeHandler();
                var left = 0; //左边界
                var right = 0; //右边界

                if (start < end) { //首先如果 start 小于 end
                    right = work.first(end);
                    left = work.last(start);

                    if (work.i == work.j) {
                        if (work.rangeI.isLMin()) out.add(new LMinRangeTwo(left, right));
                        else out.add(new LMaxRangeTwo(left, right));
                    } else {
                        if (left == work.rangeJ.right()) {
                            out.add(new Range(left));
                        } else {
                            if (work.rangeJ.isLMin()) out.add(new LMinRangeTwo(left, work.rangeJ.right()));
                            else out.add(new LMaxRangeTwo(left, work.rangeJ.right()));
                        }
                        if (work.j != work.i - 1) out.addAll(ranges.subList(work.j + 1, work.i));
                        if (work.rangeI.left() == right) {
                            out.add(new Range(right));
                        } else {
                            if (work.rangeI.isLMin()) out.add(new LMinRangeTwo(work.rangeI.left(), right));
                            else out.add(new LMaxRangeTwo(work.rangeI.left(), right));
                        }
                    }
                } else { //如果 start 大于 end
                    right = work.first(start);
                    left = work.last(end);

                    if (work.i == work.j) {
                        if (work.rangeI.isLMin()) out.add(new LMaxRangeTwo(right, left));
                        else out.add(new LMinRangeTwo(right, left));
                    } else {
                        if (work.rangeI.left() == right) {
                            out.add(new Range(right));
                        } else {
                            if (work.rangeI.isLMin()) out.add(new LMaxRangeTwo(right, work.rangeI.left()));
                            else out.add(new LMinRangeTwo(right, work.rangeI.left()));
                        }
                        for (int i = work.i - 1; i > work.j; i--) {
                            var range = ranges.get(i);
                            if (range.size() == 1) {
                                out.add(range);
                            } else {
                                if (range.isLMin()) out.add(new LMaxRangeTwo(range.right(), range.left()));
                                else out.add(new LMinRangeTwo(range.right(), range.left()));
                            }
                        }
                        if (left == work.rangeJ.right()) {
                            out.add(new Range(left));
                        } else {
                            if (work.rangeJ.isLMin()) out.add(new LMaxRangeTwo(work.rangeJ.right(), left));
                            else out.add(new LMinRangeTwo(work.rangeJ.right(), left));
                        }
                    }
                }
            }
            @Override
            public void ifOne (int num) {
                out.add(new Range(get(num)));
            }
        });
        return new Selector(shortenRanges(out));
    }

    /**
     * 查找连续的 Range 并且合并为一个
     * */
    private static ArrayList<Range> shortenRanges (ArrayList<Range> in) {
        ArrayList<Range> out = new ArrayList<>();

        for (int i = 0; i < in.size(); i++) {
            int start = in.get(i).left();
            Integer end = null;
            boolean isLMin = true;

            for (int j = i + 1; j < in.size(); j++) {
                var right = in.get(j - 1).right();
                var left = in.get(j).left();

                if (left == right + 1) {
                    i++;
                    end = in.get(j).right();
                } else if (left == right - 1) {
                    i++; isLMin = false;
                    end = in.get(j).right();
                } else break;
            }

            if (end == null) {
                out.add(in.get(i));
            } else {
                if (isLMin)
                    out.add(new LMinRangeTwo(start, end));
                else
                    out.add(new LMaxRangeTwo(start, end));
            }
        }
        return out;
    }

    /**
     * 返回当前 selector 的最大值
     * */
    public int getMax () {
        var intPair = new IntPair(); //数字处理工具
        ranges.forEach( range -> intPair.setBMax(range.max()));
        return intPair.getB();
    }

    /**
     * 返回当前 selector 的最小值
     * */
    public int getMin () {
        var intPair = new IntPair(); //数字处理工具
        ranges.forEach( range -> intPair.setAMin(range.min()));
        return intPair.getA();
    }

    /**
     * 取第一个元素出来
     * */
    public int getFirst () {
        return ranges.get(0).left();
    }

    /**
     * 取最后一个元素出来
     * */
    public int getLast () {
        return ranges.get(ranges.size() - 1).right();
    }

    /**
     * 检查当前 selector 能不能作为 “大小为size的集合/列表” 的下标集合
     * */
    public boolean isSubscript (int size) {
        return size() != 0 && getMax() < size && getMin() >= 0;
    }

    /**
     * 两种遍历的方式，其中 isForward 设置是否正向遍历
     * */
    public void forEach (boolean isForward, Consumer<Integer> work) {
        if (isForward) {
            ranges.forEach(range -> range.forEach(true, work));
        } else {
            for (int i = ranges.size() - 1; i >= 0; i--) {
                ranges.get(i).forEach(false, work);
            }
        }
    }
    public void forEach (Consumer<Integer> work) {
        forEach(true, work);
    }
    public void forEachRange (boolean isForward, RangeWork work) {
        if (isForward) {
            ranges.forEach( range -> {
                if (range.size() == 1) {
                    work.ifOne(range.left());
                } else {
                    work.ifRange(range.left(), range.right());
                }
            });
        } else {
            for (int i = ranges.size() - 1; i >= 0; i--) {
                var range = ranges.get(i);
                if (range.size() == 1) {
                    work.ifOne(range.left());
                } else {
                    work.ifRange(range.right(), range.left());
                }
            }
        }
    }
    public void forEachRange (RangeWork work) {
        forEachRange(true, work);
    }

    /**
     * 根据当前信息创建子类 SortSelector 对象
     * */
    public SortSelector sort () {
        return SortSelector.of(ranges);
    }

    /*
    /**
     * a 和 b 是两个各自元素不重复的迭代器，合并成一个元素不重复的迭代器（对于重复元素，优先取在 a 中的位置）
     * *\/
    private static Iterator<Integer> merge (Iterator<Integer> a, Iterator<Integer> b) {
        return new Iterator<>() {
            private final HashSet<Integer> visitedSet = new HashSet<>();
            private Integer next = null;

            @Override
            public boolean hasNext() {
                return next != null || a.hasNext() || b.hasNext();
            }

            @Override
            public Integer next() {
                if (a.hasNext()) {
                    next = a.next(); visitedSet.add(next);
                    if (!a.hasNext()) {
                        var temp = next;
                        if (b.hasNext()) {
                            do next = b.next();
                            while (visitedSet.contains(next) && b.hasNext());
                        }
                        if (visitedSet.contains(next)) next = null;
                        return temp;
                    }
                    return next;
                } else {
                    if (visitedSet.size() == 0) {
                        next = b.next();
                        if (!b.hasNext()) {
                            var temp = next; next = null; return temp;
                        }
                        return next;
                    }
                    var temp = next;
                    if (b.hasNext()) {
                        do next = b.next();
                        while (visitedSet.contains(next) && b.hasNext());
                    } else {
                        next = null; return temp;
                    }
                    if (visitedSet.contains(next)) next = null;
                    return temp;
                }
            }
        };
    }
    * */

    /**
     * 合并另一个 Selector 成一个新的包含了大小信息（size）的 Selector，取并集
     * */
    public Selector plus (Selector selector) {
        var set = toLinkedHashSet(ranges);
        set.addAll(toLinkedHashSet(selector.ranges));
        return create(set);
    }

    /**
     * 减去另一个 Selector 成一个新的包含了大小信息（size）的 Selector，取差集
     * */
    public Selector minus (Selector selector) {
        var set = toLinkedHashSet(ranges);
        set.removeAll(toLinkedHashSet(selector.ranges));
        return create(set);
    }

    /**
     * 返回迭代器
     * */
    public Iterator<Integer> iterator () {
        return new Iterator<>() {
            private final ArrayList<Integer> indexList = getIndexList();
            private final int lastProcess = indexList.size() - 1;
            private final int size = size();
            private int process = 0;
            private int index = 0;

            @Override
            public boolean hasNext() { return index < size; }

            @Override
            public Integer next() {
                int out; var range = ranges.get(process);
                if (range.isLMin())
                    out = range.left() + (index - indexList.get(process));
                else
                    out = range.left() - (index - indexList.get(process));
                index ++; var nextProcess = process + 1;
                if (nextProcess <= lastProcess && index == indexList.get(nextProcess))
                    process = nextProcess;
                return out;
            }
        };
    }

    /**
     * 根据当前信息创建 int[] 元素列表
     * */
    public int[] toIntArray () {
        var out = new int[size()];
        var count = new AtomicInteger(0);
        forEach(num -> out[count.getAndIncrement()] = num);
        return out;
    }

    /**
     * 重写 toString 方法
     * */
    @Override
    public String toString() {
        var printer = new Printer('{');
        forEachRange(new RangeWork() {
            @Override
            public void ifRange(int start, int end) {
                printer.appendAtPreIfSameColor(Color.GREEN, null, String.format("[%d~%d], ", start, end));
            }
            @Override
            public void ifOne(int num) {
                printer.appendAtPreIfSameColor(Color.YELLOW, null, num, ", ");
            }
        });
        return printer.select(printer.ThisOne(), editor -> editor.replaceAll(".{2}$", ""))
                .append(null, '}').toColorString();
    }

    /**
     * 传入范围数组（范围列表），返回一个全部的、满足范围的有序取值 LinkedHashSet 集合（去重）
     * */
    protected static LinkedHashSet<Integer> toLinkedHashSet (int[][] ranges) {
        var linkedHashSet = new LinkedHashSet<Integer>();
        for (int[] range : ranges) {
            if (range == null) continue; //对 null 值的处理，跳过当前循环
            if (range.length == 1) {
                linkedHashSet.add(range[0]);
            } else if (range.length >= 2) {
                if (range[0] <= range[1]) {
                    for (int i = range[0]; i <= range[1]; i++) linkedHashSet.add(i);
                } else {
                    for (int i = range[0]; i >= range[1]; i--) linkedHashSet.add(i);
                }
            }
        }
        return linkedHashSet;
    }
    protected static LinkedHashSet<Integer> toLinkedHashSet (List<Range> ranges) {
        var linkedHashSet = new LinkedHashSet<Integer>();
        ranges.forEach( range -> {
            if (range == null) return;
            range.forEach(linkedHashSet::add);
        });
        return linkedHashSet;
    }

    /**
     * 用 stream流 的方式把 范围数组 转换成 范围列表（必须保证传入的 range 二维数组符合要求）
     * */
    private static ArrayList<Range> toListRanges (int[][] ranges) {
        var out = new ArrayList<Range>();
        Arrays.stream(ranges).forEach( each -> {
            if (each.length == 1) {
                out.add(new Range(each[0]));
            } else {
                if (each[0] < each[1]) out.add(new LMinRangeTwo(each[0], each[1]));
                else out.add(new LMaxRangeTwo(each[0], each[1]));
            }
        });
        return out;
    }

    /**
     * 将一串无序或部分有序的整数根据连续性分组成范围列表
     * */
    @SuppressWarnings("Duplicates")
    private static ArrayList<Range> toListRanges (Iterator<Integer> iterator) {
        ArrayList<Range> listRanges = new ArrayList<>(); //范围列表
        byte flag = 0; //0:当前元素不连续，1:当前元素递增，2:当前元素递减
        int previous = 0; //上一个元素值
        int left = 0; //范围左边界

        if (iterator.hasNext()) {
            previous = iterator.next(); //首先拿出第一个元素
            if (iterator.hasNext()) {
                left = previous; //如果之后还有元素，则把 第一个元素值 赋值给 左边界
            } else {
                listRanges.add(new Range(previous));
            }
        }

        while (iterator.hasNext()) {
            var now = iterator.next();
            if (now == previous + 1) {
                flag = 1;
                previous = now; //把 上一个元素值 设置成 当前元素值
                if (!iterator.hasNext()) { //如果当前元素是最后一个
                    listRanges.add(new LMinRangeTwo(left, previous));break;
                }
            } else if (now == previous - 1) {
                flag = 2;
                previous = now; //把 上一个元素值 设置成 当前元素值
                if (!iterator.hasNext()) { //如果当前元素是最后一个
                    listRanges.add(new LMaxRangeTwo(left, previous));break;
                }
            } else {
                if (flag == 1) { //如果上次遍历的元素递增
                    flag = 0;
                    listRanges.add(new LMinRangeTwo(left, previous));
                } else if (flag == 2) { //如果上次遍历的元素递减
                    flag = 0;
                    listRanges.add(new LMaxRangeTwo(left, previous));
                } else {
                    listRanges.add(new Range(left));
                }
                if (!iterator.hasNext()) { //如果当前元素是最后一个
                    listRanges.add(new Range(now));break;
                }
                previous = now; //把 上一个元素值 设置成 当前元素值
                left = now; //把 当前元素值 赋值给 左边界
            }
        }
        return listRanges;
    }
}
