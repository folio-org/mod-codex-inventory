package org.folio.codex.inventory;

import org.junit.Test;
import static org.junit.Assert.*;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParser;

public class QueryConvertTest {

  String conv(String input) {
    IdMaps idMaps = new IdMaps();
    CQLParser parser = new CQLParser(CQLParser.V1POINT2);
    try {
      CQLNode top = parser.parse(input);
      QueryConvert v = new QueryConvert(idMaps);
      CQLNode res = v.convert(top);
      if (res == null) {
        return "Error: null";
      } else {
        return res.toCQL();
      }
    } catch (Exception ex) {
      return "Error: " + ex.getMessage();
    }
  }

  @Test
  public void test() {
    assertEquals("Error: expected index or term, got EOF", conv("title=("));
    assertEquals("title = x", conv("title=x"));
    assertEquals("(a) and (b)", conv("a and b"));
    assertEquals("(a) or (b)", conv("a or b"));
    assertEquals("(a) not (b)", conv("a not b"));
    assertEquals("((a) and (b)) and (c)", conv("a and b and c"));
    assertEquals("Error: contributors", conv("contributors = x"));
    assertEquals("contributors = x", conv("contributor = x"));
    assertEquals("contributors == x", conv("contributor == x"));
    assertEquals("identifiers = 123*", conv("identifier=123"));
    assertEquals("identifiers =/type = isbn a", conv("identifier=/type=isbn 123"));
    assertEquals("Error: other", conv("identifier=/type=isbn/other=x 123"));
    assertEquals("Error: missing relation and/or value", conv("identifier=/type=isbn/other 123"));
    assertEquals("(identifiers =/type = isbn a) and (b)", conv("identifier=/type=isbn 123 and b"));
    // filters
    assertEquals("(p) and (languages = dk)", conv("p and language=dk"));
    assertEquals("languages = dk", conv("language=dk"));
    assertEquals("(location = held) and (languages = dk)", conv("location=held and language=dk"));
    assertEquals("((location = held) and (p)) and (languages = dk)", conv("location=held and p and language=dk"));
    assertEquals("Error: query has source clause only", conv("source=local"));
    assertEquals("Error: null", conv("source=kb"));
    assertEquals("Error: null", conv("source=all"));
    assertEquals("Error: null", conv("a and source=kb"));
    assertEquals("a", conv("a and source=local"));
    assertEquals("(a) and (b)", conv("a and (source=local) and b"));
    assertEquals("title = a* sortby title", conv("(title=a*) and source=(kb or local) sortby title"));
    assertEquals("title = a* sortby title", conv("(title=a*) and source=(1 or 2 or 3 or 4 or 5 or local) sortby title"));
    assertEquals("Error: null", conv("(title=a*) and source=(1 or 2 or 3 or 4 or 5) sortby title"));
    // sorting
    assertEquals("title = x sortby title", conv("title = x sortby title"));
    assertEquals("title = x sortby title/ascending", conv("title = x sortby title/ascending"));
    assertEquals("title = x sortby title/ascending", conv("title = x and source = local sortby title/ascending"));
    assertEquals("Error: null", conv("title = x and source = kb sortby title/ascending"));

    // prefix
    assertEquals(">\"info:srw/context-sets/1/dc-v1.1\" (title any fish)",
      conv("> \"info:srw/context-sets/1/dc-v1.1\" title any fish"));
    assertEquals(">dc=\"info:srw/context-sets/1/dc-v1.1\" (title any fish)",
      conv("> dc = \"info:srw/context-sets/1/dc-v1.1\" title any fish"));
    assertEquals(">dc=\"info:srw/context-sets/1/dc-v1.1\" (title any fish)",
      conv("> dc = \"info:srw/context-sets/1/dc-v1.1\" title any fish and source = local"));
    assertEquals("Error: null",
      conv("> dc = \"info:srw/context-sets/1/dc-v1.1\" title any fish and source = kb"));
  }
}
