package pl.solr.swork;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SimpleTest {

	@Test
	public void boot() {
		
		Workflow<BaseInputModel, BaseOutputModel, States> workflow = new Workflow<BaseInputModel, BaseOutputModel, States>();
		workflow.addStage(new MiddleStep());
		workflow.addOutput(new BaseOutputStep());
		BaseOutputModel output = workflow.process(new BaseInputModel());
		assertNotNull(output);
		
	}
	
	public void bootTheSameInputAndOutput() {
		
		Workflow<BaseInputModel, BaseInputModel, States> workflow = new Workflow<BaseInputModel, BaseInputModel, States>();
		workflow.addStage(new MiddleStep());
		workflow.addOutput(new ShortCircuitOutputStage<BaseInputModel>());
		BaseInputModel output = workflow.process(new BaseInputModel());
		assertNotNull(output);
		
	}
	
	public class BaseInputModel  {
		
	}
	
	public enum States {
		START
	}
	
	public class MiddleStep implements Stage<BaseInputModel, States> {

		public States[] process(BaseInputModel input) {
			System.out.println("middleStep executed");
			return new States[] { };
		}

		public States[] supported() {
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
