package org.folio.codex.inventory;

import java.util.LinkedList;
import java.util.List;
import org.folio.rest.jaxrs.model.Instance;

public class ResourceTypes {

  class InstanceMap {

    String invName;
    Instance.Type t;

    InstanceMap(String name, Instance.Type t) {
      this.invName = name;
      this.t = t;
    }
  }
  List<InstanceMap> l = new LinkedList<>();

  ResourceTypes() {
    l.add(new InstanceMap("cartographic dataset", Instance.Type.MAPS));
    l.add(new InstanceMap("cartographic image", Instance.Type.MAPS));
    l.add(new InstanceMap("cartographic moving image", Instance.Type.MAPS));
    l.add(new InstanceMap("cartographic tactile image", Instance.Type.MAPS));
    l.add(new InstanceMap("cartographic tactile three-dimensional form", Instance.Type.MAPS));
    l.add(new InstanceMap("cartographic three-dimensional form", Instance.Type.MAPS));
    l.add(new InstanceMap("computer dataset", Instance.Type.DATABASES));
    l.add(new InstanceMap("computer program", Instance.Type.DATABASES));
    l.add(new InstanceMap("notated movement", Instance.Type.UNSPECIFIED));
    l.add(new InstanceMap("tactile notated music", Instance.Type.MUSIC));
    l.add(new InstanceMap("notated music", Instance.Type.MUSIC));
    l.add(new InstanceMap("performed music", Instance.Type.MUSIC));
    l.add(new InstanceMap("sounds", Instance.Type.AUDIO));
    l.add(new InstanceMap("spoken word", Instance.Type.AUDIO));
    l.add(new InstanceMap("still image", Instance.Type.POSTERS));
    l.add(new InstanceMap("tactile image", Instance.Type.POSTERS));
    l.add(new InstanceMap("tactile notated movement", Instance.Type.UNSPECIFIED));
    l.add(new InstanceMap("tactile text", Instance.Type.BOOKS));
    l.add(new InstanceMap("taxtile three-dimensional form", Instance.Type.KITS));
    l.add(new InstanceMap("text", Instance.Type.BOOKS));
    l.add(new InstanceMap("three-dimensional form", Instance.Type.KITS));
    l.add(new InstanceMap("three-dimensional moving image", Instance.Type.VIDEO));
    l.add(new InstanceMap("two dimensional moving image", Instance.Type.VIDEO));
    l.add(new InstanceMap("other", Instance.Type.UNSPECIFIED));
    l.add(new InstanceMap("unspecified", Instance.Type.UNSPECIFIED));
  }

  Instance.Type toType(String n) {
    for (InstanceMap m : l) {
      if (n.equals(m.invName)) {
        return m.t;
      }
    }
    return Instance.Type.UNSPECIFIED;
  }

  List<String> toInvName(String n) {
    List<String> res = new LinkedList<>();
    for (InstanceMap m : l) {
      if (m.t.toString().equalsIgnoreCase(n)) {
        res.add(m.invName);
      }
    }
    return res;
  }
}
