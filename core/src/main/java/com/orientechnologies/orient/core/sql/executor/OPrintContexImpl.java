package com.orientechnologies.orient.core.sql.executor;

import com.orientechnologies.orient.core.command.OCommandContext;

public class OPrintContexImpl implements OPrintContext {

  private int depth = 0;
  private int ident = 0;
  private OCommandContext ctx;

  public OPrintContexImpl() {}

  public OPrintContexImpl(OCommandContext ctx) {
    this.ctx = ctx;
  }

  public OPrintContexImpl(OCommandContext ctx, int depth, int ident) {
    this.ctx = ctx;
    this.depth = depth;
    this.ident = ident;
  }

  @Override
  public int getDepth() {
    return depth;
  }

  @Override
  public int getIdent() {
    return ident;
  }

  @Override
  public void incDepth() {
    depth++;
  }

  @Override
  public void decDepth() {
    depth--;
  }
}
