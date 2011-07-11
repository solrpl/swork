package pl.solr.swork;

//TODO listers implementation
public interface WorkflowListener<InputModel, StateModel> {

	void processedEnricher(Enricher<InputModel, StateModel> stage);

}
