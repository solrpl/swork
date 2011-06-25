package pl.solr.swork;

public interface Stage<InputType, StateModel> {
	
	StateModel[] processStage(InputType input);

	StateModel[] consumes();

}
