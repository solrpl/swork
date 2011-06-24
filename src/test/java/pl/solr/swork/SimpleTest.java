package pl.solr.swork;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

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
		workflow.addStage(new MiddleStepC());
		workflow.addStage(new MiddleStepA());
		workflow.addStage(new MiddleStepB());
		workflow.addStage(new MiddleStepD());
		workflow.addOutput(new ShortCircuitOutputStage<BaseInputModel>());
		BaseInputModel output = workflow.process(new BaseInputModel());
		assertNotNull(output);
		
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
		
		public States[] process(BaseInputModel input) {
			System.out.println("middleStepA executed");
			return new States[] { States.AFTER_A };
		}
		
	}

	public class MiddleStepB implements Stage<BaseInputModel, States> {

		public States[] consumes() {
			return new States[] { States.AFTER_A };
		}
		
		public States[] process(BaseInputModel input) {
			System.out.println("middleStepB executed");
			return new States[] { States.AFTER_B };
		}
	
	}
	
	public class MiddleStepC implements Stage<BaseInputModel, States> {

		public States[] consumes() {
			return new States[] {  States.AFTER_A,  States.AFTER_B};
		}
		
		public States[] process(BaseInputModel input) {
			System.out.println("middleStepC executed");
			return new States[] {  States.AFTER_C };
		}
	
	}

	public class MiddleStepD implements Stage<BaseInputModel, States> {

		public States[] consumes() {
			return new States[] { States.AFTER_B};
		}
		
		public States[] process(BaseInputModel input) {
			System.out.println("middleStepD executed");
			return new States[] { };
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
