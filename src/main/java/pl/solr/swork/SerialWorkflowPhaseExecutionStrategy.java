package pl.solr.swork;

import java.util.Collection;

import com.google.common.collect.Lists;

/**
 * Serial execution of selected stages.
 * 
 * @author Marek Rogoziński
 *
 * @param <InputModel> input type
 * @param <StateModel> workflow states
 */
public class SerialWorkflowPhaseExecutionStrategy<InputModel, StateModel> implements
		WorkflowPhaseExecutionStrategy<InputModel, StateModel> {

	public Collection<StateModel> execute(final Collection<Enricher<InputModel, StateModel>> toExecute, 
			InputModel input,
			Collection<WorkflowListener<InputModel, StateModel>> listeners) {
		Collection<StateModel> states = Lists.newArrayList();
		for (Enricher<InputModel, StateModel> stage : toExecute) {
			states.addAll(stage.processStage(input));
			for (WorkflowListener<InputModel, StateModel> listener : listeners) {
				listener.processedStage(stage);
			}
		}
		return states;
		
	}

}
