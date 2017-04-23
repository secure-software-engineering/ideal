package ideal;

import boomerang.BoomerangOptions;

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
}
