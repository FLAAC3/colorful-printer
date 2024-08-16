package org.flaac3.ColorfulPrinter.Model;

import java.util.Objects;

public record Color (int colorID) {
    // DEFAULT 表示不更改当前的颜色，例如 setColors(Color.DEFAULT) 的效果就相当于没写这行代码
    public static final Color DEFAULT = new Color(-1);
    public static final Color BLACK = new Color(0);
    public static final Color WHITE = new Color(15);
    public static final Color RED = new Color(88);
    public static final Color GREEN = new Color(10);
    public static final Color YELLOW = new Color(11);
    public static final Color BLUE = new Color(27);
    public static final Color MAGENTA = new Color(197);
    public static final Color CYAN = new Color(57);

    @Override
    public boolean equals (Object o) { //判断 colorID 是否相等
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return colorID == ((Color) o).colorID;
    }

    @Override
    public int hashCode () {
        return Objects.hash(colorID);
    }
}
