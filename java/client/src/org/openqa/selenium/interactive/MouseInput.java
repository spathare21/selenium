package org.openqa.selenium.interactive;

import com.google.common.base.Preconditions;

import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class MouseInput implements InputDevice, Encodable {

  private final String name;
  private final boolean isPrimary;

  public MouseInput(String name, boolean isPrimary) {
    this.name = name;
    this.isPrimary = isPrimary;
  }

  @Override
  public SourceType getInputType() {
    return SourceType.POINTER;
  }

  @Override
  public Map<String, Object> encode() {
    Map<String, Object> toReturn = new HashMap<>();

    toReturn.put("type", "pointer");
    toReturn.put("id", name);

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("primary", isPrimary);
    parameters.put("pointerType", "mouse");
    toReturn.put("parameters", parameters);

    return toReturn;
  }

  public Action createMouseMove(Duration duration, WebElement target, int x, int y) {
    return new Move(this, duration, target, x, y);
  }

  public Action createMouseDown(int button) {
    return new MousePress(this, MousePress.Direction.DOWN, button);
  }

  public Action createMouseUp(int button) {
    return new MousePress(this, MousePress.Direction.UP, button);
  }

  private static class MousePress extends Action implements Encodable {

    private final Direction direction;
    private final int button;

    public MousePress(InputDevice source, Direction direction, int button) {
      super(source);

      Preconditions.checkState(
          button >= 0,
          "Button must be greater than or equal to 0: %d", button);
      this.direction = Preconditions.checkNotNull(direction);
      this.button = button;
    }

    @Override
    public Map<String, Object> encode() {
      Map<String, Object> toReturn = new HashMap<>();

      toReturn.put("type", direction.getType());
      toReturn.put("button", button);

      return toReturn;
    }

    static enum Direction {
      DOWN("pointerDown"),
      UP("pointerUp");

      private final String type;

      Direction(String type) {
        this.type = type;
      }

      public String getType() {
        return type;
      }
    }
  }

  private static class Move extends Action implements Encodable {

    private final WebElement target;
    private final int x;
    private final int y;
    private final Duration duration;

    protected Move(InputDevice source, Duration duration, WebElement target, int x, int y) {
      super(source);

      Preconditions.checkState(x >= 0, "X value must be 0 or greater: %d", x);
      Preconditions.checkState(y >= 0, "Y value must be 0 or greater: %d", y);

      Preconditions.checkState(!duration.isNegative(), "Duration value must be 0 or greater: %s", duration);

      this.target = Preconditions.checkNotNull(target, "Target not set");
      this.x = x;
      this.y = y;
      this.duration = duration;
    }

    @Override
    protected boolean isValidFor(SourceType sourceType) {
      return SourceType.POINTER == sourceType;
    }

    @Override
    public Map<String, Object> encode() {
      Map<String, Object> toReturn = new HashMap<>();

      toReturn.put("type", "pointerMove");
      toReturn.put("duration", duration.toMillis());
      toReturn.put("element", target);
      toReturn.put("x", x);
      toReturn.put("y", y);

      return toReturn;
    }
  }
}
