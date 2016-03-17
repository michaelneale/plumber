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

import java.lang.reflect.ParameterizedType
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

/**
 * Abstract class for other model classes to inherit from, so we can get a ton of convenience methods.
 */
 @SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public abstract class AbstractPlumberModel<T extends AbstractPlumberModel<T>> implements Serializable {

    // TODO: Add some generalized validation hook here with implementations in the subclasses.

    public AbstractPlumberModel() {
    }

    /**
     * Sets the given field to the given value.
     *
     * @param key Name of the field to set
     * @param val The field's value
     * @return this object with the field set
     */
    public T fieldVal(String key, Object val) {
        this."${key}" = val
        (T) this
    }

    /**
     * Adds the given value to the list at the given field.
     *
     * @param key Name of the field to add to.
     * @param val The new value
     * @return this object with the field set
     */
    public T addValToList(String key, Object val) {
        this."${key}" << val
        (T) this
    }

    /**
     * Sets the given field to the given resolved closure of the given class.
     *
     * @param key Name of the field to set
     * @param clazz The class we'll be resolving the closure to.
     * @param closure The closure in question
     * @return this object with the field set to the resolved closure.
     */
    public T closureVal(String key, Class clazz, Closure<?> closure) {
        this."${key}" = resolveClosure(clazz, closure)
        (T) this
    }

    /**
     * Adds the given resolved closure of the given class to the map at the given field using the given map key..
     *
     * @param key Name of the field to add to.
     * @param clazz The class we'll be resolving the closure to.
     * @param mapKey The key in the map we'll be using.
     * @param closure The closure in question
     * @return this object with the field updated.
     */
    public T addClosureValToMap(String key, Class clazz, String mapKey, Closure<?> closure) {
        this."${key}".put(mapKey, resolveClosure(clazz, closure))
        (T) this
    }

    /**
     * Adds the given resolved closure of the given class to the list at the given field.
     *
     * @param key Name of the field to add to.
     * @param clazz The class we'll be resolving the closure to.
     * @param closure The closure in question
     * @return this object with the field updated.
     */
    public T addClosureValToList(String key, Class clazz, Closure<?> closure) {
        this."${key}" << resolveClosure(clazz, closure)
        (T) this
    }

    /**
     * Takes a class and closure and resolves that closure into an instance of that class.
     *
     * @param clazz
     * @param closure
     * @return an instance of the class populated from the closure.
     */
    public Object resolveClosure(Class clazz, Closure<?> closure) {
        def tmpObject = clazz.newInstance()

        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.delegate = tmpObject
        closure.call()

        return tmpObject
    }

    /**
     * Transforms this node of the plumber model and everything below it into a simple form for later processing. 
     *
     * @return Map of the plumber model from this node downward in a simple name/args/closures form for each closure.
     */
    public Map toTree() {
        def tree = [:]

        tree.name = getClass().simpleName

        // Arguments that aren't nested closures.
        tree.args = [:]

        // Arguments that *are* nested closures, including MappedClosures.
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
                            tree.closures."${fieldName}" = ((Collection) fieldValue).collect { AbstractPlumberModel a ->
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
                // MappedClosures are handled a little special - just grab it as is for now.
                else if (fieldValue instanceof MappedClosure) {
                    tree.closures."${fieldName}" = ((MappedClosure) fieldValue).toTree()
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
