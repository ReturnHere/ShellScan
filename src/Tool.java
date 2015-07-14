import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by gaokun on 15/7/13.
 */
public class Tool {

    public static byte[] readEntry(File zipFile, String entryName) {
        try {
            ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zipFile));
            while (true) {
                ZipEntry entry = inputStream.getNextEntry();
                if (entry == null) {
                    break;
                }

                if (!entry.getName().equals(entryName)) {
                    continue;
                }

                ByteArrayOutputStream tempout = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];

                while (true) {
                    int n = inputStream.read(buffer);
                    if (n == -1) {
                        break;
                    }

                    tempout.write(buffer, 0, n);
                }
                return tempout.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }
}
