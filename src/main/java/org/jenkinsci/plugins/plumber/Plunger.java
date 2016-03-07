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
public abstract class Plunger implements ExtensionPoint {

    /**
     * The name of the plunger. Should be unique.
     * TODO: Figure out how to enforce uniqueness?
     * 
     * @return The name of the contributor.
     */
    public abstract @Nonnull
    String getName();

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
     * Returns all the registered {@link Plunger}s.
     */
    public static ExtensionList<Plunger> all() {
        return ExtensionList.lookup(Plunger.class);
    }

}
