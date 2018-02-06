package org.folio.codex.inventory;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.util.List;
import org.folio.rest.jaxrs.model.Diagnostic;
import org.folio.rest.jaxrs.model.ResultInfo;
import org.folio.rest.jaxrs.model.InstanceCollection;

public class DiagnosticUtil {
  private DiagnosticUtil() {
    throw new IllegalStateException("DiagnosticUtil");
  }

  private static final Logger logger = LoggerFactory.getLogger("codex.inventory");

  public static void add(InstanceCollection col, String code, String message) {
    Diagnostic d = new Diagnostic();
    d.setSource("local");
    d.setCode(code);
    d.setMessage(message);
    ResultInfo resultInfo = col.getResultInfo();
    List<Diagnostic> dl = resultInfo.getDiagnostics();
    logger.warn("Add Diagnostic " + code + " " + message);
    dl.add(d);
  }
}
