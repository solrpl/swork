package pl.solr.swork;

public interface Stage<InputType, StateModel> {
	
	StateModel[] process(InputType input);

	StateModel[] supported();

}
