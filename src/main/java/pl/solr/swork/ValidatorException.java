package pl.solr.swork;

public class ValidatorException extends RuntimeException {

	private String name;

	public ValidatorException(final String name) {
		this.name = name;
	}

	public final String getName() {
		return name;
	}
}
