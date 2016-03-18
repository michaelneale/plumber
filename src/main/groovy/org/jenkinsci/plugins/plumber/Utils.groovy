/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.plumber

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class Utils {

    public static List<String> listForArg(Object arg) {
        return (List<String>) listForArg(String.class, arg)
    }

    public static List listForArg(Class clazz, Object arg) {
        if (clazz != null && arg != null) {
            if (clazz.isInstance(arg)) {
                return [arg]
            } else if (arg instanceof List) {
                return arg
            } else {
                // TODO: Handle Set and other collections? Nah, probably not.
                // TODO: Some error reporting though.
                return null
            }
        } else {
            return null
        }
    }

    public static String toPlumberArg(Map tree, int tabDepth = 0) {

        def tabs = getTabs(tabDepth)

        def bldr = new StringBuilder()

        tree.args.each { k, v ->
            bldr.append("${tabs}${k} ${toArgForm(v)}\n\n")
        }

        tree.closures.each { k, Map v ->
            bldr.append("${tabs}${k} {\n")
            bldr.append(toPlumberArg(v, tabDepth + 1))
            bldr.append("${tabs}}\n\n")
        }

        return bldr.toString()
    }

    public static String toArgForm(Object v) {
        if (v instanceof Map) {
            return v.collect { "${it.key}: ${quoteValIfNeeded(it.value)}" }.join(", ")
        } else if (v instanceof List || v instanceof Set) {
            return v.collect { quoteValIfNeeded(it) }.join(", ")
        } else {
            return quoteValIfNeeded(v)
        }
    }

    public static String quoteValIfNeeded(Object v) {
        if (v instanceof Boolean) {
            return v
        } else {
            return "\"${v}\""
        }
    }

    public static String getTabs(int tabDepth) {
        "\t" * tabDepth
    }

}
