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

import org.jenkinsci.plugins.plumber.model.MappedClosure
import org.jenkinsci.plugins.plumber.model.MethodMissingWrapper

public class ClosureTranslatorMap implements MethodMissingWrapper, Serializable {
    Map<String,Object> actualMap = [:]
    Class actualClass

    ClosureTranslatorMap(String clazz) {
        actualClass = Class.forName("org.jenkinsci.plugins.plumber.model.${clazz}")
    }

    ClosureTranslatorMap(Class clazz) {
        actualClass = clazz
    }

    /**
     * Convenience method to allow for "foo 'bar'" style population of the underlying map.
     *
     * @param methodName Key name, basically.
     * @param args First element will be a String hopefully.
     *
     * @return this
     */
    def methodMissing(String methodName, args) {
        def argValue
        if (args.length > 1) {
            argValue = args
        } else if (args.length == 1) {
            argValue = args[0]
        }

        if (Utils.assignableFromWrapper(MappedClosure.class, actualClass) && argValue != null) {
            actualMap[methodName] = argValue
        } else {
            def resultValue
            def actualFieldName = Utils.actualFieldName(actualClass, methodName)
            if (actualFieldName != null) {
                if (argValue != null && Utils.instanceOfWrapper(Closure.class, argValue)) {
                    Closure argClosure = argValue
                    def actualType = Utils.actualFieldType(actualClass, methodName)

                    def ctm = new ClosureTranslatorMap(actualType)

                    argClosure.delegate = ctm
                    argClosure.resolveStrategy = Closure.DELEGATE_ONLY
                    argClosure.call()

                    resultValue = ctm.getMap()
                } else {
                    resultValue = argValue
                }


                if (Utils.isFieldA(List.class, actualClass, methodName)) {
                    if (!actualMap.containsKey(actualFieldName)) {
                        actualMap[actualFieldName] = []
                    }
                    actualMap[actualFieldName] << resultValue
                } else {
                    actualMap[actualFieldName] = resultValue
                }
                // TODO: Probably error out if we get an empty args?
            }
        }
        this
    }

    public Map<String, Object> getMap() {
        def mapCopy = [:]
        mapCopy.putAll(actualMap)
        return mapCopy
    }

}

