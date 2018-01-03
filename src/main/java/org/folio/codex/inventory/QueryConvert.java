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
import org.z3950.zing.cql.CQLSortNode;
import org.z3950.zing.cql.CQLTermNode;
import org.z3950.zing.cql.Modifier;
import org.z3950.zing.cql.ModifierSet;

public class QueryConvert {

  Logger logger = OkapiLogger.get();

  class IndexDescriptor {

    final String name;

    public IndexDescriptor(String name) {
      this.name = name;
    }
  }

  Map<String, IndexDescriptor> indexMaps = new HashMap<>();

  public QueryConvert() {
    indexMaps.put("cql.serverChoice", new IndexDescriptor("cql.serverChoice"));
    indexMaps.put("id", new IndexDescriptor("id"));
    indexMaps.put("title", new IndexDescriptor("title"));
    indexMaps.put("contributor", new IndexDescriptor("contributors"));
    indexMaps.put("publisher", new IndexDescriptor("publication"));
    indexMaps.put("subject", new IndexDescriptor("subject")); // TODO
    indexMaps.put("identifier", new IndexDescriptor("identifiers"));
    indexMaps.put("location", new IndexDescriptor("location")); // TODO
    indexMaps.put("resourceType", new IndexDescriptor("resourceType")); // TODO
    indexMaps.put("source", new IndexDescriptor("source"));
    indexMaps.put("language", new IndexDescriptor("languages"));
    indexMaps.put("classification", new IndexDescriptor("classification"));
  }

  CQLNode trav(CQLNode vn1) {
    if (vn1 instanceof CQLBooleanNode) {
      CQLBooleanNode n1 = (CQLBooleanNode) vn1;
      CQLBooleanNode n2 = null;
      CQLNode left = trav(n1.getLeftOperand());
      CQLNode right = trav(n1.getRightOperand());
      ModifierSet mSet = new ModifierSet(n1.getOperator().toString().toLowerCase());
      List<Modifier> mods = n1.getModifiers();
      for (Modifier m : mods) {
        mSet.addModifier(m.getType(), m.getComparison(), m.getValue());
      }
      switch (n1.getOperator()) {
        case AND:
          n2 = new CQLAndNode(left, right, mSet);
          break;
        case OR:
          n2 = new CQLOrNode(left, right, mSet);
          break;
        case NOT:
          n2 = new CQLNotNode(left, right, mSet);
          break;
        case PROX:
          n2 = new CQLProxNode(left, right, mSet);
          break;
      }
      return n2;
    } else if (vn1 instanceof CQLTermNode) {
      CQLTermNode n1 = (CQLTermNode) vn1;
      final String index1 = n1.getIndex();
      IndexDescriptor des = indexMaps.get(index1);
      if (des == null) {
        throw new IllegalArgumentException("Unsupported index " + index1);
      }
      final String index2 = des.name;
      return new CQLTermNode(index2, n1.getRelation(), n1.getTerm());
    } else if (vn1 instanceof CQLSortNode) {
      CQLSortNode n1 = (CQLSortNode) vn1;
      CQLSortNode n2 = new CQLSortNode(trav(n1.getSubtree()));
      List<ModifierSet> mods = n1.getSortIndexes();
      for (ModifierSet mSet : mods) {
        n2.addSortIndex(mSet);
      }
      return n2;
    } else if (vn1 instanceof CQLPrefixNode) {
      CQLPrefixNode n1 = (CQLPrefixNode) vn1;
      CQLPrefix prefix = n1.getPrefix();
      return new CQLPrefixNode(prefix.getName(), prefix.getIdentifier(), trav(n1.getSubtree()));
    } else {
      return vn1;
    }
  }

  CQLNode convert(CQLNode top) {
    return trav(top);
  }
}
