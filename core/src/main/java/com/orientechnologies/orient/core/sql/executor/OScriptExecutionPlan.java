/** Created by luigidellaquila on 08/08/16. */
package com.orientechnologies.orient.core.sql.executor;

import com.orientechnologies.common.concur.OTimeoutException;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.sql.executor.resultset.OExecutionStream;
import com.orientechnologies.orient.core.sql.parser.OStatement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** @author Luigi Dell'Aquila (l.dellaquila-(at)-orientdb.com) */
public class OScriptExecutionPlan implements OInternalExecutionPlan {

  protected List<OExecutionStepInternal> steps = new ArrayList<>();
  private String statement;
  private String genericStatement;
  private OCommandContext context;
  private boolean idempotent = true;

  public OScriptExecutionPlan() {}

  @Override
  public void reset(OCommandContext ctx) {}

  @Override
  public void close() {}

  @Override
  public OExecutionStream start(OCommandContext ctx) {

    for (int i = 0; i < steps.size(); i++) {
      OExecutionStepInternal step = steps.get(i);
      OExecutionStream lastResult = step.start(ctx);
      if (lastResult.isTermination(ctx)) {
        if (idempotent) {
          return lastResult;
        } else {
          return OExecutionStream.collectAll(lastResult, ctx);
        }
      }
      if (i < steps.size() - 1) {
        OExecutionStream.consume(lastResult, ctx);
      } else {
        if (idempotent) {
          return lastResult;
        } else {
          return OExecutionStream.collectAll(lastResult, ctx);
        }
      }
    }
    // In practice never here.
    return null;
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    return prettyPrint(new OPrintContexImpl(context, depth, indent));
  }

  @Override
  public String prettyPrint(OPrintContext ctx) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < steps.size(); i++) {
      OExecutionStepInternal step = steps.get(i);
      result.append(step.prettyPrint(ctx));
      if (i < steps.size() - 1) {
        result.append("\n");
      }
    }
    return result.toString();
  }

  @Override
  public String prettyPrint() {
    return prettyPrint(0, 0);
  }

  public void chain(OStatement nextStm, OCommandContext ctx) {
    idempotent &= nextStm.isIdempotent();
    OExecutionStepInternal lastStep = steps.size() == 0 ? null : steps.get(steps.size() - 1);
    ScriptLineStep nextStep = new ScriptLineStep(nextStm, ctx);
    if (lastStep != null) {
      nextStep.setPrevious(lastStep);
    }
    steps.add(nextStep);
  }

  public void chain(ORetryExecutionPlan retryStep, OCommandContext ctx) {
    idempotent = false;
    OExecutionStepInternal nextStep =
        new OExecutionStepInternal() {

          @Override
          public OExecutionStream start(OCommandContext ctx) throws OTimeoutException {
            return OExecutionStream.collectAll(retryStep.start(ctx), ctx);
          }

          public void setPrevious(OExecutionStepInternal step) {}

          @Override
          public void sendTimeout() {}

          @Override
          public void close() {}
        };
    steps.add(nextStep);
  }

  @Override
  public List<OExecutionStep> getSteps() {
    // TODO do a copy of the steps
    return (List) steps;
  }

  public void setSteps(List<OExecutionStepInternal> steps) {
    this.steps = (List) steps;
  }

  @Override
  public OResult toResult() {
    return toResult(new OToResultContextImpl(this.context));
  }

  @Override
  public OResult toResult(OToResultContext ctx) {
    OResultInternal result = new OResultInternal();
    result.setProperty("type", "ScriptExecutionPlan");
    result.setProperty("javaType", getClass().getName());
    result.setProperty("cost", getCost());
    result.setProperty("prettyPrint", prettyPrint(0, 2));
    result.setProperty("stmText", getStatement());
    result.setProperty("genericStm", getGenericStatement());
    result.setProperty(
        "steps",
        steps == null
            ? null
            : steps.stream().map(x -> x.toResult(ctx)).collect(Collectors.toList()));
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
  public String getStatement() {
    return statement;
  }

  @Override
  public void setStatement(String statement) {
    this.statement = statement;
  }

  @Override
  public String getGenericStatement() {
    return this.genericStatement;
  }

  @Override
  public void setGenericStatement(String stm) {
    this.genericStatement = stm;
  }

  @Override
  public Set<String> getIndexes() {
    Set<String> indexes = new HashSet<>();
    for (OExecutionStep chilStep : steps) {
      OExecutionStepInternal.fillIndexes(chilStep, indexes);
    }
    return indexes;
  }

  @Override
  public void fillContext(OCommandContext context) {
    this.context = context;
  }
}
