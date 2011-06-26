package pl.solr.swork;

import java.util.Collection;

public interface Enricher<InputType, StateModel> {
	
	Collection<StateModel> enrich(InputType input);

	StateModel[] consumes();

}
