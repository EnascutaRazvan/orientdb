package com.orientechnologies.orient.core.sql.executor;

import com.orientechnologies.common.concur.OTimeoutException;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.sql.executor.resultset.OExecutionStream;
import com.orientechnologies.orient.core.sql.parser.OReturnStatement;
import com.orientechnologies.orient.core.sql.parser.OStatement;

/**
 * @author Luigi Dell'Aquila (l.dellaquila-(at)-orientdb.com)
 *     <p>This step represents the execution plan of an instruciton instide a batch script
 */
public class ScriptLineStep extends AbstractExecutionStep {
  protected final OStatement statement;
  private OInternalExecutionPlan plan;

  public ScriptLineStep(OStatement statement, OCommandContext ctx) {
    super(ctx);
    this.statement = statement;
  }

  private void initPlan(OCommandContext ctx) {
    if (plan == null) {
      plan = statement.createExecutionPlan(ctx);
    }
  }

  @Override
  public OExecutionStream internalStart(OCommandContext ctx) throws OTimeoutException {
    initPlan(ctx);
    return plan.start(ctx);
  }

  public boolean containsReturn(OCommandContext ctx) {
    initPlan(ctx);
    if (plan instanceof OScriptExecutionPlan) {
      return ((OScriptExecutionPlan) plan).containsReturn(ctx);
    }
    if (plan instanceof OSingleOpExecutionPlan) {
      if (((OSingleOpExecutionPlan) plan).statement instanceof OReturnStatement) {
        return true;
      }
    }
    if (plan instanceof OIfExecutionPlan) {
      if (((OIfExecutionPlan) plan).containsReturn()) {
        return true;
      }
    }

    if (plan instanceof OForEachExecutionPlan) {
      if (((OForEachExecutionPlan) plan).containsReturn()) {
        return true;
      }
    }
    return false;
  }

  public OExecutionStepInternal executeUntilReturn(OCommandContext ctx) {
    initPlan(ctx);
    if (plan instanceof OScriptExecutionPlan) {
      return ((OScriptExecutionPlan) plan).executeUntilReturn(ctx);
    }
    if (plan instanceof OSingleOpExecutionPlan) {
      if (((OSingleOpExecutionPlan) plan).statement instanceof OReturnStatement) {
        return new ReturnStep(((OSingleOpExecutionPlan) plan).statement, ctx);
      }
    }
    if (plan instanceof OIfExecutionPlan) {
      return ((OIfExecutionPlan) plan).executeUntilReturn(ctx);
    }
    throw new IllegalStateException();
  }

  @Override
  public String prettyPrint(OPrintContext ctx) {
    if (statement == null) {
      return "Script Line";
    }
    return statement.getOriginalStatement();
  }
}
