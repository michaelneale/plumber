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
package org.jenkinsci.plugins.plumber;

import groovy.lang.GroovyCodeSource;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

import javax.annotation.Nonnull;
import java.net.URL;

/**
 * {@link ExtensionPoint} for contributing Plumber scripts. Looks for relevant script in classpath and provides the
 * {@link GroovyCodeSource} for the script.
 *
 * Down the road, we'll be adding validation (probably elsewhere in the plugin, after parsing the script and having
 * something we can inspect to be sure it doesn't do things that are illegal in a Plumber context like node blocks,
 * stages, input...) and the logic to actually parse and use the scripts, but the intent is that Plumber scripts will be
 * a strict subset of valid {@link org.jenkinsci.plugins.workflow.cps.global.UserDefinedGlobalVariable}-style scripts,
 * which can be used in both forms.
 */
public abstract class PlumberContributor implements ExtensionPoint {

    /**
     * The name of the contributor. Should be unique.
     * TODO: Figure out how to enforce uniqueness?
     * 
     * @return The name of the contributor.
     */
    public abstract @Nonnull String getName();

    /**
     * Get the {@link GroovyCodeSource} for this contributor. Throws an {@link IllegalStateException} if the script
     * can't be loaded.
     * TODO: Probably figure out how to cache this so we don't have to load it every time.
     * TODO: Validation that the script is a valid candidate for Plumber contribution - that may be in the parsing tho.
     * TODO: Actual parsing - elsewhere, in a CPS context so that we can actually load it right.
     *
     * @return {@link GroovyCodeSource} for the contributor.
     * @throws Exception
     */
    public GroovyCodeSource getScript() throws Exception {
        // Expect that the script will be at package/name/className/contributorName.groovy
        URL scriptUrl = getClass().getClassLoader().getResource(getClass().getName().replace('$', '/').replace('.', '/')
                + '/' + getName() + ".groovy");

        try {
            GroovyCodeSource gsc = new GroovyCodeSource(scriptUrl);
            gsc.setCachable(true);

            return gsc;
        } catch (RuntimeException e) {
            // Probably could be a better error message...
            throw new IllegalStateException("Could not open script source.");
        }
    }

    /**
     * Returns all the registered {@link PlumberContributor}s.
     */
    public static ExtensionList<PlumberContributor> all() {
        return ExtensionList.lookup(PlumberContributor.class);
    }

}
