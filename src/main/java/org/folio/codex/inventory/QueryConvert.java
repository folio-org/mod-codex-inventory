package org.folio.codex.inventory;

import io.vertx.core.logging.Logger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.folio.okapi.common.OkapiLogger;
import org.z3950.zing.cql.CQLAndNode;
import org.z3950.zing.cql.CQLBooleanNode;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLNotNode;
import org.z3950.zing.cql.CQLOrNode;
import org.z3950.zing.cql.CQLPrefix;
import org.z3950.zing.cql.CQLPrefixNode;
import org.z3950.zing.cql.CQLProxNode;
import org.z3950.zing.cql.CQLRelation;
import org.z3950.zing.cql.CQLSortNode;
import org.z3950.zing.cql.CQLTermNode;
import org.z3950.zing.cql.Modifier;
import org.z3950.zing.cql.ModifierSet;
import org.z3950.zing.cql.UnknownIndexException;
import org.z3950.zing.cql.UnknownRelationException;
import org.z3950.zing.cql.UnknownRelationModifierException;

public class QueryConvert {

  Logger logger = OkapiLogger.get();

  class IndexDescriptor {

    final String name;
    final boolean filter;

    public IndexDescriptor(String name, boolean filter) {
      this.name = name;
      this.filter = filter;
    }
  }

  Map<String, IndexDescriptor> indexMaps = new HashMap<>();
  IdMaps idMaps;

  public QueryConvert(IdMaps idMaps) {
    this.idMaps = idMaps;
    indexMaps.put("cql.serverChoice", new IndexDescriptor("cql.serverChoice", false));
    indexMaps.put("id", new IndexDescriptor("id", false));
    indexMaps.put("title", new IndexDescriptor("title", false));
    indexMaps.put("contributor", new IndexDescriptor("contributors", false));
    indexMaps.put("publisher", new IndexDescriptor("publication", false));
    indexMaps.put("subject", new IndexDescriptor("subject", false)); // TODO
    indexMaps.put("identifier", new IndexDescriptor("identifiers", false));
    indexMaps.put("location", new IndexDescriptor("location", true)); // TODO
    indexMaps.put("resourceType", new IndexDescriptor("resourceType", true));
    indexMaps.put("source", new IndexDescriptor("source", true));
    indexMaps.put("language", new IndexDescriptor("languages", true));
    indexMaps.put("classification", new IndexDescriptor("classification", true));
  }

  class TravRes {
    CQLNode node;
    boolean filter;
    boolean regular;
    String source;
  }

  TravRes trav(CQLNode vn1) throws UnknownRelationModifierException, UnknownRelationException, UnknownIndexException {
    if (vn1 instanceof CQLBooleanNode) {
      TravRes res = new TravRes();

      CQLBooleanNode n1 = (CQLBooleanNode) vn1;
      TravRes n2 = new TravRes();
      TravRes left = trav(n1.getLeftOperand());
      TravRes right = trav(n1.getRightOperand());
      n2.filter = left.filter || right.filter;
      n2.regular = left.regular || right.regular;
      if (left.source != null) {
        n2.source = left.source;
      } else if (right.source != null) {
        n2.source = right.source;
      }
      ModifierSet mSet = new ModifierSet(n1.getOperator().toString().toLowerCase());
      List<Modifier> mods = n1.getModifiers();
      for (Modifier m : mods) {
        mSet.addModifier(m.getType(), m.getComparison(), m.getValue());
      }
      switch (n1.getOperator()) {
        case AND:
          if (left.node == null) {
            n2.node = right.node;
          } else if (right.node == null) {
            n2.node = left.node;
          } else {
            n2.node = new CQLAndNode(left.node, right.node, mSet);
          }
          break;
        case OR:
          if (n2.filter) {
            throw new IllegalArgumentException("unsupported OR for filter");
          }
          n2.node = new CQLOrNode(left.node, right.node, mSet);
          break;
        case NOT:
          if (n2.filter) {
            throw new IllegalArgumentException("unsupported NOT for filter");
          }
          n2.node = new CQLNotNode(left.node, right.node, mSet);
          break;
        case PROX:
          if (n2.filter) {
            throw new IllegalArgumentException("unsupported PROX for filter");
          }
          n2.node = new CQLProxNode(left.node, right.node, mSet);
          break;
      }
      return n2;
    } else if (vn1 instanceof CQLTermNode) {
      TravRes n2 = new TravRes();
      CQLTermNode n1 = (CQLTermNode) vn1;
      final String term1 = n1.getTerm();
      final String index1 = n1.getIndex();
      IndexDescriptor des = indexMaps.get(index1);
      if (des == null) {
        throw new UnknownIndexException(index1);
      }
      final String index2 = des.name;
      CQLRelation rel = n1.getRelation();
      n2.filter = des.filter;
      if (n2.filter) {
        if (!rel.getBase().equals("=")) {
          throw new UnknownRelationException(rel.getBase());
        }
      }
      n2.regular = !des.filter;
      n2.node = null;
      List<Modifier> mods = rel.getModifiers();
      if ("identifier".equals(index1) && !mods.isEmpty()) {
        logger.info("identifier BSON handling");
        for (Modifier mod : mods) {
          if (mod.getValue() == null || mod.getComparison() == null) {
            throw new UnknownRelationModifierException("missing relation and/or value");
          }
          if (!"=".equals(mod.getComparison())) {
            throw new UnknownRelationException(mod.getComparison());
          }
          if (!"type".equals(mod.getType())) {
            throw new UnknownRelationModifierException(mod.getType());
          }
          for (Map.Entry<String, String> entry : idMaps.identifierTypeMap.entrySet()) {
            if (mod.getValue().equalsIgnoreCase(entry.getValue())) {
              final String sq = "\"";
              final String bsonTerm = "*" + sq + "value" + sq + ": " + sq + term1 + sq
                + ", " + sq + "identifierTypeId" + sq + ": " + sq + entry.getKey() + sq + "*";
              CQLRelation relEqEq = new CQLRelation("==");
              CQLTermNode n = new CQLTermNode(index2, relEqEq, bsonTerm);
              if (n2.node == null) {
                n2.node = n;
              } else {
                ModifierSet mSet = new ModifierSet("or");
                n2.node = new CQLOrNode(n2.node, n, mSet);
              }
            }
          }
        }
        if (n2.node == null) {
          n2.node = new CQLTermNode(index2, rel, "a");
        }
      } else if ("resourceType".equals(index1)) {
        ResourceTypes rt = new ResourceTypes();
        List<String> names = rt.toInvName(term1);
        for (String name : names) {
          for (Map.Entry<String, String> entry : idMaps.instanceTypeMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
              CQLTermNode n = new CQLTermNode("instanceTypeId", rel, entry.getKey());
              if (n2.node == null) {
                n2.node = n;
              } else {
                ModifierSet mSet = new ModifierSet("or");
                n2.node = new CQLOrNode(n2.node, n, mSet);
              }
            }
          }
        }
        if (n2.node == null) {
          n2.node = new CQLTermNode("instanceTypeId", rel, "a");
        }
      } else if ("source".equals(index1)) {
        n2.source = term1;
      } else {
        String suffix = "";
        if (term1.matches("^\\d+$")) {
          suffix = "*";
        }
        n2.node = new CQLTermNode(index2, rel, term1 + suffix);
      }
      return n2;
    } else if (vn1 instanceof CQLSortNode) {
      CQLSortNode n1 = (CQLSortNode) vn1;
      TravRes n2 = trav(n1.getSubtree());
      CQLSortNode sn = new CQLSortNode(n2.node);
      n2.node = sn;
      List<ModifierSet> mods = n1.getSortIndexes();
      for (ModifierSet mSet : mods) {
        sn.addSortIndex(mSet);
      }
      return n2;
    } else if (vn1 instanceof CQLPrefixNode) {
      CQLPrefixNode n1 = (CQLPrefixNode) vn1;
      TravRes n2 = trav(n1.getSubtree());
      CQLPrefix prefix = n1.getPrefix();
      n2.node = new CQLPrefixNode(prefix.getName(), prefix.getIdentifier(), n2.node);
      return n2;
    } else {
      TravRes n2 = new TravRes();
      n2.filter = false;
      n2.regular = false;
      n2.node = vn1;
      return n2;
    }
  }

  public CQLNode convert(CQLNode top) throws UnknownRelationModifierException, UnknownRelationException, UnknownIndexException {
    TravRes res = trav(top);
    if (!res.regular) {
      throw new IllegalArgumentException("missing non-filter field search");
    }
    if (res.source == null || "all".equals(res.source) || "local".equals(res.source)) {
      return res.node;
    } else {
      return null;
    }
  }
}
