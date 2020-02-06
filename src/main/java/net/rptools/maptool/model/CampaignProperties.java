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

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.token.AbstractTokenOverlay;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.client.ui.token.ColorDotTokenOverlay;
import net.rptools.maptool.client.ui.token.DiamondTokenOverlay;
import net.rptools.maptool.client.ui.token.ImageTokenOverlay;
import net.rptools.maptool.client.ui.token.MultipleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.OTokenOverlay;
import net.rptools.maptool.client.ui.token.ShadedTokenOverlay;
import net.rptools.maptool.client.ui.token.SingleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.TriangleTokenOverlay;
import net.rptools.maptool.client.ui.token.TwoImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.TwoToneBarTokenOverlay;
import net.rptools.maptool.client.ui.token.XTokenOverlay;
import net.rptools.maptool.client.ui.token.YieldTokenOverlay;

public class CampaignProperties {

  public static final String DEFAULT_TOKEN_PROPERTY_TYPE = "Basic";

  private final ConcurrentMap<String, List<TokenProperty>> tokenTypeMap = new ConcurrentHashMap<>();
  private final CopyOnWriteArrayList<String> remoteRepositoryList = new CopyOnWriteArrayList<>();
  private final ConcurrentMap<String, Map<GUID, LightSource>> lightSourcesMap =
      new ConcurrentHashMap<>();
  private final ConcurrentMap<String, LookupTable> lookupTableMap = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, SightType> sightTypeMap = new ConcurrentHashMap<>();

  private String defaultSightType;
  private final Object lock = new Object();

  // TODO: fix for when old campaigns have been loaded into b33+
  private final ConcurrentMap<String, BooleanTokenOverlay> tokenStates =
      new ConcurrentSkipListMap<>();
  private final ConcurrentMap<String, BarTokenOverlay> tokenBars = new ConcurrentSkipListMap<>();
  private final ConcurrentMap<String, String> characterSheets = new ConcurrentHashMap<>();

  /** Flag indicating that owners have special permissions */
  private boolean initiativeOwnerPermissions = AppPreferences.getInitOwnerPermissions();

  /** Flag indicating that owners can only move tokens when they have initiative */
  private boolean initiativeMovementLock = AppPreferences.getInitLockMovement();

  public CampaignProperties() {
    init();
  }

  public CampaignProperties(CampaignProperties properties) {
    for (Entry<String, List<TokenProperty>> entry : properties.tokenTypeMap.entrySet()) {
      List<TokenProperty> typeList = new ArrayList<>(properties.tokenTypeMap.get(entry.getKey()));
      tokenTypeMap.put(entry.getKey(), typeList);
    }
    remoteRepositoryList.addAll(properties.remoteRepositoryList);

    if (properties.lookupTableMap != null) {
      lookupTableMap.putAll(properties.lookupTableMap);
    }
    setDefaultSightType(properties.defaultSightType);
    if (properties.sightTypeMap != null) {
      sightTypeMap.putAll(properties.sightTypeMap);
    }
    // TODO: This doesn't feel right, should we deep copy, or does this do that automatically ?
    lightSourcesMap.putAll(properties.lightSourcesMap);

    if (properties.tokenStates.isEmpty()) {
      properties.initTokenStatesMap();
    }
    for (BooleanTokenOverlay overlay : properties.tokenStates.values()) {
      overlay = (BooleanTokenOverlay) overlay.clone();
      tokenStates.put(overlay.getName(), overlay);
    } // endfor

    if (properties.tokenBars.isEmpty()) {
      properties.initTokenBarsMap();
    }
    for (BarTokenOverlay overlay : properties.tokenBars.values()) {
      overlay = (BarTokenOverlay) overlay.clone();
      tokenBars.put(overlay.getName(), overlay);
    } // endfor

    setInitiativeOwnerPermissions(properties.initiativeOwnerPermissions);
    setInitiativeMovementLock(properties.initiativeMovementLock);

    if (properties.characterSheets.isEmpty()) {
      properties.initCharacterSheetsMap();
    }
    for (String type : properties.characterSheets.keySet()) {
      characterSheets.put(type, properties.characterSheets.get(type));
    }
  }

  public void mergeInto(CampaignProperties properties) {
    // This will replace any dups
    properties.tokenTypeMap.putAll(tokenTypeMap);
    // Need to cull out dups
    for (String repo : properties.remoteRepositoryList) {
      if (!remoteRepositoryList.contains(repo)) {
        remoteRepositoryList.add(repo);
      }
    }
    properties.lightSourcesMap.putAll(lightSourcesMap);
    properties.lookupTableMap.putAll(lookupTableMap);
    properties.sightTypeMap.putAll(sightTypeMap);
    properties.tokenStates.putAll(tokenStates);
    properties.tokenBars.putAll(tokenBars);
  }

  public Map<String, List<TokenProperty>> getTokenTypeMap() {
    if (tokenTypeMap.isEmpty()) {
      initTokenTypeMap();
    }
    return tokenTypeMap;
  }

  public Map<String, SightType> getSightTypeMap() {
    if (sightTypeMap.isEmpty()) {
      initSightTypeMap();
    }
    return sightTypeMap;
  }

  public void replaceSightTypeMap(Map<String, SightType> map) {
    if (map != null) {
      sightTypeMap.clear();
      sightTypeMap.putAll(map);
    }
  }

  // TODO: This is for conversion from 1.3b19-1.3b20
  public void replaceTokenTypeMap(Map<String, List<TokenProperty>> map) {
    if (map != null) {
      tokenTypeMap.clear();
      tokenTypeMap.putAll(map);
    }
  }

  public List<TokenProperty> getTokenPropertyList(String tokenType) {
    return getTokenTypeMap().get(tokenType);
  }

  public List<String> getRemoteRepositoryList() {
    return remoteRepositoryList;
  }

  public void replaceRemoteRepositoryList(List<String> list) {
    remoteRepositoryList.clear();
    remoteRepositoryList.addAll(list);
  }

  public Map<String, Map<GUID, LightSource>> getLightSourcesMap() {
    if (lightSourcesMap.isEmpty()) {
      initLightSourcesMap();
    }
    return lightSourcesMap;
  }

  public void replaceLightSourcesMap(Map<String, Map<GUID, LightSource>> map) {
    lightSourcesMap.clear();
    lightSourcesMap.putAll(map);
  }

  public Map<String, LookupTable> getLookupTableMap() {
    if (lookupTableMap.isEmpty()) {
      initLookupTableMap();
    }
    return lookupTableMap;
  }

  // TODO: This is for conversion from 1.3b19-1.3b20
  public void replaceLookupTableMap(Map<String, LookupTable> map) {
    lookupTableMap.clear();
    lookupTableMap.putAll(map);
  }

  public Map<String, BooleanTokenOverlay> getTokenStatesMap() {
    if (tokenStates.isEmpty()) {
      initTokenStatesMap();
    }
    return tokenStates;
  }

  public Map<String, BarTokenOverlay> getTokenBarsMap() {
    if (tokenBars.isEmpty()) {
      initTokenBarsMap();
    }
    return tokenBars;
  }

  private void init() {
    initLookupTableMap();
    initLightSourcesMap();
    initRemoteRepositoryList();
    initTokenTypeMap();
    initSightTypeMap();
    initTokenStatesMap();
    initTokenBarsMap();
    initCharacterSheetsMap();
  }

  private void initLookupTableMap() {
    lookupTableMap.clear();
  }

  private void initLightSourcesMap() {
    lightSourcesMap.clear();

    try {
      Map<String, List<LightSource>> map = LightSource.getDefaultLightSources();
      for (String key : map.keySet()) {
        Map<GUID, LightSource> lightSourceMap = new LinkedHashMap<>();
        for (LightSource source : map.get(key)) {
          lightSourceMap.put(source.getId(), source);
        }
        lightSourcesMap.put(key, lightSourceMap);
      }
    } catch (IOException ioe) {
      MapTool.showError("CampaignProperties.error.initLightSources", ioe);
    }
  }

  private void initRemoteRepositoryList() {
    remoteRepositoryList.clear();
  }

  public String getDefaultSightType() {
    synchronized (lock) {
      return defaultSightType;
    }
  }

  void setDefaultSightType(String defaultSightType) {
    synchronized (lock) {
      this.defaultSightType = defaultSightType;
    }
  }

  // @formatter:off
  private static final Object[][] starter =
      new Object[][] {
        // Sight Type Name					Dist		Mult		Arc		LtSrc		Shape				Scale
        {"Normal", 0.0, 1.0, 0, null, null, false},
        {"Lowlight", 0.0, 2.0, 0, null, null, false},
        {"Grid Vision", 0.0, 1.0, 0, null, ShapeType.GRID, true},
        {"Square Vision", 0.0, 1.0, 0, null, ShapeType.SQUARE, false},
        {"Normal Vision - Short Range", 10.0, 1.0, 0, null, ShapeType.CIRCLE, true},
        {"Conic Vision", 0.0, 1.0, 120, null, ShapeType.CONE, false},
        {"Darkvision", 0.0, 1.0, 0, null, null, true},
      };
  // @formatter:on

  private void initSightTypeMap() {
    sightTypeMap.clear();
    Arrays.stream(starter)
        .filter(row -> !row[0].equals("Darkvision"))
        .forEach(
            row -> {
              SightType st =
                  new SightType(
                      (String) row[0],
                      (Double) row[2],
                      (LightSource) row[4],
                      (ShapeType) row[5],
                      (Integer) row[3],
                      ((Double) row[1]).floatValue(),
                      0, // Always 0 here it seems
                      (boolean) row[6]);
              sightTypeMap.put((String) row[0], st);
            });

    try {
      final Map<String, List<LightSource>> defaultLightSources =
          LightSource.getDefaultLightSources();

      Arrays.stream(starter)
          .filter(row -> row[0].equals("Darkvision"))
          .findFirst()
          .ifPresent(
              row -> {
                SightType st =
                    new SightType(
                        (String) row[0],
                        (Double) row[2],
                        defaultLightSources.get("Generic").get(5),
                        (ShapeType) row[5],
                        (Integer) row[3],
                        ((Double) row[1]).floatValue(),
                        0, // Always 0 here it seems
                        (boolean) row[6]);
                sightTypeMap.put((String) row[0], st);
              });
    } catch (IOException e) {
      MapTool.showError("CampaignProperties.error.noGenericLight", e);
    }

    setDefaultSightType((String) starter[0][0]);
  }

  private void initTokenTypeMap() {
    tokenTypeMap.clear();

    List<TokenProperty> list = new ArrayList<>();
    list.add(new TokenProperty("Strength", "Str"));
    list.add(new TokenProperty("Dexterity", "Dex"));
    list.add(new TokenProperty("Constitution", "Con"));
    list.add(new TokenProperty("Intelligence", "Int"));
    list.add(new TokenProperty("Wisdom", "Wis"));
    list.add(new TokenProperty("Charisma", "Char"));
    list.add(new TokenProperty("HP", true, true, false));
    list.add(new TokenProperty("AC", true, true, false));
    list.add(new TokenProperty("Defense", "Def"));
    list.add(new TokenProperty("Movement", "Mov"));
    list.add(new TokenProperty("Elevation", "Elv", true, false, false, null));
    list.add(new TokenProperty("Description", "Des"));

    tokenTypeMap.put(DEFAULT_TOKEN_PROPERTY_TYPE, list);
  }

  private void initTokenStatesMap() {
    tokenStates.clear();
    tokenStates.put("Dead", (new XTokenOverlay("Dead", Color.RED, 5)));
    tokenStates.put("Disabled", (new XTokenOverlay("Disabled", Color.GRAY, 5)));
    tokenStates.put("Hidden", (new ShadedTokenOverlay("Hidden", Color.BLACK)));
    tokenStates.put("Prone", (new OTokenOverlay("Prone", Color.BLUE, 5)));
    tokenStates.put("Incapacitated", (new OTokenOverlay("Incapacitated", Color.RED, 5)));
    tokenStates.put("Other", (new ColorDotTokenOverlay("Other", Color.RED, null)));
    tokenStates.put("Other2", (new DiamondTokenOverlay("Other2", Color.RED, 5)));
    tokenStates.put("Other3", (new YieldTokenOverlay("Other3", Color.YELLOW, 5)));
    tokenStates.put("Other4", (new TriangleTokenOverlay("Other4", Color.MAGENTA, 5)));
  }

  private void initTokenBarsMap() {
    tokenBars.clear();
    tokenBars.put(
        "Health", new TwoToneBarTokenOverlay("Health", new Color(0x20b420), Color.BLACK, 6));
  }

  private void initCharacterSheetsMap() {
    characterSheets.clear();
    characterSheets.put("Basic", "net/rptools/maptool/client/ui/forms/basicCharacterSheet.xml");
  }

  public Set<MD5Key> getAllImageAssets() {
    Set<MD5Key> set = new HashSet<>();

    // Start with the table images
    for (LookupTable table : getLookupTableMap().values()) {
      set.addAll(table.getAllAssetIds());
    }

    // States have images as well
    for (AbstractTokenOverlay overlay : getTokenStatesMap().values()) {
      if (overlay instanceof ImageTokenOverlay) {
        set.add(((ImageTokenOverlay) overlay).getAssetId());
      }
    }

    // Bars
    for (BarTokenOverlay overlay : getTokenBarsMap().values()) {
      if (overlay instanceof SingleImageBarTokenOverlay) {
        set.add(((SingleImageBarTokenOverlay) overlay).getAssetId());
      } else if (overlay instanceof TwoImageBarTokenOverlay) {
        set.add(((TwoImageBarTokenOverlay) overlay).getTopAssetId());
        set.add(((TwoImageBarTokenOverlay) overlay).getBottomAssetId());
      } else if (overlay instanceof MultipleImageBarTokenOverlay) {
        set.addAll(Arrays.asList(((MultipleImageBarTokenOverlay) overlay).getAssetIds()));
      }
    }
    return set;
  }

  /** @return Getter for initiativeOwnerPermissions */
  public boolean isInitiativeOwnerPermissions() {
    synchronized (lock) {
      return initiativeOwnerPermissions;
    }
  }

  /** @param initiativeOwnerPermissions Setter for initiativeOwnerPermissions */
  public void setInitiativeOwnerPermissions(boolean initiativeOwnerPermissions) {
    synchronized (lock) {
      this.initiativeOwnerPermissions = initiativeOwnerPermissions;
    }
  }

  /** @return Getter for initiativeMovementLock */
  public boolean isInitiativeMovementLock() {
    synchronized (lock) {
      return initiativeMovementLock;
    }
  }

  /** @param initiativeMovementLock Setter for initiativeMovementLock */
  public void setInitiativeMovementLock(boolean initiativeMovementLock) {
    synchronized (lock) {
      this.initiativeMovementLock = initiativeMovementLock;
    }
  }

  /**
   * Getter for characterSheets. Only called by {@link Campaign#getCharacterSheets()} and that
   * function is never used elsewhere within MapTool. Yet. ;-)
   */
  public Map<String, String> getCharacterSheets() {
    if (characterSheets.isEmpty()) {
      initCharacterSheetsMap();
    }
    return characterSheets;
  }
}
