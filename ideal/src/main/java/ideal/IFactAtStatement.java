package ideal;

import boomerang.accessgraph.AccessGraph;
import soot.Unit;

public interface IFactAtStatement {
	public AccessGraph getFact();

	public Unit getStmt();
}
