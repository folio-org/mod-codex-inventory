package org.folio.codex.inventory;

import org.junit.Test;
import static org.junit.Assert.*;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParser;

public class QueryConvertTest {

  String conv(String input) {
    CQLParser parser = new CQLParser(CQLParser.V1POINT2);
    try {
      CQLNode top = parser.parse(input);
      QueryConvert v = new QueryConvert();
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
    assertEquals("title = x", conv("title=x"));
    assertEquals("(a) and (b)", conv("a and b"));
    assertEquals("(a) or (b)", conv("a or b"));
    assertEquals("(a) not (b)", conv("a not b"));
    assertEquals("((a) and (b)) and (c)", conv("a and b and c"));
    assertEquals("contributors = x", conv("contributor = x"));
    assertEquals("contributors == x", conv("contributor == x"));
    assertEquals("identifiers =/type = isbn 123", conv("identifier=/type=isbn 123"));
    assertEquals("identifiers =/type = isbn/other = x 123", conv("identifier=/type=isbn/other=x 123"));
    assertEquals("identifiers =/type = isbn/other 123", conv("identifier=/type=isbn/other 123"));
    assertEquals("(identifiers =/type = isbn/other = x 123) and (b)", conv("identifier=/type=isbn/other=x 123 and b"));
    assertEquals("title = x sortby title", conv("title = x sortby title"));
    assertEquals("title = x sortby title/ascending", conv("title = x sortby title/ascending"));
    assertEquals(">\"info:srw/context-sets/1/dc-v1.1\" (title any fish)",
      conv("> \"info:srw/context-sets/1/dc-v1.1\" title any fish"));
    assertEquals(">dc=\"info:srw/context-sets/1/dc-v1.1\" (title any fish)",
      conv("> dc = \"info:srw/context-sets/1/dc-v1.1\" title any fish"));
  }
}
