package com.orientechnologies.orient.core.sql.executor.resultset;

import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.sql.executor.OExecutionPlan;
import com.orientechnologies.orient.core.sql.executor.OExecutionStep;
import com.orientechnologies.orient.core.sql.executor.OInternalExecutionPlan;
import com.orientechnologies.orient.core.sql.executor.OPrintContexImpl;
import com.orientechnologies.orient.core.sql.executor.OResult;
import java.util.List;
import java.util.Set;

public class OContextExecutionPlan implements OExecutionPlan {

  private OInternalExecutionPlan plan;
  private OCommandContext ctx;

  public OContextExecutionPlan(OInternalExecutionPlan plan, OCommandContext context) {
    this.ctx = context;
  }

  @Override
  public List<OExecutionStep> getSteps() {
    return plan.getSteps();
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    return plan.prettyPrint(new OPrintContexImpl(ctx));
  }

  @Override
  public String prettyPrint() {
    return plan.prettyPrint(new OPrintContexImpl(ctx));
  }

  @Override
  public OResult toResult() {
    return plan.toResult();
  }

  @Override
  public Set<String> getIndexes() {
    return plan.getIndexes();
  }
}
