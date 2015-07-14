import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.AxmlWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaokun on 15/7/1.
 */
public class AxmlParse {

    public static final int ATTR_NAME = 0x01010003;
    public static final int ATTR_VALUE = 0x01010024;



    private String mPackageName;
    private String mApplicationName;

    private List<String> mComps  = new ArrayList<>();

    private byte[] mAxml;

    public AxmlParse(byte[] axml) throws IOException {
        mAxml = axml;
        parseAxml();
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getApplicationName() {
        return mApplicationName;
    }



    public List<String> getmComps(){
        return mComps;

    }


    private void parseAxml() throws IOException {
        AxmlReader reader = new AxmlReader(mAxml);
        AxmlWriter writer = new AxmlWriter();

        reader.accept(new AxmlVisitor(writer) {

            @Override
            public NodeVisitor first(String ns, String name) {
                NodeVisitor nv = super.first(ns, name);

                return new NodeVisitor(nv) {

                    @Override
                    public void attr(String ns, String name, int resourceId, int type, Object obj) {
                        super.attr(ns, name, resourceId, type, obj);

                        if (name.equals("package")) {
                            mPackageName = (String) obj;
                        }
                    }

                    @Override
                    public NodeVisitor child(String ns, String name) {
                        if (name.equals("application")) { // application
                            return new NodeVisitor(nv) {
                                public void attr(String ns, String name, int resourceId, int type, Object obj) {
                                    if (resourceId == ATTR_NAME) { // android:name
                                        mApplicationName = (String) obj;
                                    }
                                }

                                @Override
                                public NodeVisitor child(String ns, String name) {

                                    if (name.equals("receiver")) {
                                        return new NodeVisitor(super.child(ns, name)) {
                                            private String mName = null;
                                            private String mValue = null;

                                            @Override
                                            public void attr(String ns, String name, int resourceId, int type, Object obj) {
                                                super.attr(ns, name, resourceId, type, obj);
                                                if (name.equals("name")) {
                                                    mValue = (String) obj;
                                                    mComps.add(mValue);
                                                }
                                            }


                                        };
                                    }

                                    if (name.equals("service")) {
                                        return new NodeVisitor(super.child(ns, name)) {
                                            private String mName = null;
                                            private String mValue = null;

                                            @Override
                                            public void attr(String ns, String name, int resourceId, int type, Object obj) {
                                                super.attr(ns, name, resourceId, type, obj);
                                                if (name.equals("name")) {
                                                    mValue = (String) obj;
                                                    mComps.add(mValue);
                                                }
                                            }


                                        };
                                    }

                                    return super.child(ns, name);
                                }
                            };
                        }

                        return super.child(ns, name);
                    }
                };
            }
        });


    }



}
