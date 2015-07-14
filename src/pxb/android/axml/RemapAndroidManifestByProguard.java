package pxb.android.axml;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pxb.android.axml.Axml.Node;
import pxb.android.axml.AxmlVisitor.NodeVisitor;

public class RemapAndroidManifestByProguard {

    /**
     * @param args
     * @throws IOException
     */

    public static class RemapNodeVisitor extends NodeVisitor {
        public static class Config {
            public String pkg;

            public Config(Map<String, String> classMap) {
                super();
                this.classMap = classMap;
            }

            public Map<String, String> classMap;
        }

        final Config cfg;
        String ns;
        String name;
        static Set<String> nameTags = new HashSet<String>(Arrays.asList("application", "activity", "receiver",
                "service"));

        public RemapNodeVisitor(Config cfg, String ns, String name, NodeVisitor nv) {
            super(nv);
            this.ns = ns;
            this.name = name;
            this.cfg = cfg;
        }

        @Override
        public NodeVisitor child(String ns, String name) {
            return new RemapNodeVisitor(cfg, ns, name, super.child(ns, name));
        }

        @Override
        public void attr(String ns, String name, int resourceId, int type, Object obj) {
            if (this.ns == null || this.ns.length() == 0) {
                Config cfg = this.cfg;
                if (this.name.equals("manifest")) {
                    if ("package".equals(name) && (ns == null || ns.length() == 0)) {
                        cfg.pkg = obj.toString();
                    }
                }
                if (resourceId == 0x1010003 && nameTags.contains(this.name)) {
                    String clz = obj.toString();
                    String key = cfg.pkg + clz;
                    boolean find = false;
                    if (cfg.classMap.containsKey(key)) {
                        obj = cfg.classMap.get(key);
                        find = true;
                    }
                    if (!find) {
                        key = clz;
                        if (cfg.classMap.containsKey(key)) {
                            obj = cfg.classMap.get(key);
                            find = true;
                        }
                    }
                    if (!find) {
                        key = cfg.pkg + "." + clz;
                        if (cfg.classMap.containsKey(key)) {
                            obj = cfg.classMap.get(key);
                            find = true;
                        }
                    }
                }
            }
            super.attr(ns, name, resourceId, type, obj);
        }
    }

    static String getPackage(Axml axml) {
        for (Node n : axml.firsts) {
            if ("manifest".equals(n.name) && (n.ns == null || n.ns.length() == 0)) {
                for (Node.Attr a : n.attrs) {
                    if ("package".equals(a.name) && (a.ns == null || a.ns.length() == 0)) {
                        return a.value.toString();
                    }
                }
                return null;
            }
        }
        return null;
    }
}
