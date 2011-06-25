package pl.solr.swork;

import java.util.Collection;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class WorkflowState<StateModel> {

	private Collection<StateModel> current = Lists.newArrayList();
	
	/** maintains state for current loop, because state should not be changed during loop. */
	private Collection<StateModel> tempState = Lists.newArrayList();

	public boolean compatible(StateModel[] stateModels) {
		if (current.containsAll(Lists.newArrayList(stateModels))) {
			return true;
		}
		return false;
	}

	public WorkflowState<StateModel> addAll(Collection<StateModel> states) {
		tempState.addAll(Lists.newArrayList(states));
		return this;
	}

	public WorkflowState<StateModel> add(final StateModel state) {
		tempState.add(state);
		return this;
	}
	
	
	public void commit() {
		current.addAll(tempState);
		tempState.clear();		
	}
	
	public Collection<StateModel> getState() {
		return Lists.newArrayList(current);
	}
	
	@Override
	public String toString() {
		return "WorkflowState: [" + Joiner.on(",").join(current) + "]";
	}

}
