package pl.solr.swork;

import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.Lists;

public class TrivialWorkflowTest {

	@Test
	public void trivialWorkflow() {
		TrivialWorkflow<SimpleModel> workflow = new TrivialWorkflow<SimpleModel>();
		workflow.addEnricher(new EnrichB());
		workflow.addEnricher(new EnrichA());
		workflow.enrichAndConvert(new SimpleModel());
	}

	public class SimpleModel {

	}

	public class EnrichA extends TrivialEnricher<SimpleModel> {

		public Collection<NullState> enrich(SimpleModel input) {
			System.out.println("EnrichA.enrich()");
			return Lists.newArrayList();
		}

		public void validate(SimpleModel input) throws EnrichException {
		}

	}

	public class EnrichB extends TrivialEnricher<SimpleModel> {

		public Collection<NullState> enrich(SimpleModel input) {
			System.out.println("EnrichB.enrich()");
			return Lists.newArrayList();
		}

		public void validate(SimpleModel input) throws EnrichException {
		}

	}
}
