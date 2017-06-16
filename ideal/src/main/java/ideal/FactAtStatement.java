package ideal;

import boomerang.accessgraph.AccessGraph;
import soot.Unit;
import soot.jimple.Stmt;

public class FactAtStatement implements IFactAtStatement {

	private AccessGraph fact;
	private Unit u;

	public FactAtStatement(Unit u, AccessGraph fact) {
		this.u = u;
		this.fact = fact;
	}

	public AccessGraph getFact() {
		return fact;
	}

	public Unit getStmt() {
		return u;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fact == null) ? 0 : fact.hashCode());
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FactAtStatement other = (FactAtStatement) obj;
		if (fact == null) {
			if (other.fact != null)
				return false;
		} else if (!fact.equals(other.fact))
			return false;
		if (u == null) {
			if (other.u != null)
				return false;
		} else if (!u.equals(other.u))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return fact + " @ " + u;
	}
}
