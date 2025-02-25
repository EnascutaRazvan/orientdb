package com.orientechnologies.orient.core.sql.parser;

import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.sql.executor.OExecutionStep;
import com.orientechnologies.orient.core.sql.executor.OInternalExecutionPlan;
import com.orientechnologies.orient.core.sql.executor.OPrintContexImpl;
import com.orientechnologies.orient.core.sql.executor.OPrintContext;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultInternal;
import com.orientechnologies.orient.core.sql.executor.OToResultContext;
import com.orientechnologies.orient.core.sql.executor.OToResultContextImpl;
import com.orientechnologies.orient.core.sql.executor.resultset.OExecutionStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OExplainExecutionPlan implements OInternalExecutionPlan {

  private OInternalExecutionPlan toProfile;
  private OCommandContext context;

  public OExplainExecutionPlan(OInternalExecutionPlan toProfile) {
    this.toProfile = toProfile;
  }

  @Override
  public void fillContext(OCommandContext context) {
    this.context = context;
  }

  @Override
  public List<OExecutionStep> getSteps() {
    return toProfile.getSteps();
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    return prettyPrint(new OPrintContexImpl(context, depth, indent));
  }

  @Override
  public String prettyPrint() {
    return prettyPrint(new OPrintContexImpl(context));
  }

  @Override
  public OResult toResult() {
    return toResult(new OToResultContextImpl(context));
  }

  @Override
  public Set<String> getIndexes() {
    return toProfile.getIndexes();
  }

  @Override
  public void close() {}

  @Override
  public OExecutionStream start(OCommandContext ctx) {
    OResultInternal result = new OResultInternal();
    result.setProperty("executionPlan", toResult(new OToResultContextImpl(ctx)));
    result.setProperty("executionPlanAsString", prettyPrint(new OPrintContexImpl(ctx)));
    return OExecutionStream.singleton(result);
  }

  @Override
  public void reset(OCommandContext ctx) {}

  @Override
  public long getCost() {
    return 0;
  }

  @Override
  public boolean canBeCached() {
    return toProfile.canBeCached();
  }

  @Override
  public boolean isExplain() {
    return true;
  }

  @Override
  public String prettyPrint(OPrintContext ctx) {
    StringBuilder result = new StringBuilder();
    result.append("EXPLAIN \n");
    ctx.incDepth();
    result.append(this.toProfile.prettyPrint(ctx));
    ctx.decDepth();
    return result.toString();
  }

  @Override
  public OResult toResult(OToResultContext ctx) {
    OResultInternal result = new OResultInternal();
    result.setProperty("type", "ExplainExecutionPlan");
    result.setProperty(JAVA_TYPE, getClass().getName());
    result.setProperty("cost", getCost());
    result.setProperty("prettyPrint", prettyPrint(0, 2));
    result.setProperty("stmText", getStatement());
    result.setProperty("genericStm", getGenericStatement());
    result.setProperty("statement", Arrays.asList(toProfile.toResult(ctx)));
    List<OExecutionStep> steps = toProfile.getSteps();
    result.setProperty(
        "steps",
        steps == null ? null : steps.stream().map(x -> x.toResult()).collect(Collectors.toList()));
    return result;
  }
}
