
package pxb.android.axml;


public class StripManifestAdapter extends AxmlVisitor {
    private static String Android_NS = "http://schemas.android.com/apk/res/android";

  

    public static class StripManifestNodeAdapter extends NodeVisitor {
        boolean strip;

        public StripManifestNodeAdapter(boolean strip) {
            super();
            this.strip = strip;
        }

        public StripManifestNodeAdapter(boolean strip, NodeVisitor nv) {
            super(nv);
            this.strip = strip;
        }

        @Override
        public void attr(String ns, String name, int resourceId, int type, Object obj) {
            if (resourceId != -1 && this.strip //
                    && resourceId != 0x101021b // versionCode
            ) {
                super.attr("", "", resourceId, type, obj);
            } else {
                super.attr(ns, name, resourceId, type, obj);
            }
        }

        @Override
        public NodeVisitor child(String ns, String name) {
            NodeVisitor nv = super.child(ns, name);
            if (nv != null) {
                if (name.equals("intent-filter")) {
                    nv = new StripManifestNodeAdapter(false, nv);
                } else {
                    nv = new StripManifestNodeAdapter(this.strip, nv);
                }
            }
            return nv;
        }
    }

    public StripManifestAdapter() {
        super();
    }

    public StripManifestAdapter(AxmlVisitor av) {
        super(av);
    }

    @Override
    public NodeVisitor first(String ns, String name) {
        NodeVisitor nv = super.first(ns, name);
        if (nv != null) {
            nv = new StripManifestNodeAdapter(true, nv);
        }
        return nv;
    }

    @Override
    public void ns(String prefix, String uri, int ln) {
        super.ns("", uri, ln);
    }

}
