package targets.file;

import org.junit.Test;

import ideal.Analysis;
import ideal.ResultReporter;
import test.IDEALTestingFramework;
import test.MustBe;
import typestate.TypestateDomainValue;
import typestate.impl.fileanalysis.FileMustBeClosedAnalysis;

public class FileMustBeClosedTests extends IDEALTestingFramework {
	@Test
	public void simple() {
		File file = new File();
		file.open();
		mustBeInErrorState(file);
		file.close();
		mustBeInAcceptingState(file);
	}

	@Test
	public void branching() {
		File file = new File();
		if (staticallyUnknown())
			file.open();
		mayBeInErrorState(file);
		file.close();
		mustBeInAcceptingState(file);
	}

	@Test
	public void aliasing() {
		File file = new File();
		File alias = file;
		if (staticallyUnknown())
			file.open();
		mayBeInErrorState(file);
		alias.close();
		mustBeInAcceptingState(file);
		mustBeInAcceptingState(alias);
	}

	@Test
	public void summaryTest() {
		File file1 = new File();
		call(file1);
		file1.close();
		mustBeInAcceptingState(file1);
		File file = new File();
		File alias = file;
		call(alias);
		file.close();
		mustBeInAcceptingState(file);
		mustBeInAcceptingState(alias);
	}

	private static void call(File alias) {
		alias.open();
	}

	@Test
	public void interprocedural() {
		File file = new File();
		file.open();
		flows(file, true);
		mayBeInAcceptingState(file);
		mayBeInErrorState(file);
	}

	private static void flows(File file, boolean b) {
		if (b)
			file.close();
	}

	private static class ObjectWithField {
		File field;
	}

	@Test
	public void flowViaField() {
		ObjectWithField container = new ObjectWithField();
		flows(container);
		if (staticallyUnknown())
			container.field.close();

		mayBeInErrorState(container.field);
	}

	private static void flows(ObjectWithField container) {
		container.field = new File();
		File field = container.field;
		field.open();
	}

	@Test
	public void nullPOATest() {
		ObjectWithField container = new ObjectWithField();
		flows(container);
		if (container.field != null)
			container.field.close();
		mustBeInAcceptingState(container.field);
	}

	@Test
	public void nullPOA2() {
		File file = null;
		if (staticallyUnknown())
			file = new File();

		file.open();
		if (file != null)
			file.close();
		mustBeInAcceptingState(file);
	}

	@Test
	public void indirectFlow() {
		ObjectWithField a = new ObjectWithField();
		ObjectWithField b = a;
		flows(a, b);
		mustBeInAcceptingState(a.field);
		mustBeInAcceptingState(b.field);
	}

	private void flows(ObjectWithField a, ObjectWithField b) {
		File file = new File();
		file.open();
		a.field = file;
		File alias = b.field;
		mustBeInErrorState(alias);
		alias.close();
	}

	@Override
	protected Analysis<TypestateDomainValue> createAnalysis(ResultReporter<TypestateDomainValue> reporter) {
		return new FileMustBeClosedAnalysis(reporter);
	}
}
