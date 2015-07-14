
package com.googlecode.dex2jar.reader.io;


public interface DataIn {

    /**
     * 获取当前位置
     * 
     * @return
     */
    int getCurrentPosition();

    void move(int absOffset);

    void pop();

    void push();

    /**
     * equals to
     * 
     * <pre>
     * push();
     * move(absOffset);
     * </pre>
     * 
     * @see #push()
     * @see #move(int)
     * @param absOffset
     */
    void pushMove(int absOffset);

    /**
	 * 
	 */
    int readByte();

    byte[] readBytes(int size);

    int readIntx();

    int readUIntx();

    int readShortx();

    int readUShortx();

    long readLeb128();

    /**
     * @return
     */
    int readUByte();

    long readULeb128();

    /**
     * @param i
     */
    void skip(int bytes);

}
