package com.orientechnologies.orient.core.sql.executor;

import com.orientechnologies.orient.core.command.OBasicCommandContext;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.sql.executor.resultset.OExecutionStream;
import com.orientechnologies.orient.core.sql.parser.ODDLStatement;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** @author Luigi Dell'Aquila (l.dellaquila-(at)-orientdb.com) */
public class ODDLExecutionPlan implements OInternalExecutionPlan {

  private final ODDLStatement statement;
  private OCommandContext context;

  public ODDLExecutionPlan(ODDLStatement stm) {
    this.statement = stm;
  }

  @Override
  public void close() {}

  @Override
  public OExecutionStream start(OCommandContext ctx) {
    return OExecutionStream.empty();
  }

  public void reset(OCommandContext ctx) {}

  @Override
  public long getCost() {
    return 0;
  }

  @Override
  public boolean canBeCached() {
    return false;
  }

  public OExecutionStream executeInternal(OBasicCommandContext ctx)
      throws OCommandExecutionException {
    return statement.executeDDL(ctx);
  }

  @Override
  public List<OExecutionStep> getSteps() {
    return Collections.emptyList();
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    return prettyPrint(new OPrintContexImpl(context, depth, indent));
  }

  @Override
  public String prettyPrint(OPrintContext ctx) {
    String spaces = OExecutionStepInternal.getIndent(ctx);
    StringBuilder result = new StringBuilder();
    result.append(spaces);
    result.append("+ DDL\n");
    result.append("  ");
    result.append(statement.prettyPrint(ctx));
    return result.toString();
  }

  @Override
  public String prettyPrint() {
    return prettyPrint(0, 0);
  }

  @Override
  public OResult toResult() {
    OResultInternal result = new OResultInternal();
    result.setProperty("type", "DDLExecutionPlan");
    result.setProperty(JAVA_TYPE, getClass().getName());
    result.setProperty("stmText", statement.toString());
    result.setProperty("cost", getCost());
    result.setProperty("prettyPrint", prettyPrint(0, 2));
    return result;
  }

  @Override
  public Set<String> getIndexes() {
    return Collections.emptySet();
  }

  @Override
  public void fillContext(OCommandContext context) {
    this.context = context;
  }
}
