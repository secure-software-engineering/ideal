package ideal;

public interface ResultReporter<V> {
	public void onSeedFinished(IFactAtStatement seed, AnalysisSolver<V> solver);

	public void onSeedTimeout(IFactAtStatement seed);
}
