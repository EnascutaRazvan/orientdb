package com.orientechnologies.orient.core.sql.executor.resultset;

import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.sql.executor.OExecutionPlan;
import com.orientechnologies.orient.core.sql.executor.OExecutionPlanContextOps;
import com.orientechnologies.orient.core.sql.executor.OInfoExecutionPlan;
import com.orientechnologies.orient.core.sql.executor.OInternalExecutionPlan;
import com.orientechnologies.orient.core.sql.executor.OQueryMetrics;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultSetInternal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class OExecutionResultSet implements OResultSetInternal, OQueryMetrics {

  private final OExecutionStream stream;
  private final OCommandContext context;
  private final Optional<OExecutionPlan> plan;
  private boolean closed = false;
  private long startTime = System.currentTimeMillis();
  private long elapsedTime = -1;

  public OExecutionResultSet(
      OExecutionStream stream, OCommandContext context, OExecutionPlanContextOps plan) {
    super();
    this.stream = stream;
    this.context = context;
    if (plan != null) {
      plan.fillContext(context);
    }
    this.plan = Optional.ofNullable(plan).map((p) -> OInfoExecutionPlan.fromResult(p.toResult()));
  }

  @Override
  public boolean hasNext() {
    if (closed) {
      return false;
    } else {
      return stream.hasNext(context);
    }
  }

  @Override
  public OResult next() {
    if (closed) {
      throw new NoSuchElementException();
    } else {
      return stream.next(context);
    }
  }

  @Override
  public void close() {
    if (!closed) {
      stream.close(context);
      closed = true;
      logProfiling();
    }
  }

  private void logProfiling() {
    computeElapsed();
    if (plan.isPresent() && plan.get() instanceof OInternalExecutionPlan) {
      var executionPlan = (OInternalExecutionPlan) plan.get();
      if (executionPlan.getStatement() != null && Orient.instance().getProfiler().isRecording()) {
        final ODatabaseDocumentInternal db = (ODatabaseDocumentInternal) context.getDatabase();
        if (db != null) {
          final OSecurityUser user = db.getUser();
          final String userString = user != null ? user.toString() : null;
          Orient.instance()
              .getProfiler()
              .stopChrono(
                  "db." + db.getName() + ".command.sql." + executionPlan.getStatement(),
                  "Command executed against the database",
                  elapsedTime,
                  "db.*.command.*",
                  null,
                  userString);
        }
      }
    }
  }

  protected void computeElapsed() {
    elapsedTime = System.currentTimeMillis() - startTime;
  }

  @Override
  public Optional<OExecutionPlan> getExecutionPlan() {
    return plan;
  }

  @Override
  public Map<String, Long> getQueryStats() {
    return null;
  }

  @Override
  public boolean isDetached() {
    return false;
  }

  @Override
  public boolean isExplain() {
    return false;
  }

  @Override
  public long getStartTime() {
    return startTime;
  }

  @Override
  public long getElapsedTimeMillis() {
    if (elapsedTime == -1) {
      return System.currentTimeMillis() - startTime;
    }
    return elapsedTime;
  }

  @Override
  public String getStatement() {
    if (plan.isPresent()) {
      OExecutionPlan rp = plan.get();
      if (rp instanceof OInternalExecutionPlan) {
        return ((OInternalExecutionPlan) rp).getGenericStatement();
      } else if (rp instanceof OInfoExecutionPlan) {
        return ((OInfoExecutionPlan) rp).getGenericStatement();
      } else {
        return rp.toString();
      }
    } else {
      return "";
    }
  }

  @Override
  public String getLanguage() {
    return "sql";
  }
}
