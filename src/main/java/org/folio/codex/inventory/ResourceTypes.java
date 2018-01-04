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
  };
  List<InstanceMap> l = new LinkedList<>();

  ResourceTypes() {
    l.add(new InstanceMap("Spoken Record", Instance.Type.AUDIO));
    l.add(new InstanceMap("Books", Instance.Type.BOOKS));
    l.add(new InstanceMap("Computer Files", Instance.Type.DATABASES));
    l.add(new InstanceMap("eBooks", Instance.Type.EBOOKS));
    l.add(new InstanceMap("3-D Objects", Instance.Type.KITS));
    l.add(new InstanceMap("Kits", Instance.Type.KITS));
    l.add(new InstanceMap("Mixed Material", Instance.Type.KITS));
    l.add(new InstanceMap("Maps", Instance.Type.MAPS));
    l.add(new InstanceMap("Music (Audio)", Instance.Type.MUSIC));
    l.add(new InstanceMap("Music (MSS)", Instance.Type.MUSIC));
    l.add(new InstanceMap("Music (Scores)", Instance.Type.MUSIC));
    l.add(new InstanceMap("Serials", Instance.Type.PERIODICALS));
    l.add(new InstanceMap("Charts Posters", Instance.Type.POSTERS));
    l.add(new InstanceMap("Theses", Instance.Type.THESISANDDISSERTATION));
    l.add(new InstanceMap("Error", Instance.Type.UNSPECIFIED));
    l.add(new InstanceMap("Videorecording", Instance.Type.VIDEO));
    l.add(new InstanceMap("Web Resources", Instance.Type.WEBRESOURCES));
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
