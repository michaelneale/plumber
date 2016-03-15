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

import org.jenkinsci.plugins.plumber.PlumberDependencyGraph
import org.junit.Test
import static org.junit.Assert.*


class PlumberDependencyGraphTest {
    @Test
    public void testFromPhaseListLinear() {
        def plumberConfig = new PlumberConfig()
        plumberConfig.fromClosure {
            phase {
                name "first"
                action {
                    script "echo first"
                }
            }
            phase {
                name "second"
                action {
                    script "echo second"
                }
                after "first"
            }
        }

        def root = plumberConfig.getConfig()

        def graph = PlumberDependencyGraph.fromPhaseList(root.phases)

        assertTrue(graph.hasMorePhases())

        def exSets = root.executionSets()

        assertTrue(exSets.size() == 2)

    }

    @Test
    public void testFromPhaseListSingle() {
        def plumberConfig = new PlumberConfig()
        plumberConfig.fromClosure {
            phase {
                name "first"
                action {
                    script "echo first"
                }
            }
        }

        def root = plumberConfig.getConfig()

        def graph = PlumberDependencyGraph.fromPhaseList(root.phases)

        assertTrue(graph.hasMorePhases())

        def exSets = root.executionSets()

        assertTrue(exSets.size() == 1)

    }

    @Test
    public void testFromPhaseListBranchingAndIndependent() {
        def plumberConfig = new PlumberConfig()
        plumberConfig.fromClosure {
            phase {
                name "first"
                action {
                    script "echo first"
                }
            }
            phase {
                name "second"
                action {
                    script "echo second"
                }
            }
            phase {
                name "third"
                action {
                    script "echo third"
                }
                after "first"
            }
            phase {
                name "fourth"
                action {
                    script "echo fourth"
                }
                after "first"
            }
        }

        def root = plumberConfig.getConfig()

        def graph = PlumberDependencyGraph.fromPhaseList(root.phases)

        assertTrue(graph.hasMorePhases())

        def exSets = root.executionSets()

        assertEquals(2, exSets.size())
        assertEquals(2, exSets[0].phases.size())
        assertEquals(2, exSets[1].phases.size())
        assertTrue(exSets[0].phases.find { it.name == "first"} != null)
        assertTrue(exSets[1].phases.find { it.name == "third"} != null)
        assertEquals("first+second", exSets[0].stageName)
        assertEquals("third+fourth", exSets[1].stageName)
    }

    @Test
    public void testFromPhaseListBranchingAndJoining() {
        def plumberConfig = new PlumberConfig()
        plumberConfig.fromClosure {
            phase {
                name "first"
                action {
                    script "echo first"
                }
            }
            phase {
                name "second"
                action {
                    script "echo second"
                }
                after "first"
            }
            phase {
                name "third"
                action {
                    script "echo third"
                }
                after "first", "second"
            }
            phase {
                name "fourth"
                action {
                    script "echo fourth"
                }
                after "second", "third"
            }
        }

        def root = plumberConfig.getConfig()

        def graph = PlumberDependencyGraph.fromPhaseList(root.phases)

        assertTrue(graph.hasMorePhases())

        def exSets = root.executionSets()

        assertEquals(4, exSets.size())
        assertEquals(1, exSets[0].phases.size())
        assertEquals(1, exSets[1].phases.size())
        assertEquals(1, exSets[2].phases.size())
        assertEquals(1, exSets[3].phases.size())
        assertTrue(exSets[0].phases.find { it.name == "first"} != null)
        assertTrue(exSets[1].phases.find { it.name == "second"} != null)
        assertTrue(exSets[2].phases.find { it.name == "third"} != null)
        assertTrue(exSets[3].phases.find { it.name == "fourth"} != null)
        assertEquals("first", exSets[0].stageName)
        assertEquals("second", exSets[1].stageName)
        assertEquals("third", exSets[2].stageName)
        assertEquals("fourth", exSets[3].stageName)
    }
}
