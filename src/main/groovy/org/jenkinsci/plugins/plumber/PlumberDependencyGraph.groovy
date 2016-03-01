package org.jenkinsci.plugins.plumber

import org.jgrapht.DirectedGraph
import org.jgrapht.alg.CycleDetector
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph

import javax.annotation.Nonnull

/**
 * How this would be used:
 *  * The step would read the list of phases, and add them to the dependency graph.
 *  * Next, it would add the dependencies (either "before" or "after") for each phase, checking to see if addDependency
 *      returns false. If so, a cycle has been created and the step will fail out.
 *  * After adding the dependencies, the step would then construct the list of "phase groups" in execution order - i.e.,
 *      getNextPhases() will be called to find all phases which don't depend on anything else and are therefore eligible
 *      for execution.
 *  * After adding a "phase group" to the step's execution order, the step would pass the "phase group" to
 *      postPhaseProcessing(...) to remove the phases now in the execution order from the graph.
 *  * The above two would then be repeated until hasMorePhases() returns false, at which point we've gone through the
 *      full set of phases and none remain to be added to the execution order.
 *
 * TODO:
 *  * Maybe mash together getNextPhases() and postPhaseProcessing() - left them separate for now in case we decide to
 *      do something that would require keeping the graph state intact between finding the next phases and removing them.
 *  * Possibly add some logic for limiting the maximum number of concurrent phases in a single batch?
 */
public class PlumberDependencyGraph {
    private DirectedGraph phaseGraph

    public PlumberDependencyGraph() {
        phaseGraph = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class)
    }

    /**
     * Adds a new phase to the graph. Returns true if the phase has been added correctly, and false if it was already
     * present.
     *
     * @param phaseName
     * @return true if added, false if already present
     */
    public boolean addPhase(@Nonnull String phaseName) {
        return phaseGraph.addVertex(phaseName)
    }

    /**
     * Adds a dependency with the target phase depending on the source phase. Adds the phases if needed. Returns true if
     * able to add the dependency, false if adding this dependency would introduce a cycle to the dependency graph,
     * which is not allowed
     *
     * @param sourcePhase
     * @param targetPhase
     * @return true if added successfully or already present, false if adding the dependency would create a cycle
     */
    public boolean addDependency(@Nonnull String sourcePhase, @Nonnull String targetPhase) {
        if (!phaseGraph.containsVertex(sourcePhase)) {
            addPhase(sourcePhase)
        }
        if (!phaseGraph.containsVertex(targetPhase)) {
            addPhase(targetPhase)
        }

        // Add the dependency to the graph, and then check for cycles. If cycles are found, remove the edge.
        phaseGraph.addEdge(sourcePhase, targetPhase)

        CycleDetector<String, DefaultEdge> detector = new CycleDetector<String, DefaultEdge>(phaseGraph)

        if (detector.detectCycles()) {
            // TODO: Log/report the cycles somehow? May need to pass in a TaskListener here.
            phaseGraph.removeEdge(sourcePhase, targetPhase)
            return false
        } else {
            return true
        }
    }

    /**
     * Get the next set of phases to run. Can be empty if there are no phases left.
     *
     * @return The list of phases to run next
     */
    public List<String> getNextPhases() {
        // Return the phases to run - can be empty.
        return phaseGraph.findAll {
            phaseGraph.incomingEdgesOf(it).isEmpty()
        }
    }

    /**
     * Given a list of phases that have already been run, remove them from the graph.
     *
     * @param phases
     */
    public void postPhaseProcessing(List<String> phases) {
        phases.each { phaseGraph.removeVertex(it) }
    }

    /**
     * Returns true if there are more phases yet to be run/removed.
     *
     * @return true if there are still phases in the graph, false otherwise
     */
    public boolean hasMorePhases() {
        return !phaseGraph.vertexSet().isEmpty()
    }

}
