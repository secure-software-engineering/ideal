package ideal;

import boomerang.BoomerangOptions;
import boomerang.accessgraph.AccessGraph;
import heros.solver.IPropagationController;
import soot.Unit;

public abstract class DefaultIDEALAnalysisDefinition<V> extends IDEALAnalysisDefinition<V> {
	@Override
	public BoomerangOptions boomerangOptions() {
		BoomerangOptions opts = new BoomerangOptions();
		opts.setQueryBudget(500);
		opts.setTrackStaticFields(Analysis.ALIASING_FOR_STATIC_FIELDS);
		return opts;
	}
	
	@Override
	public boolean enableAliasing() {
		return true;
	} 
	
	@Override
	public boolean enableNullPointOfAlias() {
		return false;
	}
	
	@Override
	public boolean enableStrongUpdates() {
		return true;
	}
	
	@Override
	public long analysisBudgetInSeconds() {
		return 30;
	}
	
	@Override
	public IDEALScheduler<V> getScheduler() {
		return new IDEALScheduler<>();
	}
	
	@Override
	public IPropagationController<Unit, AccessGraph> propagationController() {
		return new IPropagationController<Unit,AccessGraph>(){

			@Override
			public boolean continuePropagate(AccessGraph d1, Unit n, AccessGraph d2) {
				return true;
			}};
	}
}
