package pl.solr.swork;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.Lists;

public class SimpleTest {

	@Test
	public void boot() {

		Workflow<BaseInputModel, BaseOutputModel, States> workflow = new Workflow<BaseInputModel, BaseOutputModel, States>();
		workflow.addEnricher(new MiddleStepA());
		workflow.addOutputConverter(new BaseOutputStep());
		BaseOutputModel output = workflow.enrichAndConvert(new BaseInputModel());
		assertNotNull(output);

	}

	@Test
	public void bootTheSameInputAndOutput() {

		Workflow<BaseInputModel, BaseInputModel, States> workflow = new Workflow<BaseInputModel, BaseInputModel, States>();
		workflow.addEnricher(new MiddleStepA());
		workflow.addOutputConverter(new ShortCircuitOutputStage<BaseInputModel>());
		BaseInputModel output = workflow.enrichAndConvert(new BaseInputModel());
		assertNotNull(output);

	}

	@Test
	public void bootMultipleSteps() {

		Workflow<BaseInputModel, BaseInputModel, States> workflow = new Workflow<BaseInputModel, BaseInputModel, States>();
		workflow.addListener(new SimpleWorkflowListener());
		workflow.addEnricher(new MiddleStepC());
		workflow.addEnricher(new MiddleStepA());
		workflow.addEnricher(new MiddleStepB());
		workflow.addEnricher(new MiddleStepD());
		workflow.addOutputConverter(new ShortCircuitOutputStage<BaseInputModel>());
		BaseInputModel output = workflow.enrichAndConvert(new BaseInputModel());
		assertNotNull(output);
		//TODO order verification by listener

	}

	@Test
	public void bootMultipleStepsWithExternal() {

		Workflow<BaseInputModel, BaseOutputModel , States> workflow = new Workflow<BaseInputModel, BaseOutputModel, States>();
		workflow.addListener(new SimpleWorkflowListener());
		workflow.addEnricher(new MiddleStepC());
		workflow.addEnricher(new MiddleStepA());
		workflow.addEnricher(new MiddleStepD());
		workflow.addOutputConverter(new BaseOutputStep());
		BaseInputModel input = new BaseInputModel();
		Collection<States> states = workflow.enrich(input);
		//TODO verify state
		states = workflow.enrich(input, States.AFTER_B);
		//TODO verify state
		BaseOutputModel output = workflow.convert(input);
		assertNotNull(output);
		//TODO order verification by listener

	}

	@Test
	public void bootSubWorkflow() {

		Workflow<BaseInputModel, BaseInputModel, States> workflowA = new Workflow<BaseInputModel, BaseInputModel, States>();
		workflowA.addListener(new SimpleWorkflowListener());
		workflowA.addEnricher(new MiddleStepC());
		workflowA.addEnricher(new MiddleStepA());
		workflowA.addEnricher(new MiddleStepB());
		workflowA.addEnricher(new MiddleStepD());

		Workflow<BaseInputModel, BaseInputModel, States> workflowB = new Workflow<BaseInputModel, BaseInputModel, States>();
		workflowB.addListener(new SimpleWorkflowListener());
		workflowB.addEnricher(new MiddleStepC());
		workflowB.addEnricher(new MiddleStepA());
		workflowB.addEnricher(new MiddleStepB());
		workflowB.addEnricher(new MiddleStepD());

		workflowA.addOutputConverter(new ShortCircuitOutputStage<BaseInputModel>());
		workflowB.addOutputConverter(new ShortCircuitOutputStage<BaseInputModel>());

		workflowA.addEnricher(workflowB);
		BaseInputModel output = workflowA.enrichAndConvert(new BaseInputModel());
		assertNotNull(output);
		//TODO order verification by listener

	}


	public class SimpleWorkflowListener implements WorkflowListener<BaseInputModel, States> {

		public void processedStage(Enricher<BaseInputModel, States> stage) {
			System.err.println(stage);
		}

	}

	public class BaseInputModel  {

	}

	public enum States {
		AFTER_A, AFTER_B, AFTER_C
	}

	public class MiddleStep implements Enricher<BaseInputModel, States> {

		private final String name;
		private final Collection<States> consumed;
		private final Collection<States> generated;

		public MiddleStep(String name, Collection<States> consumed, Collection<States> generated) {
			this.name = name;
			this.consumed = consumed;
			this.generated = generated;

		}

		public Collection<States> enrich(BaseInputModel input) {
			System.out.println("middleStep executed: " + name);
			return Lists.newArrayList(generated);
		}

		public Collection<States> consumes() {
			return consumed;
		}

		public void validate(BaseInputModel input) throws EnrichException {

		}
	}

	public class MiddleStepA extends MiddleStep {

		public MiddleStepA() {
			super("A", new ArrayList<States>(), Lists.newArrayList(States.AFTER_A));
		}

	}

	public class MiddleStepB extends MiddleStep {

		public MiddleStepB() {
			super("B", Lists.newArrayList(States.AFTER_A), Lists.newArrayList(States.AFTER_B));
		}

	}

	public class MiddleStepC extends MiddleStep {

		public MiddleStepC() {
			super("C", Lists.newArrayList(States.AFTER_A,  States.AFTER_B), Lists.newArrayList(States.AFTER_C));
		}

	}

	public class MiddleStepD extends MiddleStep {

		public MiddleStepD() {
			super("D", Lists.newArrayList(States.AFTER_B), new ArrayList<States>());
		}

	}

	public class BaseOutputStep implements OutputConverter<BaseInputModel, BaseOutputModel> {

		public BaseOutputModel process(BaseInputModel model) {
			return new BaseOutputModel();
		}

	}

	public class BaseOutputModel  {

	}
}
