package com.webmbt.mscript;

import static java.lang.Double.parseDouble;

/**
 * Currently, when interpreting MScript expressions formed using zero, one or more comparison or arithmetic unary or
 * binary operators:
 * <ul>
 * <li>the <em>true</em> boolean value is interpreted as <em>1</em> and the <em>false</em> boolean value is interpreted
 * as <em>0</em> in binary comparison and all arithmetic operations</li>
 * <li>boolean literals (<em>true</em> and <em>false</em>) are case-sensitive i.e. True is interpreted as an identifier
 * and not as a boolean value</li>
 * <li>string literals <em>'true'</em> and <em>'false'</em> are interpreted as boolean values</li>
 * <li>whenever an operand value cannot be coerced to a numeric or boolean value, all operand values in the expression
 * are coerced to string values</li>
 * <p/>
 * </ul>
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Apr 15, 2015
 */
public class Types {

    /**
     * @throws NullPointerException  if {@code value} is {@code null}
     * @throws NumberFormatException if {@code value} is not a {@link #isTrue(String) boolean}
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
