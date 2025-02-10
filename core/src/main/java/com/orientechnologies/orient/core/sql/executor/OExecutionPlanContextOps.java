package com.orientechnologies.orient.core.sql.executor;

import com.orientechnologies.orient.core.command.OCommandContext;

public interface OExecutionPlanContextOps extends OExecutionPlan {
  void fillContext(OCommandContext context);
}
