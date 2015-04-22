package com.webmbt.mscript;

import static java.lang.Double.parseDouble;

/**
 * Currently, when interpreting MScript expressions formed using one or more <em>comparison</em> or <em>arithmetic</em>
 * unary or binary operators:
 * <ul>
 * <li>the <em>true</em> boolean value is interpreted as <em>1</em> and the <em>false</em> boolean value is interpreted
 * as <em>0</em> in binary comparison and all arithmetic operations</li>
 * <li>boolean literals (<em>true</em> and <em>false</em>) are case-sensitive i.e. True is interpreted as an identifier
 * and not as a boolean value</li>
 * <li>string literals <em>'true'</em> and <em>'false'</em> are interpreted as boolean values; <em>'True'</em>,
 * etc. are interpreted as plain string values</li>
 * <li>whenever an operand value cannot be coerced (implicitly converted) to a numeric or boolean value, all operand
 * values in the expression are coerced to string values (such coercion always succeeds)</li>
 * <p/>
 * </ul>
 *
 * @author TestOptimal, LLC
 * @version 1.0, Apr 15, 2015
 */
public class Types {

    public static String asString(double d) {
        String s = Double.toString(d);
        return d % 1 == 0 ? s.substring(0, s.indexOf(".")) : s;
    }

    public static String asString(boolean b) {
        return b ? "true" : "false";
    }

    public static String asString(Object o) {
        if (o instanceof Number) {
            return asString(((Number) o).doubleValue());
        }
        return String.valueOf(o);
    }

    /**
     * @throws NullPointerException  if {@code value} is {@code null}
     * @throws NumberFormatException if {@code value} does not represent a {@link #isTrue(String) boolean}
     *                               {@link #isFalse(String) value} and does not contain a parsable {@code double}.
     */
    public static double asNumber(String value) {
        if (isTrue(value)) {
            return 1;
        }
        if (isFalse(value)) {
            return 0;
        }
        return parseDouble(value);
    }

    public static boolean isTrue(String value) {
        return "true".equals(value);
    }

    public static boolean isFalse(String value) {
        return "false".equals(value);
    }
}
