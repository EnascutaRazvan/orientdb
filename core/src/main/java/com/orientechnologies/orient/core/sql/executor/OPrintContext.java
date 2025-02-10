package com.orientechnologies.orient.core.sql.executor;

public interface OPrintContext {

  int getDepth();

  int getIdent();

  void incDepth();

  void decDepth();

  String getCostFormatted(OExecutionStep step);

  boolean isProfilingEnabled();
}
