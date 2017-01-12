package ideal.flowfunctions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import boomerang.AliasFinder;
import boomerang.accessgraph.WrappedSootField;
import boomerang.cache.AliasResults;
import boomerang.forward.AbstractFlowFunctions;
import heros.EdgeFunction;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.edgefunc.EdgeIdentity;
import heros.flowfunc.Identity;
import ideal.AnalysisContext;
import ideal.pointsofaliasing.CallSite;
import ideal.pointsofaliasing.InstanceFieldWrite;
import ideal.pointsofaliasing.NullnessCheck;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.Constant;
import soot.jimple.EqExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

/**
 * This class defines the flow function for IDEAL. 
 *
 */
public class ForwardFlowFunctions<V> extends AbstractFlowFunctions
		implements FlowFunctions<Unit, WrappedAccessGraph, SootMethod> {
	private AnalysisContext<V> context;
	public ForwardFlowFunctions(AnalysisContext<V> context) {
		this.context = context;
	}

	@Override
	public FlowFunction<WrappedAccessGraph> getNormalFlowFunction(final WrappedAccessGraph sourceFact, final Unit curr,
			final Unit succ) {
		return new FlowFunction<WrappedAccessGraph>() {

			@Override
			public Set<WrappedAccessGraph> computeTargets(WrappedAccessGraph source) {
				context.debugger.onNormalPropagation(sourceFact, curr, succ, source);
				if (curr instanceof IdentityStmt) {
					IdentityStmt identityStmt = (IdentityStmt) curr;
					if (identityStmt.getRightOp() instanceof CaughtExceptionRef
							&& identityStmt.getLeftOp() instanceof Local) {
						Local leftOp = (Local) identityStmt.getLeftOp();
						// e = d;
						if (!source.isStatic() && typeCompatible(((Local) leftOp).getType(), source.getBaseType())) {
							HashSet<WrappedAccessGraph> out = new HashSet<WrappedAccessGraph>();
							out.add(source);
							out.add(source.deriveWithNewLocal((Local) leftOp, source.getBaseType()));
							return out;
						}
					}
				}
				if (!(curr instanceof AssignStmt)) {
					if (curr instanceof IfStmt) {
						IfStmt ifStmt = (IfStmt) curr;
						Value condition = ifStmt.getCondition();

						if (condition instanceof EqExpr && source.getFieldCount() == 0) {
							EqExpr eqExpr = (EqExpr) condition;
							Value leftOp = eqExpr.getOp1();
							Value rightOp = eqExpr.getOp2();
							boolean nullnessCheck = false;
							if (rightOp instanceof NullConstant && leftOp.equals(source.getBase())) {
								nullnessCheck = true;
							} else if (leftOp instanceof NullConstant && rightOp.equals(source.getBase())) {
								nullnessCheck = true;
							}
							if (nullnessCheck) {
								context.addPOA(new NullnessCheck<V>(sourceFact, curr, source, ifStmt.getTarget()));
							}
						}

					}
					return Collections.singleton(source);
				}

				AssignStmt as = (AssignStmt) curr;
				Value leftOp = as.getLeftOp();
				Value rightOp = as.getRightOp();

				HashSet<WrappedAccessGraph> out = new HashSet<WrappedAccessGraph>();
				out.add(source);

				if (rightOp instanceof Constant || rightOp instanceof NewExpr) {
					// a = new || a = 2
					if (leftOp instanceof Local && source.baseMatches(leftOp))
						// source == a.*
						return Collections.emptySet();
					// a.f = new || a.f = 2;
					if (leftOp instanceof InstanceFieldRef) {
						InstanceFieldRef fr = (InstanceFieldRef) leftOp;
						Value base = fr.getBase();
						SootField field = fr.getField();
						// source == a.f.*
						if (source.baseAndFirstFieldMatches(base, field))
							return Collections.emptySet();
					}

				}

				if (leftOp instanceof Local) {
					if (source.baseMatches(leftOp)) {
						if (rightOp instanceof InstanceFieldRef) {
							InstanceFieldRef fr = (InstanceFieldRef) rightOp;
							Value base = fr.getBase();
							SootField field = fr.getField();

							if (source.baseAndFirstFieldMatches(base, field)) {
								Set<WrappedAccessGraph> popFirstField = source.popFirstField();
								out.addAll(popFirstField);
							} else {
								return Collections.emptySet();
							}
						} else {
							return Collections.emptySet();
						}
					}
				} else if (leftOp instanceof InstanceFieldRef) {
					InstanceFieldRef fr = (InstanceFieldRef) leftOp;
					Value base = fr.getBase();
					SootField field = fr.getField();
					if (source.baseAndFirstFieldMatches(base, field)) {
						return Collections.emptySet();
					}
				}
				if (rightOp instanceof CastExpr) {
					CastExpr castExpr = (CastExpr) rightOp;
					Value op = castExpr.getOp();
					if (op instanceof Local) {
						if (!source.isStatic() && source.baseMatches(op)
								&& typeCompatible(castExpr.getCastType(), source.getBaseType())) {
							Type newType = (Scene.v().getFastHierarchy().canStoreType(castExpr.getCastType(),
									source.getBaseType()) ? castExpr.getCastType() : source.getBaseType());
							out.add(source.deriveWithNewLocal((Local) leftOp, newType));
						}
					}
				}

				if (rightOp instanceof Local && source.baseMatches(rightOp)) {

					if (leftOp instanceof Local) {
						// e = d;
						if (typeCompatible(((Local) leftOp).getType(), source.getBaseType())) {
							out.add(source.deriveWithNewLocal((Local) leftOp, source.getBaseType()));
						}
					} else if (leftOp instanceof InstanceFieldRef) {
						// d.f = e;
						InstanceFieldRef fr = (InstanceFieldRef) leftOp;
						Value base = fr.getBase();
						SootField field = fr.getField();

						if (base instanceof Local) {
							Local lBase = (Local) base;

							WrappedAccessGraph withNewLocal = source.deriveWithNewLocal(lBase, lBase.getType());
							WrappedSootField newFirstField = new WrappedSootField(field, source.getBaseType(), curr);
							if (AliasResults.canPrepend(withNewLocal.getDelegate(), newFirstField)) {
								WrappedAccessGraph newAp = withNewLocal.prependField(newFirstField);
								out.add(newAp);
								InstanceFieldWrite<V> instanceFieldWrite = new InstanceFieldWrite<>(sourceFact, as,
										lBase, newAp, succ);
								if (context.isInIDEPhase()) {
									out.addAll(context.instanceFieldWriteFlows(instanceFieldWrite));
								} else {
									context.addPOA(instanceFieldWrite);
								}
							}
						}
					} else if (leftOp instanceof ArrayRef) {
						ArrayRef fr = (ArrayRef) leftOp;
						Value base = fr.getBase();

						if (base instanceof Local) {
							Local lBase = (Local) base;

							WrappedAccessGraph withNewLocal = source.deriveWithNewLocal(lBase, lBase.getType());
							WrappedAccessGraph newAp = withNewLocal.prependField(
									new WrappedSootField(AliasFinder.ARRAY_FIELD, source.getBaseType(), curr));
							out.add(newAp);
							InstanceFieldWrite<V> instanceFieldWrite = new InstanceFieldWrite<>(sourceFact, as, lBase,
									newAp, succ);
							if (context.isInIDEPhase()) {
								out.addAll(context.instanceFieldWriteFlows(instanceFieldWrite));
							} else {
								context.addPOA(instanceFieldWrite);
							}
						}
					} else if (leftOp instanceof StaticFieldRef && AliasFinder.ENABLE_STATIC_FIELDS) {
						// d.f = e;
						StaticFieldRef fr = (StaticFieldRef) leftOp;
						SootField field = fr.getField();

						WrappedAccessGraph staticap = source.makeStatic();
						WrappedAccessGraph newAp = staticap
								.prependField(new WrappedSootField(field, source.getBaseType(), curr));
						out.add(newAp);
						return out;
					}
				} else if (rightOp instanceof InstanceFieldRef) {
					InstanceFieldRef fr = (InstanceFieldRef) rightOp;
					Value base = fr.getBase();
					SootField field = fr.getField();

					if (source.baseAndFirstFieldMatches(base, field)) {
						// e = a.f && source == a.f.*
						// replace in source
						if (leftOp instanceof Local && !source.baseMatches(leftOp)) {
							WrappedAccessGraph deriveWithNewLocal = source.deriveWithNewLocal((Local) leftOp,
									source.getFirstField().getType());

							out.addAll(deriveWithNewLocal.popFirstField());
						}
					}
				} else if (rightOp instanceof ArrayRef) {
					ArrayRef arrayRef = (ArrayRef) rightOp;
					if (source.baseAndFirstFieldMatches(arrayRef.getBase(), AliasFinder.ARRAY_FIELD)) {

						Set<WrappedAccessGraph> withoutFirstField = source.popFirstField();
						for (WrappedAccessGraph a : withoutFirstField) {
							out.add(a.deriveWithNewLocal((Local) leftOp, source.getFirstField().getType()));
						}
					}
				} else if (rightOp instanceof StaticFieldRef && AliasFinder.ENABLE_STATIC_FIELDS) {
					StaticFieldRef sfr = (StaticFieldRef) rightOp;
					if (source.isStatic() && source.firstFieldMatches(sfr.getField())) {
						if (leftOp instanceof Local) {
							Set<WrappedAccessGraph> withoutFirstField = source.popFirstField();
							for (WrappedAccessGraph a : withoutFirstField) {
								out.add(a.deriveWithNewLocal((Local) leftOp, source.getFirstField().getType()));
							}
						}
					}
				}

				return out;
			}
		};
	}

	@Override
	public FlowFunction<WrappedAccessGraph> getCallFlowFunction(final WrappedAccessGraph d1, final Unit callSite,
			final SootMethod callee) {
		assert callee != null;
		final Local[] paramLocals = new Local[callee.getParameterCount()];
		for (int i = 0; i < callee.getParameterCount(); i++)
			paramLocals[i] = callee.getActiveBody().getParameterLocal(i);

		final Local thisLocal = callee.isStatic() ? null : callee.getActiveBody().getThisLocal();
		return new FlowFunction<WrappedAccessGraph>() {
			@Override
			public Set<WrappedAccessGraph> computeTargets(WrappedAccessGraph source) {
				if (context.icfg().isEqualsMethod(callee) || context.icfg().isToStringMethod(callee))
					return Collections.emptySet();
				source = source.deriveWithoutEvent();
				assert source != null;
				Set<WrappedAccessGraph> out = new HashSet<>();
				Stmt is = (Stmt) callSite;
				source = source.deriveWithoutAllocationSite();
				if (AliasFinder.ENABLE_STATIC_FIELDS && source.isStatic()) {
					if (callee != null && context.icfg().isStaticFieldUsed(callee, source.getFirstField().getField())) {
						return Collections.singleton(source);
					} else {
						return Collections.emptySet();
					}
				}

				if (is.containsInvokeExpr()) {
					final InvokeExpr ie = is.getInvokeExpr();
					for (int i = 0; i < paramLocals.length; i++) {
						Value arg = ie.getArg(i);
						if (arg instanceof Local && source.baseMatches(arg)) {
							if (typeCompatible(paramLocals[i].getType(), source.getBaseType())) {
								out.add(source.deriveWithNewLocal(paramLocals[i], source.getBaseType()));
							}
						}
					}
					final Value[] callArgs = new Value[ie.getArgCount()];
					for (int i = 0; i < ie.getArgCount(); i++)
						callArgs[i] = ie.getArg(i);

					if (!callee.isStatic() && ie instanceof InstanceInvokeExpr) {
						InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) is.getInvokeExpr();

						if (source.baseMatches(iIExpr.getBase())) {
							if (callee != null && !hasCompatibleTypesForCall(source.getDelegate(), callee.getDeclaringClass()))
								return Collections.emptySet();
							if (d1 != null && d1.hasAllocationSite() && source.getFieldCount() < 1) {
								Unit sourceStmt = d1.getSourceStmt();
								if (sourceStmt instanceof AssignStmt) {
									AssignStmt as = (AssignStmt) sourceStmt;
									Value rightOp = as.getRightOp();
									Type type = rightOp.getType();
									if (type instanceof RefType) {
										RefType refType = (RefType) type;
										SootClass typeClass = refType.getSootClass();
										SootClass methodClass = callee.getDeclaringClass();
										if (typeClass != null && methodClass != null && typeClass != methodClass
												&& !typeClass.isInterface()) {
											if (!Scene.v().getFastHierarchy().isSubclass(typeClass, methodClass)) {
												return Collections.emptySet();
											}
										}
									} else if (type instanceof PrimType) {
										return Collections.emptySet();
									}

								}
							}

							WrappedAccessGraph replacedThisValue = source.deriveWithNewLocal(thisLocal, source.getBaseType());
							out.add(replacedThisValue);
						}
					}
				}
				return out;
			}
		};
	}

	@Override
	public FlowFunction<WrappedAccessGraph> getReturnFlowFunction(final WrappedAccessGraph callerD1, final WrappedAccessGraph calleeD1, final Unit callSite,
			final WrappedAccessGraph callerCallSiteFact, final SootMethod callee, final Unit exitStmt, final Unit returnSite) {
		final Local[] paramLocals = new Local[callee.getParameterCount()];
		for (int i = 0; i < callee.getParameterCount(); i++)
			paramLocals[i] = callee.getActiveBody().getParameterLocal(i);
		final Local thisLocal = callee.isStatic() ? null : callee.getActiveBody().getThisLocal();
		final SootMethod caller = (returnSite != null ? context.icfg().getMethodOf(returnSite) : null);
		return new FlowFunction<WrappedAccessGraph>() {
			

			@Override
			public Set<WrappedAccessGraph> computeTargets(WrappedAccessGraph source) {
				if (caller == null || context.icfg().isEqualsMethod(caller) || context.icfg().isToStringMethod(caller))
					return Collections.emptySet();
				// mapping of fields of AccessPath those will be killed in
				// callToReturn
				if (AliasFinder.ENABLE_STATIC_FIELDS && source.isStatic())
					return Collections.singleton(source);

				HashSet<WrappedAccessGraph> out = new HashSet<WrappedAccessGraph>();
				if (callSite instanceof Stmt) {
					Stmt is = (Stmt) callSite;

					if (is.containsInvokeExpr()) {
						InvokeExpr ie = is.getInvokeExpr();
						for (int i = 0; i < paramLocals.length; i++) {

							if (paramLocals[i] == source.getBase()) {
								Value arg = ie.getArg(i);
								if (arg instanceof Local) {
									if (typeCompatible(((Local) arg).getType(), source.getBaseType())) {
										WrappedAccessGraph deriveWithNewLocal = source.deriveWithNewLocal((Local) arg,
												source.getBaseType());
										
										EdgeFunction<V> returnEdgeFunction = context.getEdgeFunctions().getReturnEdgeFunction(callerD1, callSite, callee, exitStmt, source, returnSite, deriveWithNewLocal);
										if(!returnEdgeFunction.equalTo(EdgeIdentity.<V>v())){
												deriveWithNewLocal = deriveWithNewLocal.deriveWithEvent();
										}
										out.add(deriveWithNewLocal);
										CallSite<V> callSitePOA = new CallSite<>(callerD1, callSite, callerCallSiteFact,deriveWithNewLocal,
												returnSite);
										if (context.isInIDEPhase()) {
											out.addAll(context.callSiteFlows(callSitePOA));
										} else {
											if(callSitePOA.triggersQuery())
												context.addPOA(callSitePOA);
										}
									}
								}

							}
						}
						if (!callee.isStatic() && ie instanceof InstanceInvokeExpr) {
							if (source.baseMatches(thisLocal)) {

								InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) is.getInvokeExpr();
								Local newBase = (Local) iIExpr.getBase();
								
								if (pointsToSetCompatible(newBase, source.getBase()) && typeCompatible(newBase.getType(), source.getBaseType())) {
									WrappedAccessGraph possibleAccessPath = source.deriveWithNewLocal((Local) iIExpr.getBase(),
											source.getBaseType());
									EdgeFunction<V> returnEdgeFunction = context.getEdgeFunctions().getReturnEdgeFunction(callerD1, callSite, callee, exitStmt, source, returnSite, possibleAccessPath);
									if(!returnEdgeFunction.equalTo(EdgeIdentity.<V>v())){
										possibleAccessPath = possibleAccessPath.deriveWithEvent();
									}
									out.add(possibleAccessPath);
									
									CallSite<V> callSitePOA = new CallSite<>(callerD1, callSite, callerCallSiteFact,possibleAccessPath,
											returnSite);
									if (context.isInIDEPhase()) {
										out.addAll(context.callSiteFlows(callSitePOA));
									} else {
										if(callSitePOA.triggersQuery())
											context.addPOA(callSitePOA);
									}
								}
							}
						}
					}
				}

				if (callSite instanceof AssignStmt && exitStmt instanceof ReturnStmt) {
					AssignStmt as = (AssignStmt) callSite;
					Value leftOp = as.getLeftOp();
					// mapping of return value

					ReturnStmt returnStmt = (ReturnStmt) exitStmt;
					Value returns = returnStmt.getOp();
					// d = return out;
					if (leftOp instanceof Local) {
						if (returns instanceof Local && source.getBase() == returns) {
							out.add(source.deriveWithNewLocal((Local) leftOp, source.getBaseType()));
						}
					}
				}
				return out;
			}

		};
	}

	protected boolean pointsToSetCompatible(Local l1, Local l2) {
		PointsToAnalysis ptAnalysis = Scene.v().getPointsToAnalysis();
		return ptAnalysis.reachingObjects(l1).hasNonEmptyIntersection(ptAnalysis.reachingObjects(l2));
	}

	@Override
	public FlowFunction<WrappedAccessGraph> getCallToReturnFlowFunction(WrappedAccessGraph sourceFact, final Unit callStmt,
			Unit returnSite, boolean hasCallees) {
		if (!hasCallees) {
			return Identity.v();
		}
		if (!(callStmt instanceof Stmt)) {
			return Identity.v();
		}
		Stmt callSite = (Stmt) callStmt;
		if (!callSite.containsInvokeExpr()) {
			return Identity.v();
		}

		final InvokeExpr invokeExpr = callSite.getInvokeExpr();
		final SootMethod callee = invokeExpr.getMethod();
		return new FlowFunction<WrappedAccessGraph>() {
			@Override
			public Set<WrappedAccessGraph> computeTargets(WrappedAccessGraph source) {
				if (context.icfg().isEqualsMethod(callee) || context.icfg().isToStringMethod(callee))
					return Collections.singleton(source);
				for (int i = 0; i < invokeExpr.getArgCount(); i++) {
					if (source.baseMatches(invokeExpr.getArg(i))) {
						return Collections.emptySet();
					}
				}
				if (invokeExpr instanceof InstanceInvokeExpr) {
					InstanceInvokeExpr iie = (InstanceInvokeExpr) invokeExpr;
					Value base = iie.getBase();
					if (source.baseMatches(base)) {
						return Collections.emptySet();
					}
				}
				return Collections.singleton(source);
			}
		};
	}
}
