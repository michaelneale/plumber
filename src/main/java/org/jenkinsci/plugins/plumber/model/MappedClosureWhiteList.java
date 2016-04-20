/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.plumber.model;

import hudson.Extension;
import org.jenkinsci.plugins.scriptsecurity.sandbox.Whitelist;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Special case {@link Whitelist} to allow for invokeMethod to be run *solely* on {@link MappedClosure}s. That class
 * has a "methodMissing" override to turn all method calls into map puts.
 */
@Extension
public class MappedClosureWhiteList extends Whitelist {
    @Override
    public boolean permitsMethod(@Nonnull Method method, @Nonnull Object receiver, @Nonnull Object[] args) {
        if (method.getName().equals("invokeMethod") && MethodMissingWrapper.class.isAssignableFrom(receiver.getClass())) {
            return true;
        } else if (method.getName().equals("getProperty") && MethodMissingWrapper.class.isAssignableFrom(receiver.getClass())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean permitsConstructor(@Nonnull Constructor<?> constructor, @Nonnull Object[] args) {
        return false;
    }

    @Override
    public boolean permitsStaticMethod(@Nonnull Method method, @Nonnull Object[] args) {
        return false;
    }

    @Override
    public boolean permitsFieldGet(@Nonnull Field field, @Nonnull Object receiver) {
        return false;
    }

    @Override
    public boolean permitsFieldSet(@Nonnull Field field, @Nonnull Object receiver, @CheckForNull Object value) {
        return false;
    }

    @Override
    public boolean permitsStaticFieldGet(@Nonnull Field field) {
        return false;
    }

    @Override
    public boolean permitsStaticFieldSet(@Nonnull Field field, @CheckForNull Object value) {
        return false;
    }
}
