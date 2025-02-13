package com.orientechnologies.orient.core.sql.executor;

/** Created by luigidellaquila on 08/08/16. */
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.sql.executor.resultset.OExecutionStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** @author Luigi Dell'Aquila (l.dellaquila-(at)-orientdb.com) */
public class OIfExecutionPlan implements OInternalExecutionPlan {

  protected IfStep step;
  private OCommandContext context;

  public OIfExecutionPlan() {}

  @Override
  public void reset(OCommandContext ctx) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    step.close();
  }

  @Override
  public OExecutionStream start(OCommandContext ctx) {
    return step.start(ctx);
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    return prettyPrint(new OPrintContexImpl(context, depth, indent));
  }

  @Override
  public String prettyPrint(OPrintContext ctx) {
    StringBuilder result = new StringBuilder();
    result.append(step.prettyPrint(ctx));
    return result.toString();
  }

  @Override
  public String prettyPrint() {
    return prettyPrint(0, 0);
  }

  public void chain(IfStep step) {
    this.step = step;
  }

  @Override
  public List<OExecutionStep> getSteps() {
    // TODO do a copy of the steps
    return Collections.singletonList(step);
  }

  public void setSteps(List<OExecutionStepInternal> steps) {
    this.step = (IfStep) steps.get(0);
  }

  @Override
  public OResult toResult() {
    return toResult(new OToResultContextImpl(this.context));
  }

  @Override
  public OResult toResult(OToResultContext ctx) {
    OResultInternal result = new OResultInternal();
    result.setProperty("type", "IfExecutionPlan");
    result.setProperty("javaType", getClass().getName());
    result.setProperty("cost", getCost());
    result.setProperty("prettyPrint", prettyPrint(0, 2));
    result.setProperty("stmText", getStatement());
    result.setProperty("genericStm", getGenericStatement());
    result.setProperty("steps", Collections.singletonList(step.toResult(ctx)));
    return result;
  }

  @Override
  public long getCost() {
    return 0l;
  }

  @Override
  public boolean canBeCached() {
    return false;
  }

  @Override
  public Set<String> getIndexes() {
    Set<String> indexes = new HashSet<>();
    OExecutionStepInternal.fillIndexes(step, indexes);
    return indexes;
  }

  @Override
  public void fillContext(OCommandContext context) {
    this.context = context;
  }
}
