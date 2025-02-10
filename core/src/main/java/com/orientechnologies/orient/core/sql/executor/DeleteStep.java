package com.orientechnologies.orient.core.sql.executor;

import com.orientechnologies.common.concur.OTimeoutException;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.sql.executor.resultset.OExecutionStream;
import java.util.Optional;

/**
 * Deletes records coming from upstream steps
 *
 * @author Luigi Dell'Aquila (l.dellaquila-(at)-orientdb.com)
 */
public class DeleteStep extends AbstractExecutionStep {

  public DeleteStep(OCommandContext ctx) {
    super(ctx);
  }

  @Override
  public OExecutionStream internalStart(OCommandContext ctx) throws OTimeoutException {
    OExecutionStream upstream = getPrev().get().start(ctx);
    return upstream.map(this::mapResult);
  }

  private OResult mapResult(OResult result, OCommandContext ctx) {
    Optional<ORID> id = result.getIdentity();
    if (id.isPresent()) {
      ctx.getDatabase().delete(id.get());
    }
    return result;
  }

  @Override
  public String prettyPrint(OPrintContext ctx) {
    String spaces = OExecutionStepInternal.getIndent(ctx);
    StringBuilder result = new StringBuilder();
    result.append(spaces);
    result.append("+ DELETE");
    if (ctx.isProfilingEnabled()) {
      result.append(" (" + ctx.getCostFormatted(this) + ")");
    }
    return result.toString();
  }

  @Override
  public OExecutionStep copy(OCommandContext ctx) {
    return new DeleteStep(ctx);
  }

  @Override
  public boolean canBeCached() {
    return true;
  }
}
