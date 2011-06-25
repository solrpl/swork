package pl.solr.swork;

import java.util.Collection;

public interface Stage<InputType, StateModel> {
	
	Collection<StateModel> processStage(InputType input);

	StateModel[] consumes();

}
