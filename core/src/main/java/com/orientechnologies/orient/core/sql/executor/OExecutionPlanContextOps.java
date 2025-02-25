package com.orientechnologies.orient.core.sql.executor;

public interface OExecutionPlanContextOps extends OExecutionPlan {
  OResult toResult(OToResultContext ctx);
}
