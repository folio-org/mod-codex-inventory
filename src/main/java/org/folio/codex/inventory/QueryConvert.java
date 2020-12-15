package org.folio.codex.inventory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.folio.okapi.common.CQLUtil;
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

@java.lang.SuppressWarnings({"squid:S1192"})
public class QueryConvert {

  static class IndexDescriptor {
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
    indexMaps.put("subject", new IndexDescriptor("subjects", false));
    indexMaps.put("identifier", new IndexDescriptor("identifiers", false));
    indexMaps.put("location", new IndexDescriptor("holdingsRecords.permanentLocationId", true));
    indexMaps.put("resourceType", new IndexDescriptor("instanceTypeId", true));
    indexMaps.put("source", new IndexDescriptor("source", true));
    indexMaps.put("language", new IndexDescriptor("languages", true));
    indexMaps.put("classification", new IndexDescriptor("classification", true));
  }

  CQLNode trav(CQLNode vn1) throws UnknownRelationModifierException,
    UnknownRelationException, UnknownIndexException {

    if (vn1 instanceof CQLBooleanNode) {
      return travBoolean((CQLBooleanNode) vn1);
    } else if (vn1 instanceof CQLTermNode) {
      return travTerm((CQLTermNode) vn1);
    } else if (vn1 instanceof CQLSortNode) {
      CQLSortNode n1 = (CQLSortNode) vn1;
      CQLSortNode sn = new CQLSortNode(trav(n1.getSubtree()));
      List<ModifierSet> mods = n1.getSortIndexes();
      for (ModifierSet mSet : mods) {
        sn.addSortIndex(mSet);
      }
      return sn;
    } else if (vn1 instanceof CQLPrefixNode) {
      CQLPrefixNode n1 = (CQLPrefixNode) vn1;
      CQLPrefix prefix = n1.getPrefix();
      return new CQLPrefixNode(prefix.getName(), prefix.getIdentifier(), n1.getSubtree());
    } else {
      throw new IllegalArgumentException("Unhandled CQLNode type in QueryConvert.trav");
    }
  }

  private CQLNode travTerm(CQLTermNode n1) throws UnknownRelationException,
    UnknownRelationModifierException, UnknownIndexException {

    final String term1 = n1.getTerm();
    final String index1 = n1.getIndex();
    IndexDescriptor des = indexMaps.get(index1);
    if (des == null) {
      throw new UnknownIndexException(index1);
    }
    final String index2 = des.name;
    CQLRelation rel = n1.getRelation();
    if (des.filter && !rel.getBase().equals("=")) {
      throw new UnknownRelationException(rel.getBase());
    }
    CQLNode n2 = null;
    if ("identifier".equals(index1) && !rel.getModifiers().isEmpty()) {
      n2 = travIdentifier(term1, index2, n2, rel);
    } else if ("resourceType".equals(index1)) {
      ResourceTypes rt = new ResourceTypes();
      n2 = travFieldMap(rt.toInvName(term1), index2, rel, n2, idMaps.getInstanceTypeMap());
    } else if ("location".equals(index1)) {
      List<String> names = new LinkedList<>();
      names.add(term1);
      n2 = travFieldMap(names, index2, rel, n2, idMaps.getShelfLocationMap());
    } else {
      String suffix = "";
      if (term1.matches("^\\d+$")) {
        suffix = "*";
      }
      n2 = new CQLTermNode(index2, rel, term1 + suffix);
    }
    return n2;
  }

  private CQLNode travFieldMap(List<String> names, String index2, CQLRelation rel, CQLNode n2, Map<String, String> map) {
    for (String name : names) {
      for (Map.Entry<String, String> entry : map.entrySet()) {
        if (entry.getValue().equalsIgnoreCase(name)) {
          CQLTermNode n = new CQLTermNode(index2, rel, entry.getKey());
          if (n2 == null) {
            n2 = n;
          } else {
            ModifierSet mSet = new ModifierSet("or");
            n2 = new CQLOrNode(n2, n, mSet);
          }
        }
      }
    }
    if (n2 == null) {
      n2 = new CQLTermNode(index2, rel, "a");
    }
    return n2;
  }

  private CQLNode travIdentifier(String term1, String index2, CQLNode n2,
    CQLRelation rel) throws UnknownRelationModifierException, UnknownRelationException {

    List<Modifier> mods = rel.getModifiers();
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
      for (Map.Entry<String, String> entry : idMaps.getIdentifierTypeMap().entrySet()) {
        if (mod.getValue().equalsIgnoreCase(entry.getValue())) {
          final String sq = "\"";
          final String bsonTerm = "*" + sq + "value" + sq + ": " + sq + term1 + sq
            + ", " + sq + "identifierTypeId" + sq + ": " + sq + entry.getKey() + sq + "*";
          CQLRelation relEqEq = new CQLRelation("==");
          CQLTermNode n = new CQLTermNode(index2, relEqEq, bsonTerm);
          if (n2 == null) {
            n2 = n;
          } else {
            ModifierSet mSet = new ModifierSet("or");
            n2 = new CQLOrNode(n2, n, mSet);
          }
        }
      }
    }
    if (n2 == null) {
      n2 = new CQLTermNode(index2, rel, "a");
    }
    return n2;
  }

  private CQLNode travBoolean(CQLBooleanNode n1) throws UnknownIndexException,
    UnknownRelationModifierException, UnknownRelationException {

    CQLNode left = trav(n1.getLeftOperand());
    CQLNode right = trav(n1.getRightOperand());
    ModifierSet mSet = new ModifierSet(n1.getOperator().toString().toLowerCase());
    List<Modifier> mods = n1.getModifiers();
    for (Modifier m : mods) {
      mSet.addModifier(m.getType(), m.getComparison(), m.getValue());
    }
    switch (n1.getOperator()) {
      case AND:
        return new CQLAndNode(left, right, mSet);
      case OR:
        return new CQLOrNode(left, right, mSet);
      case NOT:
        return new CQLNotNode(left, right, mSet);
      case PROX:
        return new CQLProxNode(left, right, mSet);
      default:
        throw new IllegalArgumentException("Unhandled CQLBooleanNode type in QueryConvert.trav");
    }
  }

  public CQLNode convert(CQLNode top) throws UnknownRelationModifierException,
    UnknownRelationException, UnknownIndexException {

    CQLRelation rel = new CQLRelation("=");
    Comparator<CQLTermNode> f1 = (CQLTermNode n1, CQLTermNode n2) -> {
      if (n1.getIndex().equals(n2.getIndex()) && !n1.getTerm().equals(n2.getTerm())) {
        return -1;
      }
      return 0;
    };
    Comparator<CQLTermNode> f2 = (CQLTermNode n1, CQLTermNode n2)
      -> n1.getIndex().equals(n2.getIndex()) ? 0 : -1;

    CQLTermNode source = new CQLTermNode("source", rel, "local");
    if (!CQLUtil.eval(top, source, f1)) {
      return null;
    }
    CQLNode n2 = CQLUtil.reducer(top, source, f2);
    if (n2 == null) {
      throw new IllegalArgumentException("query has source clause only");
    }
    CQLTermNode extSelected = new CQLTermNode("ext.selected", rel, null);
    n2 = CQLUtil.reducer(n2, extSelected, f2);
    if (n2 == null) {
      throw new IllegalArgumentException("query has ext.selected clause only");
    }
    return trav(n2);
  }
}
