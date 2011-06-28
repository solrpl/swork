package pl.solr.swork;

public class EnrichWorkflow<Model, StateModel> extends Workflow<Model, Model, StateModel>{

	public EnrichWorkflow() {
		super();
		addOutputConverter(new ShortCircuitOutputStage<Model>());
	}
}
