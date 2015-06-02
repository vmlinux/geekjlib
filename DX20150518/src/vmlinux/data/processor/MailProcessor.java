package vmlinux.data.processor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;

import vmlinux.data.entity.ExecParam;
import vmlinux.data.entity.OData;
import vmlinux.tool.DataTransfer;
import vmlinux.tool.ReceiveOneMail;
import vmlinux.util.DbExecuteEx;

public class MailProcessor implements IDataProcessor
{

	@Override
	public void process(OData conf) throws Exception
	{
		//init mail settings
		boolean usessl="on".equalsIgnoreCase(conf.mailfrom._ssl)||"true".equalsIgnoreCase(conf.mailfrom._ssl);
        Properties props = System.getProperties();
        props.put("mail.smtp.host", conf.mailfrom._smtp_host);
        props.put("mail.smtp.auth", conf.mailfrom._smtp_auth);
        if(usessl)
        {
        	props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        	props.put("mail.pop3.socketFactory.fallback", "false");
        	props.put("mail.pop3.port", "995");
        	props.put("mail.pop3.socketFactory.port", "995");
        	props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        	props.put("mail.smtp.socketFactory.fallback", "false");
        	props.put("mail.smtp.port", "465");
        	props.put("mail.smtp.socketFactory.port", "465");
        }
        Session session = Session.getDefaultInstance(props, null);  
        URLName urln = new URLName("pop3", conf.mailfrom._pop3_host, usessl?995:110, null,  
        		conf.getQueryFrom(conf.mailfrom.mail_user), conf.getQueryFrom(conf.mailfrom.mail_pass));
        
        //init data processing part
		DataSource dsto=conf.to.source.getDataSource();
		DbExecuteEx exto=null;
		Connection conn=null;
		DbExecuteEx exdfrom=null;
		DbExecuteEx exdto=null;
		Connection dconn=null;
		if(conf.autoCommit())
		{
			exto=new DbExecuteEx(dsto);
		}
		else
		{
			conn=dsto.getConnection();
			conn.setAutoCommit(false);
			exto=new DbExecuteEx(conn);
		}
		try
		{
			Hashtable<String, Class> dcolmap=null;
			if(conf.diffto!=null && conf.difffrom!=null)
			{
				DataSource dsdfrom=conf.difffrom.source.getDataSource();
				DataSource dsdto=conf.diffto.source.getDataSource();
				exdfrom=new DbExecuteEx(dsdfrom);
				if(conf.autoCommit())
				{
					exdto=new DbExecuteEx(dsdto);
				}
				else
				{
					dconn=dsto.getConnection();
					dconn.setAutoCommit(false);
					exdto=new DbExecuteEx(dconn);
				}
				if(conf.diffto._preaction==null)
				{
					conf.diffto._preaction=conf.getPreAction();
				}
				if("truncate".equalsIgnoreCase(conf.diffto._preaction))
				{
					conf.diffto.table.truncate(exdto);
				}
				if("deleteall".equalsIgnoreCase(conf.diffto._preaction))
				{
					conf.diffto.table.deleteAll(exdto);
				}
				dcolmap=new Hashtable<String, Class>();
				ResultSet drsto=exdto.executeQuery("select * from "+conf.diffto.table.getTableName());
				ResultSetMetaData drstometa=drsto.getMetaData();
				for(int i=0;i<drstometa.getColumnCount();++i)
				{
					dcolmap.put(drstometa.getColumnName(i+1).toLowerCase()
							, Class.forName(drstometa.getColumnClassName(i+1)));
				}
				drsto.close();
			}
			if("truncate".equalsIgnoreCase(conf.getPreAction()))
			{
				conf.to.table.truncate(exto);
			}
			if("deleteall".equalsIgnoreCase(conf.getPreAction()))
			{
				conf.to.table.deleteAll(exto);
			}
			ResultSet rsto=exto.executeQuery("select * from "+conf.to.table.getTableName());
			ResultSetMetaData rstometa=rsto.getMetaData();
			Hashtable<String, Class> colmap=new Hashtable<String, Class>();
			for(int i=0;i<rstometa.getColumnCount();++i)
			{
				colmap.put(rstometa.getColumnName(i+1).toLowerCase()
						, Class.forName(rstometa.getColumnClassName(i+1)));
			}
			rsto.close();

			//start mail client
	        Store store = session.getStore(urln);
	        int failcount=0;
	        do
	        {
		        try
		        {
		        	store.connect();
		        	break;
		        }
		        catch(Exception exe)
		        {
		        	failcount++;
		        	if(failcount<9)
		        	{
		        		Thread.sleep(3000);
		        		System.err.println("try again #"+failcount);
		        	}
		        	else
		        	{
		        		throw exe;
		        	}
		        }
	        } while(failcount<9);
	        Folder folder = store.getFolder("INBOX");
	        folder.open(Folder.READ_ONLY);
	        Message message[] = folder.getMessages();
	        //System.out.println("Messages's length: " + message.length);
	        Date dt=new Date(new Date().getTime()/(24*60*60*1000)*(24*60*60*1000));
	        if(conf.mailfrom.mail_days>0)
	        {
	        	dt=new Date(dt.getTime()-conf.mailfrom.mail_days*(24*60*60*1000));
	        }
	        ReceiveOneMail pmm = null;
	        int n=0;
	        int nc=conf.getNCommit();
	        ExecParam param=conf.getParentParam();
	        for(int i=message.length-1;i>=0;--i)
	        {
	        	Hashtable<String,Object> row=null;
				try
				{
		        	pmm=new ReceiveOneMail((MimeMessage)message[i]);
		        	try
		        	{
			        	Date rd=pmm.getReceiveDate();
			        	if(rd==null)
			        	{
			        		rd=pmm.getSentDate();
			        	}
			        	if(rd!=null && rd.before(dt))
			        	{
			        		break;
			        	}
		        	}
		        	catch(MessageRemovedException exe)
		        	{
		        		continue;
		        	}
		        	String mailfrom=pmm.getFrom();
		        	String mailaddr=mailfrom.replaceAll(".*<(.*)>.*", "$1").trim();
		        	if(mailaddr.length()==0)
		        	{
		        		mailaddr=mailfrom;
		        	}
		        	if(conf.mailfrom.excludelist!=null && conf.mailfrom.excludelist.indexOf("/"+mailaddr+"/")>=0)
		        	{
		        		continue;
		        	}
		        	pmm.getMailContent((Part) message[i]);
		        	row=new Hashtable<String, Object>();
		        	String subject=pmm.getSubject();
		        	DataTransfer.setIfNotNull(row,"MAILSUBJECT", subject);
		        	DataTransfer.setIfNotNull(row,"MAILBODY", pmm.getBodyText(),2000);
		        	DataTransfer.setIfNotNull(row,"MAILREPLYSIGN", pmm.getReplySign());
		        	DataTransfer.setIfNotNull(row,"MAILFROM", mailfrom);
		        	DataTransfer.setIfNotNull(row,"MAILISNEW", pmm.isNew());
		        	DataTransfer.setIfNotNull(row,"MAILSENTDATE", pmm.getSentDate());
		        	DataTransfer.setIfNotNull(row,"MAILRECVDATE", pmm.getReceiveDate());
		        	DataTransfer.setIfNotNull(row,"MAILID", pmm.getMessageId());
		        	DataTransfer.setIfNotNull(row,"MAILTO", pmm.getMailAddress("to"),500);
		        	DataTransfer.setIfNotNull(row,"MAILCC", pmm.getMailAddress("cc"),500);
		        	DataTransfer.setIfNotNull(row,"MAILBCC", pmm.getMailAddress("bcc"),500);
		        	DataTransfer.setIfNotNull(row,"MAILHASATTACH", pmm.isContainAttach((Part)message[i]));

		        	if(conf.mailfrom.matchsubject!=null)
		        	{
		        		if(subject!=null && subject.indexOf(conf.mailfrom.matchsubject)>=0)
		        		{
		        			
		        		}
		        		else
		        		{
		        			continue;
		        		}
		        	}
					if(n==0 && dcolmap!=null)
					{
						conf.difffrom.prepare(row);
					}
		        	conf.to.table.insertRow(row, exto, colmap);
					if(dcolmap!=null)	//do diff
					{
						param.diffkey=(String)row.get(conf.difffrom._keyfield.toLowerCase());
						ResultSet drs=exdfrom.executeQuery(conf.getQueryFrom(conf.difffrom.query));
						if(drs.next())
						{
							if(conf.difffrom.isDiff(row,drs))
							{
								conf.diffto.table.insertRow(row, exdto, dcolmap);
							}
						}
						else if("true".equalsIgnoreCase(conf.difffrom._addnew))
						{
							conf.diffto.table.insertRow(row, exdto, dcolmap);
						}
						drs.close();
					}
				}
				catch(Exception ex)
				{
					String msg="Data Sample: "+conf.to.table.getDataSample(row);
					System.err.println(msg);
					ex.printStackTrace();
					if(!"true".equalsIgnoreCase(conf._skipexception))
					{
						throw new RuntimeException(msg+"\r\n"+ex.toString());
					}
					else
					{
						System.err.println("[auto skip exception]");
					}
				}
				++n;
				if(conf.autoCommit())
				{
					if(n%nc==0)
					{
						//System.out.println("已处理 "+n+" 行");
						conf.reportStatus("已处理 "+n+" 行");
					}
				}
				else if(conf.doCommit(n))
				{
					conn.commit();
					//System.out.println("已处理 "+n+" 行");
					conf.reportStatus("已处理 "+n+" 行");
					if(dconn!=null)
					{
						dconn.commit();
					}
				}
			}
			if(conf.autoCommit())
			{
				if(n%nc!=0)
				{
					//System.out.println("已处理 "+n+" 行");
					conf.reportStatus("已处理 "+n+" 行");
				}
			}
			else if(!conf.doCommit(n))
			{
				conn.commit();
				//System.out.println("已处理 "+n+" 行");
				conf.reportStatus("已处理 "+n+" 行");
				if(dconn!=null)
				{
					dconn.commit();
				}
			}
		}
		finally
		{
			exto.close();
			if(exdfrom!=null)
			{
				exdfrom.close();
			}
			if(exdto!=null)
			{
				exdto.close();
			}
		}
	}

}
