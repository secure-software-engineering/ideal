package ideal.flowfunctions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import boomerang.accessgraph.AccessGraph;
import boomerang.accessgraph.IFieldGraph;
import boomerang.accessgraph.WrappedSootField;
import heros.solver.JoinHandlingNode;
import soot.Local;
import soot.SootField;
import soot.Type;
import soot.Unit;
import soot.Value;

public class WrappedAccessGraph implements JoinHandlingNode<WrappedAccessGraph>{

	private final AccessGraph delegate;
	private boolean hasEvent;
	public WrappedAccessGraph(AccessGraph g, boolean hasEvent){
		this.delegate = g;
		this.hasEvent =  hasEvent;
	}

	public WrappedAccessGraph(AccessGraph g){
		this(g, false);
	}
	public boolean baseMatches(Value arg) {
		return delegate.baseMatches(arg);
	}
	public Local getBase() {
		return delegate.getBase();
	}
	public WrappedAccessGraph deriveWithNewLocal(Local local, Type type) {
		return new WrappedAccessGraph(delegate.deriveWithNewLocal(local, type), hasEvent);
	}
	public Type getBaseType() {
		return delegate.getBaseType();
	}
	public IFieldGraph getFieldGraph() {
		return delegate.getFieldGraph();
	}

	public int getFieldCount() {
		return delegate.getFieldCount();
	}

	public AccessGraph getDelegate() {
		return delegate;
	}

	public boolean hasEvent() {
		return hasEvent;
	}

	public Collection<WrappedSootField> getLastField() {
		return delegate.getLastField();
	}

	public Set<WrappedAccessGraph> popLastField() {
		Set<WrappedAccessGraph> res = new HashSet<>();
		for(AccessGraph g : delegate.popLastField()){
			res.add(new WrappedAccessGraph(g,hasEvent));
		}
		return res;
	}

	public boolean isStatic() {
		return delegate.isStatic();
	}

	public boolean baseAndFirstFieldMatches(Value base, SootField field) {
		return delegate.baseAndFirstFieldMatches(base, field);
	}

	public Set<WrappedAccessGraph> popFirstField() {
		Set<WrappedAccessGraph> res = new HashSet<>();
		for(AccessGraph g : delegate.popFirstField()){
			res.add(new WrappedAccessGraph(g,hasEvent));
		}
		return res;
	}

	public WrappedAccessGraph prependField(WrappedSootField newFirstField) {
		return new WrappedAccessGraph(delegate.prependField(newFirstField),hasEvent);
	}

	public WrappedAccessGraph makeStatic() {
		return new WrappedAccessGraph(delegate.makeStatic(), hasEvent);
	}

	public Collection<WrappedSootField> getFirstField() {
		return delegate.getFirstField();
	}

	public boolean firstFieldMatches(SootField field) {
		return delegate.firstFieldMustMatch(field);
	}

	public WrappedAccessGraph deriveWithoutAllocationSite() {
		return new WrappedAccessGraph(delegate.deriveWithoutAllocationSite(),hasEvent);
	}

	public boolean hasAllocationSite() {
		return delegate.hasAllocationSite();
	}

	public Unit getSourceStmt() {
		return delegate.getSourceStmt();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
		result = prime * result + (hasEvent ? 1231 : 1237);
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
		WrappedAccessGraph other = (WrappedAccessGraph) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		if (hasEvent != other.hasEvent)
			return false;
		return true;
	}

	@Override
	public boolean handleJoin(WrappedAccessGraph joiningNode) {
		return false;
	}

	@Override
	public heros.solver.JoinHandlingNode.JoinKey createJoinKey() {
		return null;
	}

	@Override
	public void setCallingContext(WrappedAccessGraph callingContext) {
		this.hasEvent = callingContext.hasEvent || hasEvent;
	}

	public WrappedAccessGraph deriveWithoutEvent() {
		return new WrappedAccessGraph(delegate, false);
	}
	public WrappedAccessGraph deriveWithEvent() {
		return new WrappedAccessGraph(delegate, true);
	}

	public Collection<Type> getType() {
		return delegate.getType();
	}
	
	public String toString(){
		return delegate.toString() + " "+hasEvent;
	}
}
