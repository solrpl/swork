package pl.solr.swork;

public interface OutputStage<InputModel, OutputModel> {

	OutputModel process(InputModel model);
}
