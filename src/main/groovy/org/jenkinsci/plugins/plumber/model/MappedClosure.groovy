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

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted


/**
 * Helper class for translating from a closure to a map.
 */
@ToString
@EqualsAndHashCode
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
class MappedClosure implements Serializable, MethodMissingWrapper, ModelForm {
    @Delegate Map<String,Object> delegate = [:]

    public MappedClosure() {

    }

    @Whitelisted
    public MappedClosure(Map<String,Object> inMap) {
        this.delegate.putAll(inMap)
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
        if (args.length > 1) {
            this."${methodName}" = args
        } else if (args.length == 1) {
            this."${methodName}" = args[0]
        }

        // TODO: Probably error out if we get an empty args?
        
        this
    }

    @Whitelisted
    public void modelFromMap(Map<String,Object> inMap) {
        this.delegate.putAll(inMap)
    }

    @Whitelisted
    public Map<String, Object> getMap() {
        def mapCopy = [:]
        mapCopy.putAll(delegate)
        return mapCopy
    }

    public Map toTree() {
        def tree = [:]

        tree.name = delegate.name
        tree.args = [:]
        tree.closures = [:]

        tree.args.putAll(delegate)

        return tree
    }

    static final int serialVersionUID = 1L
}
