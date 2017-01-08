package org.openqa.selenium.interactive;

import com.google.common.base.Preconditions;

import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PointerInput implements InputDevice, Encodable {

  private final Kind kind;
  private final String name;
  private final boolean isPrimary;

  public PointerInput(Kind kind, Optional<String> name, boolean isPrimary) {
    this.kind = Preconditions.checkNotNull(kind, "Must set kind of pointer device");
    this.name = name.orElse(UUID.randomUUID().toString());
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
    parameters.put("pointerType", kind.getWireName());
    toReturn.put("parameters", parameters);

    return toReturn;
  }

  public Interaction createPointerMove(Duration duration, WebElement target, int x, int y) {
    return new Move(this, duration, target, x, y);
  }

  public Interaction createPointerDown(int button) {
    return new PointerPress(this, PointerPress.Direction.DOWN, button);
  }

  public Interaction createPointerUp(int button) {
    return new PointerPress(this, PointerPress.Direction.UP, button);
  }

  private static class PointerPress extends Interaction implements Encodable {

    private final Direction direction;
    private final int button;

    public PointerPress(InputDevice source, Direction direction, int button) {
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

    enum Direction {
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

  private static class Move extends Interaction implements Encodable {

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

  public enum Kind {
    MOUSE("mouse"),
    PEN("pen"),
    TOUCH("touch"),;

    private final String wireName;


    Kind(String pointerSubType) {
      this.wireName = pointerSubType;
    }

    public String getWireName() {
      return wireName;
    }
  }
}
