package Server;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

 
public class ChatServer 
{
    
    ServerSocket serverSocket;
    private final int SERVER_PORT=8888;

    private final int backlog=20;
    
    public ChatServer()
    {
    	try
    	{
    InetAddress addr = InetAddress.getByName("115.29.47.239");
    		serverSocket=new ServerSocket(SERVER_PORT,backlog,addr);
    		System.out.println("Server started...");
    		System.out.println("Server port is:"+SERVER_PORT);
    		getIP();
    		while(true)
    		{
    			Socket socket=serverSocket.accept();
    			new ServerThread(socket); 
    		}
    	}
    	catch(Exception e)
    	{
    		System.out.println("[ERROR]Cound not start server."+e);
    	}
    }
    
    public void getIP()
    {
    	try
    	{
    		InetAddress localAddress=InetAddress.getLocalHost();
    		byte[] ipAddress=localAddress.getAddress();
    		System.out.println("Server IP is:"+(ipAddress[0]&0xff)+"."+(ipAddress[1]&0xff)+"."+(ipAddress[2]&0xff)+"."+(ipAddress[3]&0xff)); 
    	}
    	catch(Exception e)
    	{
    		System.out.println("[ERROR]Cound not get IP."+e);
    	}
    }
    public static void main(String[] args) 
    {
    	new ChatServer();
    }
}
