package pl.solr.swork;

public interface OutputConverter<InputModel, OutputModel> {

	OutputModel process(InputModel model);
}
