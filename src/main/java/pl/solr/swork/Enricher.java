package pl.solr.swork;

import java.util.Collection;

public interface Enricher<InputType, StateModel> {
	
	void validate(InputType input);
	
	Collection<StateModel> enrich(InputType input);

	Collection<StateModel> consumes();

}
