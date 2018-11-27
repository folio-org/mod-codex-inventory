package org.folio.codex.inventory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.folio.okapi.common.OkapiLogger;
import org.folio.rest.jaxrs.model.Contributor;
import org.folio.rest.jaxrs.model.Identifier;
import org.folio.rest.jaxrs.model.Instance;
import org.folio.rest.jaxrs.model.InstanceCollection;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;

public class InstanceConvert {

  static Logger logger = OkapiLogger.get();

  private InstanceConvert() {
    throw new IllegalStateException("Instance");
  }

  public static void invToCollection(JsonObject j, InstanceCollection col,
    IdMaps idMaps, String source) {

    JsonArray a = j.getJsonArray("instances");
    if (a == null) {
      throw (new IllegalArgumentException("instances missing"));
    }
    List<Instance> l = new LinkedList<>();
    for (int i = 0; i < a.size(); i++) {
      Instance instance = new Instance();
      invToCodex(a.getJsonObject(i), instance, idMaps, source);
      l.add(instance);
    }
    col.setInstances(l);
    Integer cnt = j.getInteger("totalRecords");
    if (cnt == null) {
      throw (new IllegalArgumentException("totalRecords missing"));
    }
    col.getResultInfo().setTotalRecords(cnt);
  }

  public static void invToCodex(JsonObject j, Instance instance,
    IdMaps idMaps, String source) {

    logger.info("invToCodex\n" + j.encodePrettily());
    mapId(j, instance);
    mapTitle(j, instance);
    mapAlternativeTitle(j, instance);
    mapSeries(j, instance);
    mapContributors(j, idMaps, instance);
    mapPublication(j, instance);
    mapInstanceTypeId(j, idMaps, instance);
    mapInstanceFormatIds(j, idMaps, instance);
    mapIdentifiers(j, idMaps, instance);
    mapSource(instance, source);
    mapLanguages(j, instance);
    mapEditions(j, instance);
  }

  private static void mapEditions(JsonObject j, Instance instance) {
    JsonArray ar = j.getJsonArray("editions");
    if (ar != null && ar.size() > 0) {
      instance.setVersion(ar.getString(0));
    }
  }

  private static void mapLanguages(JsonObject j, Instance instance) {
    JsonArray ar = j.getJsonArray("languages");
    if (ar != null) {
      List<String> il = new LinkedList<>();
      for (int i = 0; i < ar.size(); i++) {
        final String lang = ar.getString(i);
        il.add(lang);
      }
      instance.setLanguage(il);
    }
  }

  private static void mapSource(Instance instance, String source) {
    // required in codex
    instance.setSource(source);
  }

  private static void mapIdentifiers(JsonObject j, IdMaps idMaps, Instance instance) {
    JsonArray ar = j.getJsonArray("identifiers");
    if (ar != null) {
      Set<Identifier> il = new HashSet<>();
      for (int i = 0; i < ar.size(); i++) {
        JsonObject ji = ar.getJsonObject(i);
        final String type
          = idMaps.getIdentifierTypeMap().get(ji.getString("identifierTypeId"));
        if (type != null) {
          Identifier identifier = new Identifier();
          identifier.setType(type);
          identifier.setValue(ji.getString("value"));
          il.add(identifier);
        }
      }
      instance.setIdentifier(il);
    }
  }

  private static void mapInstanceFormatIds(JsonObject j, IdMaps idMaps, Instance instance) {
    JsonArray ar = j.getJsonArray("instanceFormatIds");
    if (ar != null && ar.size() > 0) {
      String id = ar.getString(0);
      final String format = idMaps.getInstanceFormatMap().get(id);
      if (format == null) {
        throw (new IllegalArgumentException("instanceFormatId " + id + " does not exist"));
      }
      instance.setFormat(format);
    }
  }

  private static void mapInstanceTypeId(JsonObject j, IdMaps idMaps, Instance instance) {
    final String id = j.getString("instanceTypeId");
    if (id == null) {
      throw (new IllegalArgumentException("instanceTypeId missing"));
    }
    final String name = idMaps.getInstanceTypeMap().get(id);
    if (name == null) {
      throw (new IllegalArgumentException("instanceTypeId " + id + " does not exist"));
    }

    ResourceTypes rt = new ResourceTypes();
    instance.setType(rt.toType(name));
  }

  private static void mapPublication(JsonObject j, Instance instance) {
    JsonArray ar = j.getJsonArray("publication");
    if (ar != null && ar.size() > 0) {
      JsonObject ji = ar.getJsonObject(0);
      final String publisher = ji.getString("publisher");
      if (publisher != null) {
        instance.setPublisher(publisher);
      }
      final String date = ji.getString("dateOfPublication");
      if (date != null) {
        instance.setDate(date);
      }
    }
  }

  private static void mapContributors(JsonObject j, IdMaps idMaps, Instance instance) {
    JsonArray ar = j.getJsonArray("contributors");
    if (ar != null) {
      Set<Contributor> cl = new HashSet<>();
      for (int i = 0; i < ar.size(); i++) {
        JsonObject ji = ar.getJsonObject(i);
        final String type
          = idMaps.getContributorNameTypeIdMap().get(ji.getString("contributorNameTypeId"));
        if (type != null) {
          Contributor c = new Contributor();
          c.setName(ji.getString("name"));
          c.setType(type);
          cl.add(c);
        }
      }
      instance.setContributor(cl);
    }
  }

  private static void mapSeries(JsonObject j, Instance instance) {
    JsonArray ar = j.getJsonArray("series");
    if (ar != null && ar.size() > 0) {
      instance.setSeries(ar.getString(0));
    }
  }

  private static void mapAlternativeTitle(JsonObject j, Instance instance) {
    JsonArray ar = j.getJsonArray("alternativeTitles");
    if (ar != null && ar.size() > 0) {
      instance.setAltTitle(ar.getJsonObject(0).getString("alternativeTitle"));
    }
  }

  private static void mapTitle(JsonObject j, Instance instance) {
    final String title = j.getString("title");
    if (title == null) {
      throw (new IllegalArgumentException("title missing"));
    }
    instance.setTitle(title);
  }

  private static void mapId(JsonObject j, Instance instance) {
    final String id = j.getString("id");
    if (id == null) {
      throw (new IllegalArgumentException("id missing"));
    }
    instance.setId(id);
  }

}
