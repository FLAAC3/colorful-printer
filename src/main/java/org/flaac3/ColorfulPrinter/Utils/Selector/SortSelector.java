package org.flaac3.ColorfulPrinter.Utils.Selector;

import org.flaac3.ColorfulPrinter.Utils.Selector.Range.LMaxRangeTwo;
import org.flaac3.ColorfulPrinter.Utils.Selector.Range.LMinRangeTwo;
import org.flaac3.ColorfulPrinter.Utils.Selector.Range.Range;

import java.util.*;

public class SortSelector extends Selector {
    /**
     * 和父类一样，首先要确定输入的 ranges 不能有重复元素，并且还需要从小到大排序，<br/>
     * 才可以直接用构造方法实例化，例如：
     * <pre>{@code new int[][]{{0},{2,5}}} //正确，因为 {@code {0,2,3,4,5}} 是按从小到大顺序排列的</pre>
     * */
    public SortSelector(int[][] ranges) {
        super(toListRanges(ranges));
    }
    public SortSelector(List<Range> ranges) {
        super(ranges);
    }

    /**
     * 传入一个取值范围数组（列表），去重并且按 从小到大 排序后再根据对应迭代器实例化 SortSelector
     * */
    public static SortSelector of (int[][] ranges) {
        return create(toLinkedHashSet(ranges));
    }
    public static SortSelector of (List<Range> ranges) {
        return create(toLinkedHashSet(ranges));
    }
    /**
     * 创建一个仅包含了大小信息（size）的 SortSelector
     * */
    private static SortSelector create (LinkedHashSet<Integer> linkedHashSet) {
        var listRanges = toListRanges(
                //这时接受到的 iterator 是按 从小到大 的顺序排列
                linkedHashSet.stream().sorted(Integer::compareTo).iterator()
        );
        var sortSelector = new SortSelector(listRanges); //创建一个 SortSelector
        sortSelector.setSize(linkedHashSet.size()); //设置元素总数
        return sortSelector; //返回 SortSelector
    }

    /**
     * 根据 index下标 返回对应的元素
     * */
    public int get (int index) {
        if (index < 0 || index >= size())
            throw new IndexOutOfBoundsException("get 方法的 index 参数超出范围，或者 SortSelector 本身为空");

        var indexList = getIndexList(); //索引列表
        var i = search(index, indexList.size() - 1); //二分法查找 indexList 中对应的下标

        return ranges.get(i).left() + (index - indexList.get(i));
    }

    /**
     * 此时 传入的selector 是本身的下标集合，返回本身的子集，相当于深拷贝
     * */
    @Override
    public Selector get (Selector selector) {
        if (!selector.isSubscript(size()))
            throw new IndexOutOfBoundsException("get 方法的 selector 参数超出范围，或者 SortSelector 本身为空");

        var out = new ArrayList<Range>(); //构造返回结果的元素列表

        selector.forEachRange(new RangeWork() {
            @Override
            public void ifRange (int start, int end) {
                var work = new RangeHandler();
                var left = 0; //左边界
                var right = 0; //右边界

                if (start < end) { //首先如果 start 小于 end
                    LMinSelector(start, end, work, out);
                } else { //如果 start 大于 end
                    right = work.first(start);
                    left = work.last(end);

                    if (work.i == work.j) {
                        out.add(new LMaxRangeTwo(right, left));
                    } else {
                        if (work.rangeI.left() == right)
                            out.add(new Range(right));
                        else
                            out.add(new LMaxRangeTwo(right, work.rangeI.left()));
                        for (int i = work.i - 1; i > work.j; i--) {
                            var range = ranges.get(i);
                            if (range.size() == 1)
                                out.add(range);
                            else
                                out.add(new LMaxRangeTwo(range.right(), range.left()));
                        }
                        if (left == work.rangeJ.right())
                            out.add(new Range(left));
                        else
                            out.add(new LMaxRangeTwo(work.rangeJ.right(), left));
                    }
                }
            }
            @Override
            public void ifOne (int num) {
                out.add(new Range(get(num)));
            }
        });
        return new Selector(out);
    }

    /**
     * 如果本身是从小到大排序的元素，选择的时候也是正序，则返回的也是从小到大排序的元素
     * */
    public SortSelector get (SortSelector sortSelector) {
        if (!sortSelector.isSubscript(size()))
            throw new IndexOutOfBoundsException("get 方法的 SortSelector 参数超出范围，或者 SortSelector 本身为空");

        var out = new ArrayList<Range>(); //构造返回结果的元素列表

        sortSelector.forEachRange(new RangeWork() {
            @Override
            public void ifRange (int start, int end) {
                LMinSelector(start, end, new RangeHandler(), out);
            }
            @Override
            public void ifOne (int num) {
                out.add(new Range(get(num)));
            }
        });
        return new SortSelector(out);
    }

    /**
     * @param start 展开后的起始下标
     * @param end 展开后的结束下标 <br/>
     * 这两个参数是指从元素列表中选择一个范围，并且开始下标要小于结束下标
     * @param out 把在选择范围中的 Range 添加到列表中
     * */
    private void LMinSelector (int start, int end, RangeHandler work, ArrayList<Range> out) {
        int right = work.first(end);
        int left = work.last(start);

        if (work.i == work.j) {
            out.add(new LMinRangeTwo(left, right));
        } else {
            if (left == work.rangeJ.right())
                out.add(new Range(left));
            else
                out.add(new LMinRangeTwo(left, work.rangeJ.right()));
            if (work.j != work.i - 1) out.addAll(ranges.subList(work.j + 1, work.i));
            if (work.rangeI.left() == right)
                out.add(new Range(right));
            else
                out.add(new LMinRangeTwo(work.rangeI.left(), right));
        }
    }

    /**
     * 重写父类获取范围最大值、最小值的方法
     * */
    @Override
    public int getMax() {
        return getLast();
    }

    @Override
    public int getMin() {
        return getFirst();
    }

    /**
     * 重写父类的 sort 方法，如果向上转型成父类后调用此方法可以直接返回，不用创建子类的新对象
     * */
    @Override
    public SortSelector sort() {
        return this;
    }

    /**
     * a 和 b 是两个已经从小到大排好序的迭代器（各自的元素不重复），合并成一个从小到大排好序的集合的迭代器（元素不重复）
     * */
    private static Iterator<Integer> merge (Iterator<Integer> a, Iterator<Integer> b) {
        return new Iterator<>() {
            private Integer aNum = null;
            private Integer bNum = null;
            private boolean has = a.hasNext() || b.hasNext(); //只要 a 或 b 中至少有一个还有元素，则 has 为 true

            @Override
            public boolean hasNext() {
                return has;
            }

            @Override
            public Integer next() {
                Integer temp;

                if (aNum == null && a.hasNext()) {
                    aNum = a.next();
                }
                if (bNum == null && b.hasNext()) {
                    bNum = b.next();
                }

                if (aNum == null) { //如果aNum为null，说明a迭代器已经遍历完，返回bNum，并将bNum置为null，更新has变量
                    temp = bNum; bNum = null; has = b.hasNext();
                } else {
                    if (bNum == null) { //如果bNum为null，说明b迭代器已经遍历完，返回aNum，并将aNum置为null，更新has变量
                        temp = aNum; aNum = null; has = a.hasNext();
                    } else {
                        if (aNum < bNum) {
                            temp = aNum; aNum = null;
                        } else if (aNum.equals(bNum)) {
                            //如果aNum等于bNum，则只返回一个元素（这里选择返回aNum），并将aNum和bNum都置为null，更新has变量
                            temp = aNum; aNum = null; bNum = null;
                            has = a.hasNext() || b.hasNext(); //因为这里一次性移除了两个值，要提前判断两个迭代器之后有没有值
                        } else {
                            temp = bNum; bNum = null;
                        }
                    }
                }
                return temp;
            }
        };
    }

    /**
     * 合并两个 SortSelector 成一个新的 SortSelector，取并集
     * */
    public SortSelector plus (SortSelector sortSelector) {
        var listRanges = toListRanges(merge(
                iterator(), sortSelector.iterator()
        ));
        return new SortSelector(listRanges);
    }

    /**
     * 减去另一个 Selector 成一个新的包含了大小信息（size）的 SortSelector，取差集
     * */
    @Override
    public SortSelector minus (Selector selector) {
        var hashSet = toLinkedHashSet(ranges);
        hashSet.removeAll(toLinkedHashSet(selector.ranges));
        var out = new SortSelector(toListRanges(hashSet.iterator()));
        out.setSize(hashSet.size());
        return out;
    }

    /**
     * a 和 b 是两个已经从小到大排好序的迭代器（各自的元素不重复），返回从 a 中剔除 b 中含有的元素的迭代器
     * */
    private static Iterator<Integer> remove (Iterator<Integer> a, Iterator<Integer> b) {
        return new Iterator<>() {
            private Integer next;
            private Integer previousBNum = null;

            /*
            * 如果直接写成 private Integer next = getNext(); 因为 previousBNum 此时还没有定义，
            * 所以 getNext() 方法中用到 previousBNum 的代码就会被忽略？离谱的是还不会报错。可以用{}构造代码块
            * 来延迟调用 getNext() 方法，或者将定义 previousBNum 的语句挪到前面来解决问题。
            * */
            { next = getNext(); }

            private Integer getNext () {
                loopA: while (a.hasNext()) { var aNum = a.next();
                    if (previousBNum != null) {
                        if (aNum < previousBNum) return aNum;
                        else if (aNum.equals(previousBNum)) {
                            previousBNum = null; continue;
                        } else previousBNum = null;
                    }
                    while (b.hasNext()) { var bNum = b.next();
                        if (bNum.equals(aNum)) continue loopA;
                        else if (bNum > aNum) {
                            previousBNum = bNum; return aNum;
                        }
                    }
                    return aNum;
                }
                return null;
            }

            @Override
            public boolean hasNext () {
                return next != null;
            }

            @Override
            public Integer next () {
                var temp = next; next = getNext(); return temp;
            }
        };
    }

    /**
     * 减去另一个 SortSelector 成一个新的 SortSelector，取差集
     * */
    public SortSelector minus (SortSelector sortSelector) {
        var listRanges = toListRanges(remove(
                iterator(), sortSelector.iterator()
        ));
        return new SortSelector(listRanges);
    }

    /**
     * 用 stream流 的方式把 范围数组 转换成 范围列表（必须保证传入的 range 二维数组符合要求）
     * */
    private static ArrayList<Range> toListRanges (int[][] ranges) {
        var out = new ArrayList<Range>();
        Arrays.stream(ranges).forEach(each -> {
            if (each.length == 1)
                out.add(new Range(each[0]));
            else
                out.add(new LMinRangeTwo(each[0], each[1]));
        });
        return out;
    }

    /**
     * 将一串从小到大排列的整数根据连续性分组成范围列表
     * */
    @SuppressWarnings("Duplicates")
    private static ArrayList<Range> toListRanges (Iterator<Integer> iterator) {
        ArrayList<Range> listRanges = new ArrayList<>(); //范围列表
        byte flag = 0; //0:当前元素不连续，1:当前元素递增
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
        while (iterator.hasNext()) { //从第二个元素开始遍历
            int now = iterator.next(); //当前元素
            if (now == previous + 1) {
                flag = 1;
                previous = now; //把 上一个元素值 设置成 当前元素值
                if (!iterator.hasNext()) { //如果当前元素是最后一个
                    listRanges.add(new LMinRangeTwo(left, previous));break;
                }
            } else {
                if (flag == 1) { //如果上次遍历的元素递增
                    flag = 0;
                    listRanges.add(new LMinRangeTwo(left, previous));
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
