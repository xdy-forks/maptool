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

import java.io.Serializable;

public class TokenProperty implements Serializable {
  private final String name;
  private final String shortName;
  private final boolean highPriority; // showOnStatSheet; so that 1.3b28 files load in 1.3b29
  private final boolean ownerOnly;
  private final boolean gmOnly;
  private final String defaultValue;

  public TokenProperty(String name, String shortName) {
    this(name, shortName, false, false, false, null);
  }

  public TokenProperty(String name, boolean highPriority, boolean isOwnerOnly, boolean isGMOnly) {
    this(name, null, highPriority, isOwnerOnly, isGMOnly, null);
  }

  public TokenProperty(
      String name,
      String shortName,
      boolean highPriority,
      boolean isOwnerOnly,
      boolean isGMOnly,
      String defaultValue) {
    this.name = name;
    this.shortName = shortName;
    this.highPriority = highPriority;
    this.ownerOnly = isOwnerOnly;
    this.gmOnly = isGMOnly;
    this.defaultValue = defaultValue;
  }

  public boolean isOwnerOnly() {
    return ownerOnly;
  }

  public boolean isShowOnStatSheet() {
    return highPriority;
  }

  public String getName() {
    return name;
  }

  public String getShortName() {
    return shortName;
  }

  public boolean isGMOnly() {
    return gmOnly;
  }

  public String getDefaultValue() {
    return this.defaultValue;
  }
}
