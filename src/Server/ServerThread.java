package Server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.Vector;



class ServerThread extends Thread
{
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private static Vector onlineUser=new Vector(10,5);
	private static Vector socketUser=new Vector(10,5);
	private String strReceive,strKey;
	private StringTokenizer st;
	private final String USERLIST_FILE="./user.txt";
	
	public ServerThread(Socket client)throws IOException
	{
		socket=client;
		in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
		this.start();
	}
	
	public void run()
	{
		try
		{
			while(true)
			{
				strReceive=in.readLine();
				st=new StringTokenizer(strReceive,"|");
				strKey=st.nextToken();
				if(strKey.equals("login"))
				{
					login();
				}
				else if(strKey.equals("talk"))
				{
					talk();
				}
				else if(strKey.equals("init"))
				{
				 iniClinetOnline();
				}
				else if(strKey.equals("reg"))
				{
					register();
				}
			}
		}
		catch(IOException e)
		{
			String leaveUser=closeSocket();
			System.out.println("[SYSTEM]"+leaveUser+"leave chatroom!");
			sendAll("talk|>>>"+leaveUser+"离开了聊天室。");
		}
	}
	
	private boolean isExisUser(String name)
	{
		String strRead;
		try
		{
			FileInputStream inputfile=new FileInputStream(USERLIST_FILE);
			BufferedReader inputdata=new BufferedReader(new InputStreamReader(inputfile));
			while((strRead=inputdata.readLine())!=null)
			{
				StringTokenizer stUser=new StringTokenizer(strRead,"|");
				if(stUser.nextToken().equals(name))
				{
					return true;
				}
			}
		}
		catch(FileNotFoundException fn)
		{
			System.out.println("[ERROR]User File has not exist!"+fn);
			out.println("warning|读/写文件时出错！");
		}
		catch(IOException e)
		{
			System.out.println("[ERROR]"+e);
			out.println("warning|读/写文件时出错！");
		}
		return false;
	}
	
	
	public boolean isUserLogin(String name,String password)
	{
		String strRead;
		try
		{
			FileInputStream inputfile=new FileInputStream(USERLIST_FILE);
			BufferedReader inputdata=new BufferedReader(new InputStreamReader(inputfile));
			while((strRead=inputdata.readLine())!=null)
			{
				if(strRead.equals(name+"|"+password))
				{
					return true;
				}
			}
		}
		catch(FileNotFoundException fn)
		{
			System.out.println("[ERROR]User File has not exist!"+fn);
			out.println("warning|读/写文件时出错！");
		}
		catch(IOException ie)
		{
			System.out.println("[ERROR]"+ie);
			out.println("warning|读/写文件时出错！");
		}
		return false;
	}
	
	private void register()throws IOException
	{
		String name=st.nextToken();
		String password=st.nextToken().trim();
		File file=new File(USERLIST_FILE);
		if(file.exists())
		{
			if(isExisUser(name))
			{
				System.out.println("[ERROR]"+name+"Register fail!");
				out.println("warning|该用户名已存在，请改名！");
			}
			else
			{
				RandomAccessFile userFile=new RandomAccessFile(USERLIST_FILE,"rw");
				userFile.seek(userFile.length());
				userFile.writeBytes(name+"|"+password+"\r\n");
				login(name);
			}
		}
		else
		{
			file.createNewFile();
			if(isExisUser(name))
			{
				System.out.println("[ERROR]"+name+"Register fail!");
				out.println("warning|该用户名已存在，请改名！");
			}
			else
			{
				RandomAccessFile userFile=new RandomAccessFile(USERLIST_FILE,"rw"); 
				userFile.seek(userFile.length());
				userFile.writeBytes(name+"|"+password+"\r\n");
				login(name);
			}
		}
	}
	
	private void login()throws IOException
	{
		String name=st.nextToken();
		String password=st.nextToken().trim(); 
		boolean succeed=false;
		System.out.println("[USER LOGIN]"+name+":"+password+":"+socket);
		for(int i=0;i<onlineUser.size();i++)
		{
			if(onlineUser.elementAt(i).equals(name))
			{
				System.out.println("[ERROR]"+name+"is logined!");
				out.println("warning|"+name+"已经登录聊天室");
			}
		}
		if(isUserLogin(name,password))
		{
			login(name);
			succeed=true;
		}
		if(!succeed)
		{
			out.println("warning|"+name+"登录失败，请检查您的输入！");
			System.out.println("[SYSTEM]"+name+"login fail");
		}
	}
	
	private void login(String name)throws IOException
	{
		out.println("login|succeed");
		sendAll("online|"+name);
		onlineUser.addElement(name);
		socketUser.addElement(socket);
		sendAll("talk|>>>"+name+"进入聊天室！");
		System.out.println("[SYSTEM]"+name+"login succeed!");
	}
	
private void talk() throws IOException{
    
    String strTalkInfo = st.nextToken();
    String strSender = st.nextToken();
    String strReceiver = st.nextToken();
    System.out.println(strSender+"send:"+"[TALK_"+ strReceiver+ "]"+strTalkInfo);
    Socket socketSend;
    PrintWriter outSend;
    
    //得到当前时间
    GregorianCalendar calendar = new GregorianCalendar();
    String strTime ="("+ calendar.get(Calendar.HOUR_OF_DAY)+":"+ calendar.get(Calendar.MINUTE)+":"+ calendar.get(Calendar.SECOND)+")";
    strTalkInfo=strTime+strTalkInfo;
     if(strReceiver.equals("All"))
    {
     sendAll("talk|"+strSender+"对所有人说："+strTalkInfo);
    } else{
    if(strSender.equals(strReceiver)){
    out.println("talk|>>>不能自言自语");
    }else{
    for (int i = 0; i< onlineUser.size();i++) {
    if (strReceiver.equals(onlineUser.elementAt(i))) {
     socketSend = (Socket) socketUser.elementAt(i);
     outSend = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketSend.getOutputStream())), true);
     	 	outSend.println("talk|"+ strSender+"对你说:"+ strTalkInfo);
    }else if
    	(strSender.equals(onlineUser.elementAt(i))) {
    	socketSend = (Socket) socketUser.elementAt(i);
    	outSend = new PrintWriter(new BufferedWriter(
     	 new OutputStreamWriter(
     	 socketSend.getOutputStream())), true);
     	 	outSend.println("talk|你对"+ strReceiver+"说:"+ strTalkInfo);	
    	}
    	}
    	}
    }
    }
    
    //初始化在线用户列表
    private void iniClinetOnline() throws IOException{
     String strOnline = "online";
     for (int i = 0; i< onlineUser.size();i++) 
     {
       strOnline+="|"+onlineUser.elementAt(i);
     }
     out.println(strOnline);
    }
    
    //信息群发
    private void sendAll(String strSend){
       Socket socketSend;
       PrintWriter outSend;
       try{
          for (int i = 0; i< socketUser.size();i++) {
         socketSend = (Socket) socketUser.elementAt(i);
          outSend = new PrintWriter(new BufferedWriter(
         new OutputStreamWriter(socketSend.getOutputStream())), true);
     	 	outSend.println(strSend);
          }
       }
       catch (IOException e)
       {
       	System.out.println("[ERROR] send all fail!");
       }
    }
    
    
    //关闭套接字
    private String closeSocket()
    {
	    String strUser = "";
	    for (int i = 0; i<socketUser.size();i++)
	    {
		    if(socket.equals((Socket) socketUser.elementAt(i))) 
		    {
		    	strUser = onlineUser.elementAt(i).toString();
		    	socketUser.removeElementAt(i);
		    	onlineUser.removeElementAt(i);
		    	sendAll("remove|"+strUser);
		    }
	    }	
    try
    {
     in.close();
     out.close();
     socket.close();
    }catch (IOException e){
     System.out.println("[ERROR]"+e);
    }
    return strUser;
    }   
}