import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.AxmlWriter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by gaokun on 15/7/14.
 */
public class UnkonwnShell {

    byte[] amXml;
    byte[] dexPath;
    String packageName;
    double guardValue;


    public  UnkonwnShell(byte[] amData,byte[] dexData,double guardKey){
        this.amXml = amData;
        this.dexPath = dexData;
        this.guardValue = guardKey ;

    }



    public  boolean isShell() throws IOException {
        boolean isShellTag = false ;

        AxmlReader amReader = new AxmlReader(amXml);

        final ArrayList<String> activityClazz = new ArrayList<String>();

        if (amReader != null){
            AxmlWriter axmlWriter = new AxmlWriter();
            amReader.accept(new AxmlVisitor(axmlWriter) {

                @Override
                public NodeVisitor first(String ns, String name) {
                    NodeVisitor nv = super.first(ns, name);

                    return new NodeVisitor(nv) {
                        @Override
                        public void attr(String ns, String name, int resourceId, int type, Object obj) {
                            super.attr(ns, name, resourceId, type, obj);

                            if (name.equals("package")) {
                                packageName = (String) obj;
                            }
                        }




                        @Override
                        public NodeVisitor child(String ns, String name) {
                            if (name.equals("application")) { // application
                                return new NodeVisitor(nv) {



                                    @Override
                                    public NodeVisitor child(String ns, String name) {

                                        if (name.equals("activity")) {

                                            return new NodeVisitor(super.child(ns, name)) {

                                                @Override
                                                public void attr(String ns, String name, int resourceId, int type, Object obj) {
                                                    //  super.attr(ns, name, resourceId, type, obj);
                                                    if (name.equals("name")){
                                                        String activityName = (String) obj;
                                                        activityClazz.add(activityName);

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

        int size = activityClazz.size();


        int num = 0;
        for (int i = 0; i <size ; i++) {
            String clazz = activityClazz.get(i);
            String fullName = null;
            boolean isNormal = false;

            if(clazz.startsWith(packageName)){
                isNormal = true ;
                fullName = clazz ;
            }else {
                if (clazz.startsWith(".")){
                    isNormal = true ;
                    fullName = packageName + clazz;
                }
            }
            if (isNormal){

                boolean isExists = false;
                try {
                    isExists = DexParse.getStrings(dexPath,fullName);
                } catch (DexParse.DexParserException e) {
                    e.printStackTrace();
                }
                if(isExists){
                    return false;
                }else{

                    num = num +1 ;
                    double rate = num / size;
                    if (rate > this.guardValue){
                        return true;
                    }

                }

            }else {
                //存在组件名字既不是以包名开头，又不以.开头的情况
                String tagA = packageName + "." + clazz;
                boolean extis = false ;
                try {
                    extis = DexParse.getStrings(dexPath, tagA) || DexParse.getStrings(dexPath,clazz);
                } catch (DexParse.DexParserException e) {
                    e.printStackTrace();
                }
                if (extis){
                    return false ;
                }else{

                    num = num +1 ;
                    double rate = num / size;
                    if (rate > this.guardValue){
                        return true;
                    }
                }
            }

        }


        return isShellTag;


    }




}
