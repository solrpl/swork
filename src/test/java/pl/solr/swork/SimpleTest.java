package pl.solr.swork;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class SimpleTest {

	@Test
	public void boot() {

		Workflow<BaseInputModel, BaseOutputModel, States> workflow = new Workflow<BaseInputModel, BaseOutputModel, States>();
		SimpleWorkflowListener listener = new SimpleWorkflowListener();
		MiddleStep a = new MiddleStepA();
		workflow.addListener(listener);
		workflow.addEnricher(a);
		workflow.addOutputConverter(new BaseOutputStep());
		BaseOutputModel output = workflow.enrichAndConvert(new BaseInputModel());
		assertNotNull(output);
		listener.assertCalled(Lists.newArrayList(a));
	}

	@Test
	public void bootTheSameInputAndOutput() {

		Workflow<BaseInputModel, BaseInputModel, States> workflow = new Workflow<BaseInputModel, BaseInputModel, States>();
		MiddleStep a = new MiddleStepA();
		SimpleWorkflowListener listener = new SimpleWorkflowListener();
		workflow.addListener(listener);
		workflow.addEnricher(a);
		workflow.addOutputConverter(new ShortCircuitOutputStage<BaseInputModel>());
		BaseInputModel output = workflow.enrichAndConvert(new BaseInputModel());
		assertNotNull(output);
		listener.assertCalled(Lists.newArrayList(a));

	}

	@Test
	public void bootMultipleSteps() {

		Workflow<BaseInputModel, BaseInputModel, States> workflow = new Workflow<BaseInputModel, BaseInputModel, States>();
		SimpleWorkflowListener listener = new SimpleWorkflowListener();
		MiddleStep a = new MiddleStepA();
		MiddleStep b = new MiddleStepB();
		MiddleStep c = new MiddleStepC();
		MiddleStep d = new MiddleStepD();

		workflow.addListener(listener);
		workflow.addEnricher(c);
		workflow.addEnricher(a);
		workflow.addEnricher(b);
		workflow.addEnricher(d);
		workflow.addOutputConverter(new ShortCircuitOutputStage<BaseInputModel>());
		BaseInputModel output = workflow.enrichAndConvert(new BaseInputModel());
		assertNotNull(output);
		listener.assertCalled(Lists.newArrayList(a, b, c, d));

	}

	@Test
	public void bootMultipleStepsWithExternal() {

		Workflow<BaseInputModel, BaseOutputModel , States> workflow = new Workflow<BaseInputModel, BaseOutputModel, States>();
		SimpleWorkflowListener listener = new SimpleWorkflowListener();
		MiddleStep a = new MiddleStepA();
		MiddleStep c = new MiddleStepC();
		MiddleStep d = new MiddleStepD();

		workflow.addListener(listener);
		workflow.addEnricher(c);
		workflow.addEnricher(a);
		workflow.addEnricher(d);
		workflow.addOutputConverter(new BaseOutputStep());
		BaseInputModel input = new BaseInputModel();
		Collection<States> states = workflow.enrich(input);
		assertArrayEquals(new States[]{States.AFTER_A}, states.toArray());
		listener.assertCalled(Lists.newArrayList(a));

		states = workflow.enrich(input, States.AFTER_B);
		assertEquals(3, states.size());

		listener.assertCalled(Lists.newArrayList(c, d));
		BaseOutputModel output = workflow.convert(input);
		assertNotNull(output);
	}

	@Test
	public void bootSubWorkflow() {
		SimpleWorkflowListener listenerA = new SimpleWorkflowListener();
		SimpleWorkflowListener listenerB = new SimpleWorkflowListener();
		MiddleStep a = new MiddleStepA();
		MiddleStep b = new MiddleStepB();
		MiddleStep c = new MiddleStepC();
		MiddleStep d = new MiddleStepD();

		Workflow<BaseInputModel, BaseInputModel, States> workflowA = new Workflow<BaseInputModel, BaseInputModel, States>();

		workflowA.addListener(listenerA);

		workflowA.addEnricher(c);
		workflowA.addEnricher(a);
		workflowA.addEnricher(b);
		workflowA.addEnricher(d);

		Workflow<BaseInputModel, BaseInputModel, States> workflowB = new Workflow<BaseInputModel, BaseInputModel, States>();
		workflowB.addListener(listenerB);
		workflowB.addEnricher(c);
		workflowB.addEnricher(a);
		workflowB.addEnricher(b);
		workflowB.addEnricher(d);

		workflowA.addOutputConverter(new ShortCircuitOutputStage<BaseInputModel>());
		workflowB.addOutputConverter(new ShortCircuitOutputStage<BaseInputModel>());

		workflowA.addEnricher(workflowB);
		BaseInputModel output = workflowA.enrichAndConvert(new BaseInputModel());
		assertNotNull(output);
		listenerA.assertCalled(Lists.newArrayList(a, workflowB, c, b, d));
		listenerB.assertCalled(Lists.newArrayList(a, b, c, d));

	}


	public class SimpleWorkflowListener implements WorkflowListener<BaseInputModel, States> {
		private List<Enricher<BaseInputModel, States>> called = Lists.newArrayList();
		
		public void processedEnricher(Enricher<BaseInputModel, States> stage) {
			called.add(stage);
			System.err.println(stage);
		}

		public void assertCalled(List<? extends Enricher<BaseInputModel, States>> list) {
			assertArrayEquals(list.toArray(), called.toArray());
			called.clear();
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
