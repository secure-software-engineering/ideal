package ideal;

public interface ResultReporter<V> {
	public void onSeedFinished(FactAtStatement seed, AnalysisSolver<V> solver);
}
