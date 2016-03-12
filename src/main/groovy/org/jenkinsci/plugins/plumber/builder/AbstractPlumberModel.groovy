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

import java.lang.reflect.ParameterizedType

/**
 * Abstract class for other builder model classes to inherit from, so we can get fromNode.
 */
public abstract class AbstractPlumberModel<T extends AbstractPlumberModel<T>> {

    // TODO: Add some generalized validation hook here with implementations in the subclasses.

    public T fieldVal(String key, Object val) {
        this."${key}" = val
        (T) this
    }

    public T addValToList(String key, Object val) {
        this."${key}" << val
        (T) this
    }

    public T closureVal(String key, Class clazz, Closure<?> closure) {
        this."${key}" = resolveClosure(clazz, closure)
        (T) this
    }

    public T addClosureValToMap(String key, Class clazz, String mapKey, Closure<?> closure) {
        this."${key}".put(mapKey, resolveClosure(clazz, closure))
        (T) this
    }

    public T addClosureValToList(String key, Class clazz, Closure<?> closure) {
        this."${key}" << resolveClosure(clazz, closure)
        (T) this
    }

    private Object resolveClosure(Class clazz, Closure<?> closure) {
        def tmpObject = clazz.newInstance()

        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = tmpObject
        closure.call()

        return tmpObject
    }

    public Map toTree() {
        def tree = [:]

        tree.name = getClass().simpleName

        // Arguments that aren't nested closures.
        tree.args = [:]

        // Arguments that *are* nested closures.
        tree.closures = [:]

        // Get all the non-synthetic fields of the class we're in.
        this.getClass().getDeclaredFields().findAll { !it.isSynthetic() }.each { f ->
            // Stash aside the field name.
            String fieldName = f.name

            // If the field is actually populated in this instance...
            if (this."${fieldName}" != null) {
                // Grab the field value - we need that later.
                def fieldValue = this."${fieldName}"

                // If the field's a ParameterizedType, we need to check it to see if it's containing a Plumber class.
                if (f.getGenericType() instanceof ParameterizedType) {
                    // First class listed in the actual type arguments - we ignore anything past this because eh.
                    def containedClass = (Class) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]

                    // First, special casing for Maps, which we'll just copy in as is regardless of what they contain.
                    // We only have lists of possible closures, not maps of them, so...
                    if (fieldValue instanceof Map) {
                        tree.args."${fieldName}" = fieldValue
                    }
                    // Next, check if the contained class is a Plumber class.
                    else if (AbstractPlumberModel.class.isAssignableFrom(containedClass)) {

                        // If we've got a collection here, then we need to collect and transform its elements.
                        if (fieldValue instanceof Collection) {
                            tree.closures."${fieldName}" = ((Collection)fieldValue).collect { AbstractPlumberModel a ->
                                a.toTree()
                            }
                        }
                        // If it's not a collection, then just transform it.
                        else {
                            tree.closures."${fieldName}" = ((AbstractPlumberModel) fieldValue).toTree()
                        }
                    } else {
                        // This means it's a collection but not a collection of closures.
                        tree.args."${fieldName}" = fieldValue
                    }
                }
                // Non-parameterized type, so check if it's a Plumber class, transforming if needed.
                else if (AbstractPlumberModel.class.isAssignableFrom(f.getType())) {
                    tree.closures."${fieldName}" = ((AbstractPlumberModel) fieldValue).toTree()
                }
                // MappedClosures are handled a little special - pull the actual Map out of them and use that.
                else if (fieldValue instanceof MappedClosure) {
                    tree.args."${fieldName}" = ((MappedClosure) fieldValue).getRawMap()
                }
                // And lastly, if it's not a parameterized type and it's not a Plumber class *and* it's not a
                // MappedClosure, add it to args.
                else {
                    tree.args."${fieldName}" = fieldValue
                }
            }
        }

        return tree
    }
}
