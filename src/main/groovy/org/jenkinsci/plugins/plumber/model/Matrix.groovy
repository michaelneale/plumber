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
package org.jenkinsci.plugins.plumber.model

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class Matrix extends AbstractPlumberModel {
    MappedClosure axes

    public Matrix() {

    }

    public Matrix(Map<String,Object> args) {
        if (args != null) {
            if (args.containsKey("axes") && args.axes instanceof Map) {
                this.axes = new MappedClosure((Map<String, Object>) args.axes)
            }
        }
    }

    // NOTE: Going with 'matrix { axes { FOO ["a", "b", "c"] } }' syntax so that we don't get screwed when we eventually
    // need to support other configuration options on Matrix.
    Matrix axes(Closure<?> closure) {
        closureVal("axes", MappedClosure.class, closure)
    }

    /**
     * Gets a list of maps for each possible combination of the matrix.
     *
     * @return a list of maps, with env var name as key and value of the env var as value
     */
    public List<Map<String,Object>> matrixCombinations() {
        def combos = []

        // Just a bit of protection in case of empty axes.
        if (!axes.isEmpty()) {
            // toList needed here and below to get combinations and transpose to play nice together.
            def keyList = axes.keySet().toList()

            List<List<String>> valueCombos = axes.values().toList().combinations()

            valueCombos.each { List<String> combo ->
                combos.add([keyList, combo].transpose().collectEntries())
            }
        }

        return combos
    }
}