package com.orientechnologies.orient.core.sql.executor;

import com.orientechnologies.common.concur.OTimeoutException;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.sql.executor.resultset.OExecutionStream;
import com.orientechnologies.orient.core.sql.executor.resultset.OTimeoutExecutionStream;
import com.orientechnologies.orient.core.sql.parser.OTimeout;

/** Created by luigidellaquila on 08/08/16. */
public class AccumulatingTimeoutStep extends AbstractExecutionStep {

  private final OTimeout timeout;

  public AccumulatingTimeoutStep(OTimeout timeout, OCommandContext ctx) {
    super(ctx);
    this.timeout = timeout;
  }

  @Override
  public OExecutionStream internalStart(OCommandContext ctx) throws OTimeoutException {
    final OExecutionStream internal = getPrev().get().start(ctx);
    return new OTimeoutExecutionStream(internal, this.timeout.getVal().longValue(), this::fail);
  }

  private void fail() {
    if (OTimeout.RETURN.equals(this.timeout.getFailureStrategy())) {
      // do nothing
    } else {
      sendTimeout();
      throw new OTimeoutException("Timeout expired");
    }
  }

  @Override
  public boolean canBeCached() {
    return true;
  }

  @Override
  public OExecutionStep copy(OCommandContext ctx) {
    return new AccumulatingTimeoutStep(timeout.copy(), ctx);
  }

  @Override
  public String prettyPrint(OPrintContext ctx) {
    return OExecutionStepInternal.getIndent(ctx)
        + "+ TIMEOUT ("
        + timeout.getVal().toString()
        + "ms)";
  }
}
