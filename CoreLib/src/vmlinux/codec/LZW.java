package vmlinux.codec;

import java.io.*;
import java.util.*;

interface Compression
{
	void compress(InputStream inp, OutputStream out) throws IOException;
	void decompress(InputStream inp, OutputStream out) throws IOException;
}

public class LZW implements Compression
{
	public static class LimitedDict extends Dict
	{
		int maxSize;

		LimitedDict(int maxSize)
		{
			this.maxSize = maxSize;
		}

		public void add(ByteArray str)
		{
			if (size() < maxSize)
				super.add(str);
		}
	}

	public static class BitInputStream extends FilterInputStream
	{

		public BitInputStream(InputStream is)
		{
			super(is);
		}

		class BitManager
		{

			private int[] buf = new int[8];

			private int cnt = -1;

			boolean atTheEnd()
			{
				return ((buf[7] == 1) && (cnt < 0));
			}

			void setTheEnd()
			{
				buf[7] = 1;
				cnt = -1;
			}

			boolean noMoreBuffer()
			{
				return cnt < 0;
			}

			void setNext(int next)
			{
				for (cnt = 0; cnt < 8; ++cnt)
				{
					buf[cnt] = next % 2;
					next /= 2;
				}

				if (buf[7] == 1)
				{
					for (cnt = 7; cnt >= 0; cnt--)
						if (buf[cnt] == 0)
							break;
					cnt--;
				}
				else
				{
					cnt = 6;
				}
			}

			int getNext()
			{
				return buf[cnt--];
			}

			int left()
			{
				return cnt + 1;
			}

		};

		BitManager bitManager = new BitManager();

		byte[] tempBuf = null;

		int tempBufPtr = 0;

		int tempBufLen = 0;

		private int readNextByte() throws IOException
		{
			int val = -1;
			if (tempBufPtr == tempBufLen)
				val = super.read();
			else
			{
				byte b = tempBuf[tempBufPtr++];
				if ((b & 0x80) > 0)
					val = ((int) (b & 0x7F)) | 0x80;
				else
					val = b;
			}
			return val;
		}

		public int read() throws IOException
		{

			if (bitManager.atTheEnd())
				return -1;

			if (bitManager.noMoreBuffer())
			{
				int i = readNextByte();
				if (i < 0)
					bitManager.setTheEnd();
				else
					bitManager.setNext(i);
				return read();
			}

			return bitManager.getNext();
		}

		public int read(byte[] arr) throws IOException
		{
			return read(arr, 0, arr.length);
		}

		public int read(byte[] arr, int off, int len) throws IOException
		{
			int bytelen = ((len - bitManager.left()) / 7);
			tempBuf = new byte[bytelen];
			tempBufLen = in.read(tempBuf);
			tempBufPtr = 0;
			for (int i = 0; i < len; ++i)
			{
				int next = read();
				if (next < 0)
					return i;
				arr[off + i] = (byte) next;
			}
			return len;
		}

	}

	public static class BitOutputStream extends FilterOutputStream
	{
		class BitManager
		{
			int buf = 0;

			int cnt = 0;

			int writeOne(int next)
			{
				int ret = -1;
				buf = buf * 2 + next;
				cnt++;
				if (cnt == 7)
				{
					cnt = 0;
					ret = buf;
					buf = 0;
				}
				else
				{
					ret = -1;
				}
				return ret;
			}

			int writeLast()
			{
				int x = 0;
				for (int i = 0; i < 7 - cnt; ++i)
					x = x * 2 + 1;
				for (int i = 7 - cnt; i < 8; ++i)
					x = x * 2;
				return buf | x;
			}
		}

		BitManager bitManager = new BitManager();

		public BitOutputStream(OutputStream os)
		{
			super(os);
		}

		public void write(int i) throws IOException
		{
			int x = bitManager.writeOne(i >= 1 ? 1 : 0);
			if (x >= 0)
				out.write(x);
		}

		public void write(byte[] arr) throws IOException
		{
			write(arr, 0, arr.length);
		}

		public void write(byte[] arr, int off, int len) throws IOException
		{
			int clen = 0;
			for (int i = 0; i < len; ++i)
			{
				int x = bitManager.writeOne(arr[off + i]);
				if (x >= 0)
					arr[off + (clen++)] = (byte) x;
			}
			out.write(arr, off, clen);
		}

		public void close() throws IOException
		{
			out.write(bitManager.writeLast());
			out.close();
		}

	}

	public static class ByteArray
	{

		final byte[] arr;

		ByteArray(byte[] b)
		{
			arr = (byte[]) b.clone();
		}

		ByteArray()
		{
			arr = new byte[0];
		}

		ByteArray(byte b)
		{
			arr = new byte[] { b };
		}

		public boolean equals(Object o)
		{
			ByteArray ba = (ByteArray) o;
			return java.util.Arrays.equals(arr, ba.arr);
		}

		public int hashCode()
		{
			int code = 0;
			for (int i = 0; i < arr.length; ++i)
				code = code * 2 + arr[i];
			return code;
		}

		public int size()
		{
			return arr.length;
		}

		byte getAt(int i)
		{
			return arr[i];
		}

		public ByteArray conc(ByteArray b2)
		{
			int sz = size() + b2.size();
			byte[] b = new byte[sz];
			for (int i = 0; i < size(); ++i)
				b[i] = getAt(i);
			for (int i = 0; i < b2.size(); ++i)
				b[i + size()] = b2.getAt(i);
			return new ByteArray(b);
		}

		public ByteArray conc(byte b2)
		{
			return conc(new ByteArray(b2));
		}

		public byte[] getBytes()
		{
			return (byte[]) arr.clone();
		}

		public boolean isEmpty()
		{
			return size() == 0;
		}

		public byte getLast()
		{
			return arr[size() - 1];
		}

		public ByteArray dropLast()
		{
			byte[] newarr = new byte[size() - 1];
			for (int i = 0; i < newarr.length; ++i)
				newarr[i] = arr[i];
			return new ByteArray(newarr);
		}

		public String toString()
		{
			return new String(arr);
		}
	}

	public static class Dict
	{

		Map mp = new HashMap();

		List ls = new ArrayList();

		public void add(ByteArray str)
		{
			mp.put(str, new Integer(ls.size()));
			ls.add(str);
		}

		public final int numFromStr(ByteArray str)
		{
			return (mp.containsKey(str) ? ((Integer) mp.get(str)).intValue() : -1);
		}

		public final ByteArray strFromNum(int i)
		{
			return (i < ls.size() ? (ByteArray) ls.get(i) : null);
		}

		public final int size()
		{
			return ls.size();
		}
	}

	public static void test1main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("Usage: java BitInputStream FromFile ToFile");
			System.out
					.println("where 'FromFile' is a file to be open as a Bit Stream");
			System.out
					.println("and they are written as characters of '0's and '1's");
			System.out.println("every line having one char");
			System.exit(1);
		}
		try
		{
			InputStream is = new BitInputStream(new BufferedInputStream(
					new FileInputStream(args[0])));
			PrintWriter os = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(args[1])));
			int next;
			while ((next = is.read()) >= 0)
				os.println(next);
			is.close();
			os.close();
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println(args[0] + " file cannot be opened");
			System.exit(1);
		}
		catch (IOException ioe)
		{
			System.out.println("Error in reading file " + args[0]
					+ " or writing file " + args[1]);
			System.exit(1);
		}
	}

	public static void test2main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("Usage: java BitOutputStream FromFile ToFile");
			System.out
					.println("where 'FromFile' includes characters of '0' and '1'");
			System.out.println("and they are written as bits into 'ToFile'");
			System.exit(1);
		}

		try
		{
			InputStream is = new BufferedInputStream(new FileInputStream(
					args[0]));
			OutputStream os = new BitOutputStream(new BufferedOutputStream(
					new FileOutputStream(args[1])));
			int next;
			while ((next = is.read()) >= 0)
			{
				char ch = (char) next;
				if (ch == '0' || ch == '1')
					os.write((int) (ch - '0'));
			}
			is.close();
			os.close();
		}
		catch (FileNotFoundException fnfe)
		{
			System.out.println(args[0] + " file not found");
			System.exit(1);
		}
		catch (IOException ioe)
		{
			System.out.println("Error in reading file " + args[0]
					+ " or writing file " + args[1]);
			System.exit(1);
		}
	}

	boolean stopped = false;
	Dict dict;
	int numOfBits;
	final ByteArray emptyBA = new ByteArray();
	ByteArray w = emptyBA;

	public LZW()
	{
		numOfBits = 12;

		dict = new LimitedDict(1 << numOfBits);

		for (int i = 0; i < 256; ++i)
			dict.add(new ByteArray((byte) i));
	}

	int encodeOneChar(int n)
	{
		byte c = (byte) n;
		ByteArray nw = w.conc(c);
		int code = dict.numFromStr(nw);

		if (code != -1)
		{
			w = nw;
			return -1;
		}
		else
		{
			dict.add(nw);
			nw = w;
			w = new ByteArray(c);
			return dict.numFromStr(nw);
		}
	}

	int encodeLast()
	{
		ByteArray nw = w;
		w = emptyBA;
		return dict.numFromStr(nw);
	}

	void writeCode(OutputStream os, int code) throws IOException
	{
		for (int i = 0; i < numOfBits; ++i)
		{
			os.write(code & 1);
			code /= 2;
		}
	}

	int readCode(InputStream is) throws IOException
	{
		int num = 0;
		for (int i = 0; i < numOfBits; ++i)
		{
			int next = is.read();
			if (next < 0)
				return -1;
			num += next << i;
		}
		return num;
	}

	private class UnClosedOutputStream extends FilterOutputStream
	{
		public UnClosedOutputStream(OutputStream os)
		{
			super(os);
		}

		public void write(byte b[], int off, int len) throws IOException
		{
			out.write(b, off, len);
		}

		public void close() throws IOException
		{
		}
	}

	public void compress(InputStream is, OutputStream os) throws IOException
	{
		os = new BitOutputStream(new UnClosedOutputStream(os));
		int next;
		int code;
		while ((next = is.read()) >= 0)
		{
			if (stopped)
				break;
			code = encodeOneChar(next);
			if (code >= 0)
				writeCode(os, code);
		}
		code = encodeLast();
		if (code >= 0)
			writeCode(os, code);
		os.close();
	}

	ByteArray decodeOne(int code)
	{

		ByteArray str = dict.strFromNum(code);
		if (str == null)
		{
			str = w.conc(w.getAt(0));
			dict.add(str);
		}
		else if (!w.isEmpty())
			dict.add(w.conc(str.getAt(0)));
		w = str;
		return w;
	}

	public void decompress(InputStream is, OutputStream os) throws IOException
	{
		is = new BitInputStream(is);
		ByteArray str;
		int code;
		while ((code = readCode(is)) >= 0)
		{
			if (stopped)
				break;
			str = decodeOne(code);
			os.write(str.getBytes());
		}
	}

	public void stop()
	{
		stopped = true;
	}

	public static void main(String args[]) throws Exception
	{
		teststring();
	}
	
	public static void teststring() throws IOException
	{
		LZW lzw=new LZW();
		String input="this is a test string, with very long long .....jrhm:'5741LAN12016697',zxbh:'5741LAN12016697',xspid:'11020414',xqid:'',giswgbm:'574XQJD00022',jjxbm:'9088921',jjxmc:'贺丞小区光分路器001',khjlid:'58820',xqmc:'',crmkhbh:'174337332998',crmkhwybh:'274388321731967974',fwkssj:'2011-11-25 0:00:00',fwjssj:'',jjxdz:'南演武街108弄8#车库旁',jjxlx:'分光器',ldlx:'ZY_FLG',zybm:'JD.HCLBH/OBD001',sjrq:'2013-6-12 0:00:00',zwbz:'1',khmc:'苏伟斌',zjdz:'浙江省宁波市',xxzjdz:'贺丞路88弄18号406室',bzyhmc2:'甲种',crmcpmc:'有线宽带',yxqymc:'宁波市分公司-宁波市分公司-江东分局-白鹤支局-白鹤A',cxlx:'城市',yhxj:'',lxrmc:'苏伟斌',lxrdh:'TEL：13486658188',khjllxhm:'',asset_integ_id:'1-RKSPAGN',sxtjkssj:'',khlxzwms:'家庭客户',jfyhbz:'1',sl:'2048Kbps',sxbz:'0',qftjlx:'err',gnyhbz:'0',jffs:'1',ztcyhbh:'74000093',ztcyhmc:'包月90元_NB',gxjrlx:'FTTH线路',crmyhbh:'74000093',crmyhmc:'包月90元_NB',tgbs:'0',kdbnlx:'0',cpgc:'8',itvbs:'0',hybs:'1',zzqfyf:'201304',dqrq:'',xbrq:'',czsr:'90',cwsr:'90.00',swsc:'2698436.00',swll:'170447219.00',lwrq:'300012',wdrbs:'3',rhitvyhs:'0.00',rhcdmayhs:'0.00',rhfixyhs:'0.00',rhyhbs:'0',qfje:'75.43',fkfs:'现金',gmyhbs:'0',q3ycwsr:'90.00',q3yczsr:'90.00',sjlyhjrzh:'nbdl12016697',itvzh:'',itvbs1:'',itvs:''},{fj:'江东区',zj:'白鹤支局',wgbm:'JD003',wgmc:'江东白鹤1',giswgmc:'演武花园',cdma_n:'0',fix_n:'1',kd_n:'1',kh_yfffix_n:'0',kh_hfffix_n:'1',kh_yffcdma_n:'0',kh_hffcdma_n:'0',kh_lan_n:'1',kh_xdsl_n:'0',xspmc4:'909999999999999',zcbh:'1-3TN5A4B',xspmc:'LAN拨号[普通线路]',zczt:'现行',khjl:'谢春晖',jrhm:'5741LAN01375510',zxbh:'5741LAN01375510',xspid:'11020418',xqid:'',giswgbm:'574XQJD00011',jjxbm:'10320255',jjxmc:'演武花园LAN网GF035',khjlid:'58820',xqmc:'',crmkhbh:'174100001506737',crmkhwybh:'2740191516',fwkssj:'2007-9-21 0:00:00',fwjssj:'',jjxdz:'文景街98弄21#(演武花园)',jjxlx:'光分纤盒',ldlx:'ZY_FLG',zybm:'JD.TSHDJ/GF035',sjrq:'2013-6-12 0:00:00',zwbz:'1',khmc:'张嗣惠',zjdz:'浙江省宁波市',xxzjdz:'文景街98 弄 17 号 101室',bzyhmc2:'甲种',crmcpmc:'有线宽带',yxqymc:'宁波市分公司-宁波市分公司-江东分局-白鹤支局-白鹤A',cxlx:'城市',yhxj:........";
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		lzw.compress(new ByteArrayInputStream(input.getBytes()), bos);
		byte[] buf=bos.toByteArray();
		ByteArrayOutputStream bos2=new ByteArrayOutputStream();
		lzw.decompress(new ByteArrayInputStream(buf), bos2);
		String output=new String(bos2.toByteArray());
		System.out.println("in:"+input);
		System.out.println("out:"+output);
	}
	
	public static void testlzw()
	{
		LZW lzw = new LZW();
		try
		{
			lzw.compress(new FileInputStream("LZW.JAVA"), new FileOutputStream(
					"lzw.lzw"));
			lzw.decompress(new FileInputStream("lzw.lzw"),
					new FileOutputStream("lzw1.java"));
		}
		catch (Exception e)
		{
		}
	}
}

