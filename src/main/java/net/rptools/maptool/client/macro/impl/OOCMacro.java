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
package net.rptools.maptool.client.macro.impl;

import java.awt.Color;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.model.TextMessage;

@MacroDefinition(
    name = "ooc",
    aliases = {"ooc"},
    description = "ooc.description")
public class OOCMacro extends AbstractMacro {
  public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
    macro = processText(macro);
    StringBuilder sb = new StringBuilder();

    // Prevent spoofing
    sb.append(MapTool.getFrame().getCommandPanel().getIdentity());
    sb.append(": ");

    Color color = MapTool.getFrame().getCommandPanel().getTextColorWell().getColor();
    if (color != null) {
      sb.append("<span style='color:#")
          .append(Integer.toHexString((color.getRGB() & 0xFFFFFF)))
          .append("'>");
      // sb.append("<span style='color:#").append(String.format("%06X", (color.getRGB() &
      // 0xFFFFFF))).append("'>");
    }
    sb.append("(( ").append(macro).append(" ))");
    if (color != null) {
      sb.append("</span>");
    }
    MapTool.addMessage(TextMessage.say(context.getTransformationHistory(), sb.toString()));
  }
}
