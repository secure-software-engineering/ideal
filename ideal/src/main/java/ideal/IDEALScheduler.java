package ideal;

import heros.solver.Scheduler;

public class IDEALScheduler<V> extends Scheduler {

	protected int propagationCount;
	protected PerSeedAnalysisContext<V> context;
	public void setContext(PerSeedAnalysisContext<V> context){
		this.context = context;
	}
	@Override
	public void awaitExecution() {
		while (!worklist.isEmpty()) {
			Runnable pop = worklist.pop();
			if (propagationCount % 1000 == 0) {
				context.checkTimeout();
			}
			pop.run();
		}
	}
}
