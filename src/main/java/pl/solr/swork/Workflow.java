package pl.solr.swork;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * The main entry class for workflow operations.
 * 
 * @author Marek Rogoziński
 *
 * @param <InputModel> class used as input information to the workflow
 * @param <OutputModel> class used as output information from the workflow
 * @param <StateModel> possible states for the workflow
 */
public class Workflow<InputModel, OutputModel, StateModel> {
	/** remaining stages in workflow. */
	private Collection<Stage<InputModel, StateModel>> waitingStages = Lists.newArrayList();
	
	/** processed stages in workflow. */
	private Collection<Stage<InputModel, StateModel>> executedStages = Lists.newArrayList();

	/** output processors for conversion from input model to output model. */
	private Collection<OutputStage<InputModel, OutputModel>> outputStages = Lists.newArrayList();
	
	/** current state for workflow. */
	private WorkflowState<StateModel> state = new WorkflowState<StateModel>();
	
	private static final Logger LOG = LoggerFactory.getLogger(Workflow.class); 
	
	/**
	 * Main method for processing argument by the workflow.
	 * @param input input argument
	 * @return object returned by workflow
	 */
	public OutputModel process(final InputModel input) {		
		if (outputStages.size() == 0) {
			throw new RuntimeException("Workflow should contain outputStages");
		}
		
		int i = 1;
		while(true) {
			int executed = processStages(input);
			LOG.debug("Processed workflow pass: " + i + " " + state);
			i++;
			if (waitingStages.size() == 0) {
				break;
			}
			if (executed == 0) {
				StringBuilder waitingList = new StringBuilder();
				for (Stage<InputModel, StateModel> stage : waitingStages) {
					waitingList
						.append(" * ")
						.append(stage.getClass().getName())
						.append(" [")
						.append(Joiner.on(",").join(stage.consumes()))
						.append("]\n");
				}
				LOG.error("Not every stage of workflow was executed.\n"
						+ "The following stages are waiting:\n" + waitingList);
				throw new RuntimeException("Not every stage executed.");				
			}
		}
		return processOutput(input);
	}
	
	public Workflow<InputModel, OutputModel, StateModel> addStage(final Stage<InputModel, StateModel> stage) {
		this.waitingStages.add(stage);
		return this;
	}
	
	public Workflow<InputModel, OutputModel, StateModel> addAllStages(final Collection<Stage<InputModel, StateModel>> stages) {
		this.waitingStages.addAll(stages);
		return this;
	}
	
	public Workflow<InputModel, OutputModel, StateModel> addOutput(OutputStage<InputModel, OutputModel> output) {
		this.outputStages.add(output);
		return this;
	}	
	
	private int processStages(InputModel input) {
		Collection<Stage<InputModel, StateModel>> executed = Lists.newArrayList();
		for (Stage<InputModel, StateModel> s : waitingStages) {
			if (state.compatible(s.consumes())) {
				state.add(s.process(input));
				executed.add(s);
			}
		}
		executedStages.addAll(executed);
		waitingStages.removeAll(executed);
		state.commit(); //state cannot be changed without commit after loop
		return executed.size();
	}

	private OutputModel processOutput(InputModel input) {
		for (OutputStage<InputModel, OutputModel> o : outputStages) {
			OutputModel model = o.process(input);
			if (model != null) {
				return model;
			}
		}
		throw new RuntimeException("Not output stage.");
	}


}
