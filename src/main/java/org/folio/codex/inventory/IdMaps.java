package org.folio.codex.inventory;

import java.util.LinkedHashMap;
import java.util.Map;

public class IdMaps {

  private final Map<String, String> contributorNameTypeIdMap = new LinkedHashMap<>();

  public Map<String, String> getContributorNameTypeIdMap() {
    return contributorNameTypeIdMap;
  }

  private final Map<String, String> instanceTypeMap = new LinkedHashMap<>();

  public Map<String, String> getInstanceTypeMap() {
    return instanceTypeMap;
  }

  private final Map<String, String> instanceFormatMap = new LinkedHashMap<>();

  public Map<String, String> getInstanceFormatMap() {
    return instanceFormatMap;
  }

  private final Map<String, String> identifierTypeMap = new LinkedHashMap<>();

  public Map<String, String> getIdentifierTypeMap() {
    return identifierTypeMap;
  }

  private final Map<String, String> shelfLocationMap = new LinkedHashMap<>();

  public Map<String, String> getShelfLocationMap() {
    return shelfLocationMap;
  }

}
