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
public class Workflow<InputModel, OutputModel, StateModel> implements Enricher<InputModel, StateModel>{
	/** remaining enrichers in workflow. */
	private Collection<Enricher<InputModel, StateModel>> waitingEnrichers = Lists.newArrayList();

	/** processed enrichers in workflow. */
	private Collection<Enricher<InputModel, StateModel>> executedEnrichers = Lists.newArrayList();

	/** output processors for conversion from input model to output model. */
	private Collection<OutputConverter<InputModel, OutputModel>> outputConverters = Lists.newArrayList();

	/** current state for workflow. */
	private WorkflowState<StateModel> state = new WorkflowState<StateModel>();

	/** listeners for worflow events. */
	private Collection<WorkflowListener<InputModel, StateModel>> listeners = Lists.newArrayList();

	/** execution strategy for enrichers. */
	private WorkflowPhaseExecutionStrategy<InputModel, StateModel> workflowPhaseExecutionStrategy = new SerialWorkflowPhaseExecutionStrategy<InputModel, StateModel>(); 

	/** item of consumed states calculated from enclosed enrichers for using of workflow like enricher. */
	private Collection<StateModel> consumesList = Lists.newArrayList();

	/** logger. */
	private static final Logger LOG = LoggerFactory.getLogger(Workflow.class);


	public Collection<StateModel> consumes() {
		return Lists.newArrayList();
	}

	public void validate(InputModel input) throws EnrichException {

	}

	public Collection<StateModel> enrich(final InputModel input) {
		int i = 1;
		while (true) {
			int executed = processEnrichers(input);
			LOG.debug("Processed workflow pass: " + i + " " + state);
			i++;
			if (executed == 0) {
				break;
			}
		}
		return state.getState();
	}

	public Collection<StateModel> enrich(final InputModel input, final StateModel model) {
		state.add(model).commit();
		return enrich(input);
	}

	public OutputModel convert(final InputModel input) {
		if (waitingEnrichers.size() != 0) {
			errorNotEmpty();
		}
		return processOutputConverters(input);
	}


	/**
	 * Main method for processing argument by the workflow.
	 * @param input input argument
	 * @return object returned by workflow
	 */
	public OutputModel enrichAndConvert(final InputModel input) {
		if (outputConverters.size() == 0) {
			throw new RuntimeException("Workflow should contain outputConverters");
		}

		enrich(input);
		return convert(input);
	}
	
	private void errorNotEmpty() {
		StringBuilder waitingList = new StringBuilder();
		for (Enricher<InputModel, StateModel> enricher : waitingEnrichers) {
			waitingList
			.append(" * ")
			.append(enricher.getClass().getName())
			.append(" [")
			.append(Joiner.on(",").join(enricher.consumes()))
			.append("]\n");
		}
		LOG.error("Not every enricher of workflow was executed.\n"
				+ "The following enricher are waiting:\n" + waitingList);
		throw new RuntimeException("Not every enricher executed.");									
	}
	
	public Workflow<InputModel, OutputModel, StateModel> addEnricher(final Enricher<InputModel, StateModel> enricher) {
		this.consumesList.addAll(enricher.consumes());
		this.waitingEnrichers.add(enricher);
		return this;
	}
	
	public Workflow<InputModel, OutputModel, StateModel> addAllEnrichers(final Collection<Enricher<InputModel, StateModel>> enrichers) {
		for(Enricher<InputModel, StateModel> enricher : enrichers) {
			addEnricher(enricher);
		}
		return this;
	}
	
	public Workflow<InputModel, OutputModel, StateModel> addOutputConverter(OutputConverter<InputModel, OutputModel> output) {
		this.outputConverters.add(output);
		return this;
	}
	
	public Workflow<InputModel, OutputModel, StateModel> addListener(WorkflowListener<InputModel, StateModel> listener) {
		this.listeners .add(listener);
		return this;
	}
	
	private int processEnrichers(InputModel input) {
		Collection<Enricher<InputModel, StateModel>> toExecute = Lists.newArrayList();
		for (Enricher<InputModel, StateModel> s : waitingEnrichers) {
			if (state.compatible(s.consumes())) {
				toExecute.add(s);
			}
		}
		state.addAll(workflowPhaseExecutionStrategy.execute(toExecute, input, listeners));
		executedEnrichers.addAll(toExecute);
		waitingEnrichers.removeAll(toExecute);
		state.commit(); //state cannot be changed without commit after loop
		return toExecute.size();
	}

	private OutputModel processOutputConverters(InputModel input) {
		for (OutputConverter<InputModel, OutputModel> o : outputConverters) {
			OutputModel model = o.process(input);
			if (model != null) {
				return model;
			}
		}
		throw new RuntimeException("Not output converter.");
	}


}
