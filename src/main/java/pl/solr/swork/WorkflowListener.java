package pl.solr.swork;

//TODO listers implementation
public interface WorkflowListener<InputModel, StateModel> {
	
	void processedStage(Enricher<InputModel, StateModel> stage);

}
