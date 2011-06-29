package pl.solr.swork;

import java.util.Collection;

/**
 * The main interface for classes that changed workflow input object.
 *
 * @author Marek Rogoziński
 *
 * @param <InputType> type of input object
 * @param <StateModel> workflow state model
 */
public interface Enricher<InputType, StateModel> {

	void validate(InputType input);

	Collection<StateModel> enrich(InputType input);

	Collection<StateModel> consumes();

}
