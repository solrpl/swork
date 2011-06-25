package pl.solr.swork;

//TODO listers implementation
public interface WorkflowListener<InputModel, StateModel> {
	
	void processedStage(Stage<InputModel, StateModel> stage);

}
