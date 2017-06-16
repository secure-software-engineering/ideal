package ideal.pointsofaliasing;

import soot.Unit;

public abstract class Event<V> implements PointOfAlias<V>{
	abstract Unit getCallsite();
}
