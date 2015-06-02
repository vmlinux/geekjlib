package vmlinux.util;

import java.io.*;

import jxl.*;
import jxl.write.*;
import jxl.write.Number;

/**
 * using jxl
 * @author job
 *
 */
public class ExcelSheet
{
	private WritableWorkbook workbook;
	private WritableSheet currentSheet;
	private WritableCell currentCell;
	
	public ExcelSheet(String file,String encoding)
	{
		File f=new File(file);
		if(f.exists())
		{
			WorkbookSettings wbs=new WorkbookSettings();
			wbs.setEncoding(encoding);
			try
			{
				Workbook wb=Workbook.getWorkbook(f);
				workbook=Workbook.createWorkbook(f, wb);
			}
			catch(Exception ex)
			{
				throw new RuntimeException(ex);
			}
		}
		else
		{
			try
			{
				workbook=Workbook.createWorkbook(f);
			//throw new RuntimeException("File to load doesn't exists "+file);
			}
			catch(Exception ex)
			{
				throw new RuntimeException(ex);
			}
		}
	}
	
	public void close()
	{
		if(workbook!=null)
		{
			//File fin=new File(infile);
			//File fout=new File(outfile);
			try
			{
				workbook.write();
				workbook.close();
				//fin.delete();
				//fout.renameTo(fin);
			}
			catch(Exception ex)
			{
				
			}
		}
	}
	
	public WritableSheet setSheet(int n)
	{
		if(workbook==null)
			throw new RuntimeException("Workbook not loaded");
		currentSheet=workbook.getSheet(n);
		return currentSheet;
	}
	
	public WritableSheet setSheet(String name)
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
		return workbook.getSheetNames();
	}
	
	public WritableSheet getSheet()
	{
		return currentSheet;
	}
	
	public WritableCell setCell(int i,int j)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		currentCell=currentSheet.getWritableCell(i,j);
		return currentCell;
	}
	
	public WritableCell setCell(String name)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		currentCell=currentSheet.getWritableCell(name);
		return currentCell;
	}
	
	public WritableCell getCell()
	{
		return currentCell;
	}
	
	protected Cell findCellLike(String text)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		Cell cell = null;
		int rows=currentSheet.getRows();
		//int cols=currentSheet.getColumns();
		boolean found = false;
		for (int i = 0; i < rows && !found; i++)
		{
			Cell row[] = currentSheet.getRow(i);
			for (int j = 0; j < row.length && !found; j++)
			{
				if (row[j].getContents().indexOf(text)>=0)
				{
					cell = row[j];
					found = true;
				}
			}
		}

		return cell;
	}
	
	public WritableCell findCell(String text)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		Cell cell=findCellLike(text);
		if(cell==null)
			return null;
		return setCell(cell.getColumn(),cell.getRow());
	}
	
	public int findRow(String text)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		Cell cell=findCellLike(text);
		if(cell==null)
			return -1;
		return cell.getRow();
	}
	
	public int findColumn(String text)
	{
		if(currentSheet==null)
			throw new RuntimeException("Sheet not set");
		Cell cell=findCellLike(text);
		if(cell==null)
			return -1;
		return cell.getColumn();
	}
	
	public String getCellString(String name)
	{
		if(setCell(name)!=null)
		{
			return currentCell.getContents();
		}
		return null;
	}
	
	public String getCellString(int i,int j)
	{
		if(setCell(i,j)!=null)
		{
			return currentCell.getContents();
		}
		return null;
	}

	protected void addCell(WritableCell cell)
	{
		try
		{
			//cell.setCellFeatures(currentCell.getWritableCellFeatures());
			cell.setCellFormat(currentCell.getCellFormat());
			currentSheet.addCell(cell);
			currentCell=cell;
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public void setCellString(String name,String value)
	{
		if(setCell(name)!=null)
		{
			WritableCell cell=new Label(currentCell.getColumn(),currentCell.getRow(),value);
			addCell(cell);
		}
	}
	
	public void setCellNumber(String name,double value)
	{
		if(setCell(name)!=null)
		{
			WritableCell cell=new Number(currentCell.getColumn(),currentCell.getRow(),value);
			addCell(cell);
		}
	}
	
}
