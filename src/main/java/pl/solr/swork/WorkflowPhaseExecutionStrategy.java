package pl.solr.swork;

import java.util.Collection;

/**
 * Execution strategy for workflow.
 * This class manages way of execution for stages selected for current loop.
 * 
 * @author Marek Rogoziński
 *
 * @param <InputModel> input type
 * @param <StateModel> workflow states
 */
public interface WorkflowPhaseExecutionStrategy<InputModel, StateModel> {

	Collection<StateModel> execute(final Collection<Enricher<InputModel, StateModel>> toExecute, 
									final InputModel input,
									final Collection<WorkflowListener<InputModel, StateModel>> listeners);
}
