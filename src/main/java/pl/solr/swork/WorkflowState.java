package pl.solr.swork;

import java.util.Collection;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class WorkflowState<StateModel> {

	private Set<StateModel> current = Sets.newHashSet();
	
	/** maintains state for current loop, because state should not be changed during loop. */
	private Set<StateModel> tempState = Sets.newHashSet();

	public boolean compatible(StateModel[] stateModels) {
		if (stateModels == null || current.containsAll(Lists.newArrayList(stateModels))) {
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
