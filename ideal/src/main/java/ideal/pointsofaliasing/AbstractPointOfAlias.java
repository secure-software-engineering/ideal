package ideal.pointsofaliasing;

import java.util.Collection;

import boomerang.accessgraph.AccessGraph;
import heros.solver.PathEdge;
import ideal.PerSeedAnalysisContext;
import soot.Unit;

public abstract class AbstractPointOfAlias<V> implements PointOfAlias<V>{
  protected AccessGraph d2;
  protected Unit curr;
  protected Unit succ;
  protected AccessGraph d1;

  public AbstractPointOfAlias(AccessGraph d1, Unit stmt, AccessGraph d2, Unit succ) {
    this.d1 = d1;
    this.curr = stmt;
    this.d2 = d2;
    this.succ = succ;
  }


  public String toString() {
    return "<" + d1.toString() + ">-<" + curr.toString() + "," + d2 + ">";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((curr == null) ? 0 : curr.hashCode());
    result = prime * result + ((d1 == null) ? 0 : d1.hashCode());
    result = prime * result + ((d2 == null) ? 0 : d2.hashCode());
    result = prime * result + ((succ == null) ? 0 : succ.hashCode());
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
    @SuppressWarnings("rawtypes")
    AbstractPointOfAlias other = (AbstractPointOfAlias) obj;
    if (curr == null) {
      if (other.curr != null)
        return false;
    } else if (!curr.equals(other.curr))
      return false;
    if (d1 == null) {
      if (other.d1 != null)
        return false;
    } else if (!d1.equals(other.d1))
      return false;
    if (d2 == null) {
      if (other.d2 != null)
        return false;
    } else if (!d2.equals(other.d2))
      return false;
    if (succ == null) {
      if (other.succ != null)
        return false;
    } else if (!succ.equals(other.succ))
      return false;
    return true;
  };

}
