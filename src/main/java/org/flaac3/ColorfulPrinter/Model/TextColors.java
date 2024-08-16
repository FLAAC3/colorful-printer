package org.flaac3.ColorfulPrinter.Model;

public record TextColors (Color textColor, Color backGroundColor) {
    public static final TextColors NO_COLOR = new TextColors(null, null);
    public static final String ResetStr = "\u001b[0m";
    public static final String NextLineStr = "\r\n";
    public static String getTextColorStr (Color c) {
        return String.format("\u001b[38;5;%dm", c.colorID());
    }
    public static String getBackGroundColorStr (Color c) {
        return String.format("\u001b[48;5;%dm", c.colorID());
    }
}