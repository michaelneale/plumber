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

    public static String toPipelineScript(Map tree, int tabDepth = 0) {

        def tabs = getTabs(tabDepth)

        def bldr = new StringBuilder()

        tree.args.each { k, v ->
            if (v instanceof Map) {
                bldr.append("${tabs}${k} " + v.collect { argKey, argVal -> "${argKey}: \"${argVal}\"" }.join(", "))
            } else if (v instanceof List || v instanceof Set) {
                bldr.append("${tabs}${k} " + v.collect { "\"${it}\"" }.join(", "))
            } else {
                bldr.append("${tabs}${k} \"${v}\"")
            }
            bldr.append("\n\n")
        }

        tree.closures.each { k, Map v ->
            bldr.append("${tabs}${k} {\n")
            bldr.append(toPipelineScript(v, tabDepth + 1))
            bldr.append("${tabs}}\n\n")
        }

        return bldr.toString()
    }

    private static String getTabs(int tabDepth) {
        "\t" * tabDepth
    }
}
