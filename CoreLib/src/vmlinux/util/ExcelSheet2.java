package vmlinux.util;

import java.io.*;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * using poi
 * @author job
 *
 */
public class ExcelSheet2
{
	private Workbook workbook;
	private Sheet currentSheet;
	private Cell currentCell;
	
	public ExcelSheet2(String file,String encoding)
	{
		File f=new File(file);
		try
		{
			if(f.exists())
			{
				workbook=WorkbookFactory.create(f);
			}
			else
			{
				workbook=WorkbookFactory.create(f);
			}
			setSheet(0);
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public void close()
	{
		if(workbook!=null)
		{
		}
	}
	
	public Sheet setSheet(int n)
	{
		if(workbook==null)
			throw new RuntimeException("Workbook not loaded");
		currentSheet=workbook.getSheetAt(n);
		return currentSheet;
	}
	
	public Sheet setSheet(String name)
	{
		if(workbook==null)
			throw new RuntimeException("Workbook not loaded");
		currentSheet=workbook.getSheet(name);
		return currentSheet;
	}
	
	public String[] getSheetNames()
	{
		if(workbook==null)
			throw new RuntimeException("Workbook not loaded");
		String[] names=new String[workbook.getNumberOfSheets()];
		for(int i=0;i<names.length;++i)
		{
			names[i]=workbook.getSheetName(i);
		}
		return names;
	}
	
	public Sheet getSheet()
	{
		return currentSheet;
	}
	
	public Cell setCell(int i,int j)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		currentCell=currentSheet.getRow(i).getCell(j);
		return currentCell;
	}
	
	public Cell setCell(String name)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		String col=name.toUpperCase().replaceAll("([a-zA-Z]+)[0-9]+", "$1");
		String row=name.toUpperCase().replaceAll("[a-zA-Z]+([0-9]+)", "$1");
		int coln=0;
		for(int i=0;i<col.length();++i)
		{
			char c=col.charAt(i);
			coln=coln*26+c-'A'+1;
		}
		int rown=Integer.parseInt(row);
		currentCell=currentSheet.getRow(rown-1).getCell(coln-1);
		return null;
	}
	
	public Cell getCell()
	{
		return currentCell;
	}
	
	protected Cell findCellLike(String text)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		Cell cell = null;
		Iterator<Row> i=currentSheet.rowIterator();
		while(i.hasNext())
		{
			Row r=i.next();
			for(int j=r.getFirstCellNum();j<r.getLastCellNum();++j)
			{
				Cell c=r.getCell(j);
				if(c.getStringCellValue().indexOf(text)>=0)
				{
					break;
				}
			}
		}
		return cell;
	}
	
	public Cell findCell(String text)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		Cell cell = null;
		Iterator<Row> i=currentSheet.rowIterator();
		while(i.hasNext())
		{
			Row r=i.next();
			for(int j=r.getFirstCellNum();j<r.getLastCellNum();++j)
			{
				Cell c=r.getCell(j);
				if(c.getStringCellValue().equals(text))
				{
					break;
				}
			}
		}
		return cell;
	}
	
	public int findRow(String text)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		Cell cell=findCellLike(text);
		if(cell==null)
			return -1;
		return cell.getRowIndex();
	}
	
	public int findColumn(String text)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		Cell cell=findCellLike(text);
		if(cell==null)
			return -1;
		return cell.getColumnIndex();
	}
	
	public String getCellString(String name)
	{
		if(setCell(name)!=null)
		{
			return currentCell.getStringCellValue();
		}
		return null;
	}
	
	public String getCellString(int i,int j)
	{
		if(setCell(i,j)!=null)
		{
			return currentCell.getStringCellValue();
		}
		return null;
	}

	protected void addCell(Cell cell)
	{
		throw new RuntimeException("not implemented");
	}
	
	public void setCellString(String name,String value)
	{
		if(setCell(name)!=null)
		{
			currentCell.setCellValue(value);
		}
	}
	
	public void setCellNumber(String name,double value)
	{
		if(setCell(name)!=null)
		{
			currentCell.setCellValue(value);
		}
	}
	
}
