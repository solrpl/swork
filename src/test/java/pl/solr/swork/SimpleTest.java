package pl.solr.swork;

import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.Lists;

public class SimpleTest {

	@Test
	public void boot() {
		
		Workflow<BaseInputModel, BaseOutputModel, States> workflow = new Workflow<BaseInputModel, BaseOutputModel, States>();
		workflow.addStage(new MiddleStepA());
		workflow.addOutput(new BaseOutputStep());
		BaseOutputModel output = workflow.process(new BaseInputModel());
		assertNotNull(output);
		
	}
	
	@Test
	public void bootTheSameInputAndOutput() {
		
		Workflow<BaseInputModel, BaseInputModel, States> workflow = new Workflow<BaseInputModel, BaseInputModel, States>();
		workflow.addStage(new MiddleStepA());
		workflow.addOutput(new ShortCircuitOutputStage<BaseInputModel>());
		BaseInputModel output = workflow.process(new BaseInputModel());
		assertNotNull(output);
		
	}

	@Test
	public void bootMultipleSteps() {
		
		Workflow<BaseInputModel, BaseInputModel, States> workflow = new Workflow<BaseInputModel, BaseInputModel, States>();
		workflow.addListener(new SimpleWorkflowListener());
		workflow.addStage(new MiddleStepC());
		workflow.addStage(new MiddleStepA());
		workflow.addStage(new MiddleStepB());
		workflow.addStage(new MiddleStepD());
		workflow.addOutput(new ShortCircuitOutputStage<BaseInputModel>());
		BaseInputModel output = workflow.process(new BaseInputModel());
		assertNotNull(output);
		//TODO order verification by listener
		
	}

	@Test
	public void bootMultipleStepsWithExternal() {
		
		Workflow<BaseInputModel, BaseOutputModel , States> workflow = new Workflow<BaseInputModel, BaseOutputModel, States>();
		workflow.addListener(new SimpleWorkflowListener());
		workflow.addStage(new MiddleStepC());
		workflow.addStage(new MiddleStepA());
		workflow.addStage(new MiddleStepD());
		workflow.addOutput(new BaseOutputStep());
		BaseInputModel input = new BaseInputModel();
		Collection<States> states = workflow.proceed(input);
		//TODO verify state
		states = workflow.proceed(input, States.AFTER_B);
		//TODO verify state
		BaseOutputModel output = workflow.convert(input);
		assertNotNull(output);
		//TODO order verification by listener
		
	}
	
	public class SimpleWorkflowListener implements WorkflowListener<BaseInputModel, States> {

		public void processedStage(Stage<BaseInputModel, States> stage) {
			System.err.println(stage);
		}
		
	}
	
	public class BaseInputModel  {
		
	}
	
	public enum States {
		AFTER_A, AFTER_B, AFTER_C
	}
	
	public class MiddleStepA implements Stage<BaseInputModel, States> {

		public States[] consumes() {
			return new States[] {  };
		}
		
		public Collection<States> processStage(BaseInputModel input) {
			System.out.println("middleStepA executed");
			return Lists.newArrayList(States.AFTER_A);
		}
		
	}

	public class MiddleStepB implements Stage<BaseInputModel, States> {

		public States[] consumes() {
			return new States[] { States.AFTER_A };
		}
		
		public Collection<States> processStage(BaseInputModel input) {
			System.out.println("middleStepB executed");
			return Lists.newArrayList(States.AFTER_B);
		}
	
	}
	
	public class MiddleStepC implements Stage<BaseInputModel, States> {

		public States[] consumes() {
			return new States[] {  States.AFTER_A,  States.AFTER_B};
		}
		
		public Collection<States> processStage(BaseInputModel input) {
			System.out.println("middleStepC executed");
			return Lists.newArrayList(States.AFTER_C);
		}
	
	}

	public class MiddleStepD implements Stage<BaseInputModel, States> {

		public States[] consumes() {
			return new States[] { States.AFTER_B };
		}
		
		public Collection<States> processStage(BaseInputModel input) {
			System.out.println("middleStepD executed");
			return Lists.newArrayList();
		}
			
	}
	
	public class BaseOutputStep implements OutputStage<BaseInputModel, BaseOutputModel> {

		public BaseOutputModel process(BaseInputModel model) {
			return new BaseOutputModel();
		}
		
	}
	
	public class BaseOutputModel  {
		
	}
}
