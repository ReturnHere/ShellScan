
package pxb.android.axml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.googlecode.dex2jar.reader.io.DataIn;
import com.googlecode.dex2jar.reader.io.DataOut;

@SuppressWarnings("serial")
class StringItems extends ArrayList<StringItem> {

    byte[] stringData;

    public int getSize() {
        return 5 * 4 + this.size() * 4 + stringData.length + 0;// TODO
    }

    public void prepare() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = 0;
        int offset = 0;
        baos.reset();
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (StringItem item : this) {
            item.index = i++;
            String stringData = item.data;
            Integer of = map.get(stringData);
            if (of != null) {
                item.dataOffset = of;
            } else {
                item.dataOffset = offset;
                map.put(stringData, offset);
                int length = stringData.length();
                byte[] data = stringData.getBytes("UTF-16LE");
                baos.write(length);
                baos.write(length >> 8);
                baos.write(data);
                baos.write(0);
                baos.write(0);
                offset += 4 + data.length;
            }
        }
        // TODO
        stringData = baos.toByteArray();
    }

    public void read(DataIn in, int size) throws IOException {
        int trunkOffset = in.getCurrentPosition() - 8;
        int stringCount = in.readIntx();
        int styleOffsetCount = in.readIntx();
        int flags = in.readIntx();
        int stringDataOffset = in.readIntx();
        int stylesOffset = in.readIntx();
        for (int i = 0; i < stringCount; i++) {
            StringItem stringItem = new StringItem();
            stringItem.index = i;
            stringItem.dataOffset = in.readIntx();
            this.add(stringItem);
        }
        Map<Integer, String> stringMap = new TreeMap();
        // if (styleOffsetCount != 0) {
        // throw new RuntimeException();
        // for (int i = 0; i < styleOffsetCount; i++) {
        // StringItem stringItem = new StringItem();
        // stringItem.index = i;
        // stringItems.add(stringItem);
        // }
        // }
        if (stylesOffset != 0) {
            System.err.println("ignore style offset at 0x" + Integer.toHexString(trunkOffset));
        }
        int base = trunkOffset + stringDataOffset;
        for (StringItem item : this) {
            String s = stringMap.get(item.dataOffset);
            if (s == null) {
                in.move(base + item.dataOffset);
                if (0 != (flags & AxmlReader.UTF8_FLAG)) {
                    int length = (int) in.readLeb128();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream(length + 10);
                    for (int r = in.readByte(); r != 0; r = in.readByte()) {
                        bos.write(r);
                    }
                    s = new String(bos.toByteArray(), "UTF-8");
                } else {
                    int length = in.readShortx();
                    byte[] data = in.readBytes(length * 2);
                    // in.skip(2);
                    s = new String(data, "UTF-16LE");
                }
                stringMap.put(item.dataOffset, s);
            }
            item.data = s;
        }

    }

    public void write(DataOut out) throws IOException {
        out.writeInt(this.size());
        out.writeInt(0);// TODO
        out.writeInt(0);
        out.writeInt(7 * 4 + this.size() * 4);
        out.writeInt(0);
        for (StringItem item : this) {
            out.writeInt(item.dataOffset);
        }
        out.writeBytes(stringData);
        // TODO
    }
}
