import pxb.android.axml.Axml;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by gaokun on 15/7/13.
 */
public class main {

    public static void main(String[] args) throws IOException {
        String path = args[0];
        File file = new File(path);
        if (file.exists()) {
            byte[] axmlData = Tool.readEntry(file,"AndroidManifest.xml");
           // String filePath = file.getAbsolutePath();
            byte[] classDate = Tool.readEntry(file,"classes.dex");
            UnkonwnShell unkonwnShell =new UnkonwnShell(axmlData,classDate,0.1);
            boolean shell = unkonwnShell.isShell();
            if (shell){
                System.out.print("本应用被加壳 ! \n");




            }else{
                System.out.print("本应用未加壳 ! \n");
            }




        }else{
            System.out.print(path + " is not exist ! \n");
        }

    }
}
