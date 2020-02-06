/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import java.awt.geom.Area;

public final class SightType {
  private final String name;
  private final double multiplier;
  private final LightSource personalLightSource;
  private final ShapeType shape;
  private final int arc;
  private final float distance;
  private final int offset;
  private final boolean scaleWithToken;

  public int getOffset() {
    return this.offset;
  }

  public float getDistance() {
    return this.distance;
  }

  public ShapeType getShape() {
    return shape != null ? shape : ShapeType.CIRCLE;
  }

  public boolean isScaleWithToken() {
    return scaleWithToken;
  }

  public SightType(
      String name,
      double multiplier,
      LightSource personalLightSource,
      ShapeType shape,
      int arc,
      float distance,
      int offset,
      boolean scaleWithToken) {
    this.name = name;
    this.multiplier = multiplier;
    this.personalLightSource = personalLightSource;
    this.shape = shape;
    this.arc = arc;
    this.distance = distance;
    this.offset = offset;
    this.scaleWithToken = scaleWithToken;
  }

  public String getName() {
    return name;
  }

  public double getMultiplier() {
    return multiplier;
  }

  public boolean hasPersonalLightSource() {
    return personalLightSource != null;
  }

  public LightSource getPersonalLightSource() {
    return personalLightSource;
  }

  public int getArc() {
    return arc;
  }

  /**
   * Get the shapedArea of a token's vision in a zone
   *
   * @param token the token.
   * @param zone the zone.
   * @return the Area of the vision shape.
   */
  public Area getVisionShape(Token token, Zone zone) {
    return zone.getGrid()
        .getShapedArea(getShape(), token, getDistance(), getArc(), getOffset(), scaleWithToken);
  }
}
