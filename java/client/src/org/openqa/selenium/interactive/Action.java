package org.openqa.selenium.interactive;

import com.google.common.base.Preconditions;

public abstract class Action {

  private final InputDevice source;

  protected Action(InputDevice source) {
    this.source = Preconditions.checkNotNull(source);
  }

  protected boolean isValidFor(SourceType sourceType) {
    return source.getInputType() == sourceType;
  }

  public InputDevice getSource() {
    return source;
  }
}
