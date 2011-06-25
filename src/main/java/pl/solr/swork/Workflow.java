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

	/** listeners for worflow events. */
	private Collection<WorkflowListener<InputModel, StateModel>> listeners = Lists.newArrayList();
	
	/** execution strategy for stages. */
	private WorkflowPhaseExecutionStrategy<InputModel, StateModel> workflowPhaseExecutionStrategy = new SerialWorkflowPhaseExecutionStrategy<InputModel, StateModel>(); 
	
	/** logger. */
	private static final Logger LOG = LoggerFactory.getLogger(Workflow.class);
	
	
	public Collection<StateModel> proceed(final InputModel input) {
		int i = 1;
		while(true) {
			int executed = processStages(input);
			LOG.debug("Processed workflow pass: " + i + " " + state);
			i++;
			if (executed == 0) {
				break;
			}
		}
		return state.getState();
	}
	
	public Collection<StateModel> proceed(final InputModel input, final StateModel model) {
		state.add(model).commit();
		return proceed(input);
	}
	
	public OutputModel convert(final InputModel input) {
		if (waitingStages.size() != 0) {
			errorNotEmpty();
		}
		return processOutput(input);
	}

	
	/**
	 * Main method for processing argument by the workflow.
	 * @param input input argument
	 * @return object returned by workflow
	 */
	public OutputModel process(final InputModel input) {		
		if (outputStages.size() == 0) {
			throw new RuntimeException("Workflow should contain outputStages");
		}
		
		proceed(input);
		return convert(input);
	}
	
	private void errorNotEmpty() {
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
	
	public Workflow<InputModel, OutputModel, StateModel> addListener(WorkflowListener<InputModel, StateModel> listener) {
		this.listeners .add(listener);
		return this;
	}
	
	private int processStages(InputModel input) {
		Collection<Stage<InputModel, StateModel>> toExecute = Lists.newArrayList();
		for (Stage<InputModel, StateModel> s : waitingStages) {
			if (state.compatible(s.consumes())) {
				toExecute.add(s);
			}
		}
		state.addAll(workflowPhaseExecutionStrategy.execute(toExecute, input, listeners));
		executedStages.addAll(toExecute);
		waitingStages.removeAll(toExecute);
		state.commit(); //state cannot be changed without commit after loop
		return toExecute.size();
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
