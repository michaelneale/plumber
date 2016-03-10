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
package org.jenkinsci.plugins.plumber.builder

/**
 * Abstract class for other builder model classes to inherit from, so we can get fromNode.
 */
public abstract class AbstractPlumberModel<T extends AbstractPlumberModel<T>> {

    // TODO: Add some generalized validation hook here with implementations in the subclasses.

    public T fieldVal(String key, Object val) {
        this."${key}" = val
        (T)this
    }

    public T addValToList(String key, Object val) {
        this."${key}" << val
        (T)this
    }

    public T closureVal(String key, Class clazz, Closure<?> closure) {
        this."${key}" = resolveClosure(clazz, closure)
        (T)this
    }

    public T addClosureValToMap(String key, Class clazz, String mapKey, Closure<?> closure) {
        this."${key}".put(mapKey, resolveClosure(clazz, closure))
        (T)this
    }

    public T addClosureValToList(String key, Class clazz, Closure<?> closure) {
        this."${key}" << resolveClosure(clazz, closure)
        (T)this
    }

    private Object resolveClosure(Class clazz, Closure<?> closure) {
        def tmpObject = clazz.newInstance()

        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = tmpObject
        closure.call()

        return tmpObject
    }
}
