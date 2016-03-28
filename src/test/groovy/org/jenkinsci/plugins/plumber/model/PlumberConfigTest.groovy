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

import org.junit.Test
import static org.junit.Assert.*;

class PlumberConfigTest {
    @Test
    public void testBasicConfig() {
        def config = new PlumberConfig()
        def c = {
            phase {
                name "foo"
                action {
                    script "echo hello"
                }
            }
        }

        config.fromClosure(c)
        def root = config.getConfig()

        assertTrue(root.phases.size() == 1)
    }

    @Test
    public void testPhaseOverrides() {
        def plumberConfig = new PlumberConfig()

        def c = {
            env "foo": "bar", "boo": "far"

            archiveDirs "one/dir", "second/dir"

            stashDirs "stash/one", "stash/two"

            notifications {
                config("email") {
                    to "some@one.com"
                }
                onSuccess true
            }

            treatUnstableAsSuccess true

            phase {
                name "overridePhase"

                env "foo": "banana", "pants": "trousers"

                archiveDirs "third/dir"

                treatUnstableAsSuccess false

                notifications {
                    config("email") {
                        to "someone@else.com"
                    }
                    onSuccess false
                }

                action {
                    script "echo hello"
                }
            }
        }

        plumberConfig.fromClosure(c)

        def root = plumberConfig.getConfig()

        def phase = root.phases.first()

        def overrides = phase.getOverrides(root)

        assertEquals(["third/dir"], overrides.archiveDirs)
        assertEquals(["stash/one", "stash/two"], overrides.stashDirs)
        assertEquals("banana", overrides.env?.foo)
        assertEquals("trousers", overrides.env?.pants)
        assertEquals("far", overrides.env?.boo)
        assertEquals(false, overrides.notifications?.onSuccess)
        assertEquals("someone@else.com", overrides.notifications?.configs?.email?.to)
        assertFalse(overrides.treatUnstableAsSuccess)
    }

    @Test
    public void testFunnel() {
        def config = new PlumberConfig()
        def c = {
            phase {
                name "foo"
                action {
                    funnel("simpleEcho") {
                        pants 'trousers'
                        shirts 'polos'
                    }
                }
            }
        }

        config.fromClosure(c)
        def root = config.getConfig()

        assertTrue(root.phases.size() == 1)
        def funnel = root.phases[0].action.funnel
        assertNotNull(funnel)
        assertNotNull(funnel.getMap())

        assertEquals("trousers", funnel.getMap().pants)
        assertEquals("polos", funnel.getMap().shirts)
        assertEquals("simpleEcho", funnel.getMap().name)
    }

    @Test
    public void testMapConfig() {
        def plumberConfig = new PlumberConfig()

        def m = [
                env:["foo": "bar", "boo": "far"],

                archiveDirs: ["one/dir", "second/dir"],

                stashDirs: ["stash/one", "stash/two"],

                notifications: [
                        configs:[ email: [
                                to: "some@one.com"
                        ]],
                        onSuccess: true
                ],

                treatUnstableAsSuccess: true,

                phases: [
                        [name: "overridePhase",
                         env: ["foo": "banana", "pants": "trousers"],
                         archiveDirs: "third/dir",
                         treatUnstableAsSuccess: false,
                         notifications: [
                                 configs: [email: [
                                         to: "someone@else.com"
                                 ]],
                                 onSuccess: false
                         ],

                         action: [
                                 script: "echo hello"
                         ]
                        ]
                ]
        ]

        plumberConfig.fromMap(m)

        def root = plumberConfig.getConfig()

        def phase = root.phases.first()

        def overrides = phase.getOverrides(root)

        assertEquals(["third/dir"], overrides.archiveDirs)
        assertEquals(["stash/one", "stash/two"], overrides.stashDirs)
        assertEquals("banana", overrides.env?.foo)
        assertEquals("trousers", overrides.env?.pants)
        assertEquals("far", overrides.env?.boo)
        assertEquals(false, overrides.notifications?.onSuccess)
        assertEquals("someone@else.com", overrides.notifications?.configs?.email?.to)
        assertFalse(overrides.treatUnstableAsSuccess)
    }

}
