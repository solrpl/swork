package pl.solr.swork;

public class ShortCircuitOutputStage<InputModel> implements
		OutputStage<InputModel, InputModel> {

	public InputModel process(final InputModel model) {
		return model;
	}

}
