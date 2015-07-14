

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by gaokun on 15/6/12.
 */
public class DexParse {
	public static class DexParserException extends Exception
	{
		public DexParserException(String detailMessage, Throwable throwable)
		{
			super(detailMessage, throwable);
		}


	}

	public static boolean getStrings(byte[] dexDate ,String clazz) throws DexParserException
	{
		boolean flag=false;
		String tag = clazz.replace(".","/");
		InputStream dexInputStream = new ByteArrayInputStream(dexDate);
		try
		{


			SeekableInputStream seekableInputStream = new SeekableInputStream(dexInputStream);
			DataDecoder decoder = new DataDecoder(seekableInputStream);

			// Read the number of strings
			seekableInputStream.seek(STRING_IDS_SIZE_OFFSET);
			int numStrings = decoder.readInt();
			if (numStrings == 0){
				return flag;

			}

			// Read the string table offset
			int stringOffset = decoder.readInt();
			seekableInputStream.seek(stringOffset);

			// Read each string offset
			int[] stringOffsets = new int[numStrings];
			for (int i = 0; i < numStrings; i++)
			{
				stringOffsets[i] = decoder.readInt();
			}

			// Sort the string offsets to be sure we don't have to seek backwards
			Arrays.sort(stringOffsets);

			// Read each string
			for (int offset : stringOffsets)
			{
				try
				{
					seekableInputStream.seek(offset);
				}
				catch (IllegalStateException e)
				{
					throw new DexParserException("Read string that went past the beginning of the next one", e);
				}

				String temp=decoder.readStringDataItem();

				if (temp.indexOf(tag) >-1) {
					return true ;
				}


			}

			return flag;
		}
		catch (IOException e)
		{
			throw new DexParserException("IO Exception while reading strings", e);
		}
	}

	private static class SeekableInputStream extends InputStream
	{
		public SeekableInputStream(InputStream inputStream)
		{
			this.inputStream = inputStream;
		}

		public int read() throws IOException
		{
			int value = inputStream.read();
			position++;
			return value;
		}

		public int read(byte[] b) throws IOException
		{
			int numRead = inputStream.read(b);
			position += numRead;
			return numRead;
		}

		public int read(byte[] b, int off, int len) throws IOException
		{
			int numRead = inputStream.read(b, off, len);
			position += numRead;
			return numRead;
		}

		public long skip(long n) throws IOException
		{
			long numSkipped = inputStream.skip(n);
			position += numSkipped;
			return numSkipped;
		}

		public int available() throws IOException
		{
			return inputStream.available();
		}

		public void close() throws IOException
		{
			inputStream.close();
		}

		public void mark(int readlimit)
		{
			inputStream.mark(readlimit);
		}

		public void reset() throws IOException
		{
			inputStream.reset();
		}

		public boolean markSupported()
		{
			return inputStream.markSupported();
		}

		public void seek(long destPosition) throws IOException
		{
			if (destPosition < position)
				throw new IllegalStateException("Cannot seek backwards to position " + destPosition + " from position " + position);

			long numToSkip = destPosition - position;
			while (numToSkip > 0)
			{
				long numSkipped = skip(numToSkip);
				if (numSkipped <= 0)
					throw new IOException("Attempting to skip " + numToSkip + " bytes failed to skip anything");
				numToSkip -= numSkipped;
			}
		}

		private InputStream inputStream;
		private long position = 0;
	}

	private static class DataDecoder
	{
		public DataDecoder(InputStream inputStream)
		{
			this.inputStream = inputStream;
		}

		public byte readByte() throws IOException
		{
			return (byte)inputStream.read();
		}

		public int readInt() throws IOException
		{
			readFully(buf, 0, 4);
			return (buf[0] & 0xFF) |
					((buf[1] & 0xFF) << 8) |
					((buf[2] & 0xFF) << 16) |
					((buf[3] & 0xFF) << 24);
		}

		public int readULeb128() throws IOException
		{
			int retVal = 0;
			int shift = 0;

			byte b;
			do
			{
				b = readByte();
				retVal |= (b & 0x7F) << shift;
				shift += 7;
			}
			while ((b & 0x80) != 0);

			return retVal;
		}

		public String readStringDataItem() throws IOException
		{
	            /*int numChars = */readULeb128();

			// numChars is useless since it is the decoded length of the MUTF-8 string,
			// not the number of bytes.  We have to look for the null terminator

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			byte nextByte;
			while ((nextByte = readByte()) != 0)
			{
				bytes.write(nextByte);
			}

			return new String(bytes.toByteArray());
		}

		private void readFully(byte[] buf, int offset, int length) throws IOException
		{
			int numRead = 0;
			while (numRead < length)
			{
				int curNumRead = inputStream.read(buf, offset + numRead, length - numRead);
				if (curNumRead < 0)
					throw new EOFException("Unexpected end of file found");
				numRead += curNumRead;
			}
		}

		private InputStream inputStream;
		private byte[] buf = new byte[4];
	}

	private static final int STRING_IDS_SIZE_OFFSET = 56;



}
