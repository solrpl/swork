package pl.solr.swork.utils;

import pl.solr.swork.ValidatorException;

public final class Validators {

	public static <T> T checkNotNull(String name, T obj) {
		if (obj == null) {
			throw new ValidatorException(name);
		}
		return obj;
	}

}
