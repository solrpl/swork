package pl.solr.swork;

import java.util.Collection;

import com.google.common.collect.Lists;

public abstract class TrivialEnricher<Model> implements Enricher<Model, NullState> {

	public Collection<NullState> consumes() {
		return Lists.newArrayList();
	}
}
