package vmlinux.util;

import java.util.Calendar;

import vmlinux.reflect.XObject;

public class CalcUtil
{
	public static int[] lunarInfo=new int[]{
			0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,
			0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,
			0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,
			0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,
			0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,
			0x06ca0,0x0b550,0x15355,0x04da0,0x0a5d0,0x14573,0x052d0,0x0a9a8,0x0e950,0x06aa0,
			0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,
			0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b5a0,0x195a6,
			0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,
			0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x055c0,0x0ab60,0x096d5,0x092e0,
			0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,
			0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,
			0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,
			0x05aa0,0x076a3,0x096d0,0x04bd7,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,
			0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0};

	public static int[] solarMonth=new int[]{31,28,31,30,31,30,31,31,30,31,30,31};
	public static String[] lunarGan=new String[]{"甲","乙","丙","丁","戊","己","庚","辛","壬","癸"};
	public static String[] lunarZhi=new String[]{"子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥"};
	public static String[] lunarAnimals=new String[]{"鼠","牛","虎","兔","龙","蛇","马","羊","猴","鸡","狗","猪"};
	public static String[] solarTerm = new String[]{"小寒","大寒","立春","雨水","惊蛰","春分","清明","谷雨","立夏","小满","芒种","夏至","小暑","大暑","立秋","处暑","白露","秋分","寒露","霜降","立冬","小雪","大雪","冬至"};
	public static int[] termInfo = new int[]{0,21208,42467,63836,85337,107014,128867,150921,173149,195551,218072,240693,263343,285989,308563,331033,353350,375494,397447,419210,440795,462224,483532,504758};
	public static String[] lunarStr1 = new String[]{"日","一","二","三","四","五","六","七","八","九","十"};
	public static String[] lunarStr2 = new String[]{"初","十","廿","卅","　"};
	public static String[] monthName = new String[]{"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};

	/*****************************************************************************
    	日期计算
	 *****************************************************************************/

	//====================================== 传回农历 y年的总天数
	public static int lunarYearDays(int y) 
	{
		int i, sum = 348;
		for(i=0x8000; i>0x8; i>>=1)
		{
			sum += (lunarInfo[y-1900] & i)==0? 0: 1;
		}
		return (sum+leapDays(y));
	}

	//====================================== 传回农历 y年闰月的天数
	public static int leapDays(int y) 
	{
		if(leapMonth(y)!=0)
		{
			return ((lunarInfo[y-1900] & 0x10000)==0? 29: 30);
		}
		else return (0);
	}

	//====================================== 传回农历 y年闰哪个月 1-12 , 没闰传回 0
	public static int leapMonth(int y) 
	{
		return (lunarInfo[y-1900] & 0xf);
	}

	//====================================== 传回农历 y年m月的总天数
	public static int monthDays(int y,int m) 
	{
		return( (lunarInfo[y-1900] & (0x10000>>m))==0? 29: 30);
	}

	public static class LunarInfo implements XObject
	{
		public int year;
		public int month;
		public int day;
		public boolean isLeap;
		public int yearCyl;
		public int dayCyl;
		public int monCyl;
	}
	
	//====================================== 算出农历, 传入日期物件, 传回农历日期物件
	//     该物件属性有 .year .month .day .isLeap .yearCyl .dayCyl .monCyl
	public static void Lunar(LunarInfo info,java.util.Date objDate) {
	
		int i, leap=0, temp=0;
		java.util.Date baseDate = new java.util.Date(1900,0,31);
		int offset   = (int)((objDate.getTime() - baseDate.getTime())/86400000);
		
		info.dayCyl = offset + 40;
		info.monCyl = 14;
		
		for(i=1900; i<2050 && offset>0; i++) 
		{
			temp = lunarYearDays(i);
			offset -= temp;
			info.monCyl += 12;
		}
		
		if(offset<0) 
		{
			offset += temp;
			i--;
			info.monCyl -= 12;
		}
		
		info.year = i;
		info.yearCyl = i-1864;
		
		leap = leapMonth(i); //闰哪个月
		info.isLeap = false;
		
		for(i=1; i<13 && offset>0; i++) 
		{
			//闰月
			if(leap>0 && i==(leap+1) && info.isLeap==false)
			{ 
				--i; 
				info.isLeap = true; 
				temp = leapDays(info.year); 
			}
			else
			{
				temp = monthDays(info.year, i); 
			}
		
			//解除闰月
			if(info.isLeap==true && i==(leap+1)) 
			{
				info.isLeap = false;
			}
		
			offset -= temp;
			if(info.isLeap == false)
			{
				info.monCyl ++;
			}
		}
		
		if(offset==0 && leap>0 && i==leap+1)
		{
			if(info.isLeap)
			{ 
				info.isLeap = false; 
			}
			else
			{ 
				info.isLeap = true; 
				--i; 
				--info.monCyl;
			}
		}
		
		if(offset<0)
		{ 
			offset += temp; 
			--i; 
			--info.monCyl; 
		};
		
		info.month = i;
		info.day = offset + 1;
	}

	//==============================传回国历 y年某m+1月的天数
	public static int solarDays(int y,int m) 
	{
		if(m==1)
		{
			return(((y%4 == 0) && (y%100 != 0) || (y%400 == 0))? 29: 28);
		}
		else
		{
			return(solarMonth[m]);
		}
	}
	
	//============================== 传入 offset 传回干支, 0=甲子
	public static String cyclical(int num) 
	{
		return (lunarGan[num%10]+lunarZhi[num%12]);
	}

	/*
	//===== 某年的第n个节气为几日(从0小寒起算)
	public static java.util.Date sTerm(int y,int n) 
	{
		java.util.Date offDate = new java.util.Date( ( 31556925974.7*(y-1900) + termInfo[n]*60000  ) + java.util.Date.UTC(1900,0,6,2,5) );
		return(offDate.getUTCDate());
	}
	*/

	public static java.util.Date newDate(int year,int month,int day)
	{
		Calendar c=Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.MONDAY, day);
		return c.getTime();
	}
}
