import java.util.*;
import java.lang.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.concurrent.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

public class MuxDemuxSimple implements Runnable{

    private DatagramSocket myS = null;
    private BufferedReader in;
    private SimpleMessageHandler[] myMessageHandlers;
    private SynchronizedListQueue outgoing = new SynchronizedListQueue();
    private Boolean reading_thread_up = false;

	MuxDemuxSimple(SimpleMessageHandler[] h, DatagramSocket s){
		myS = s;
		myMessageHandlers = h;
	}

	public void run(){
		if (reading_thread_up) {

			// /////////////////////////////////////////////////////////////////////////////////////////////
			// This is the writting loop thread 
			// /////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("Now writting");
			while(true){
				try{

					String message_to_send = outgoing.dequeue();
					byte[] byteArray = message_to_send.getBytes();
					try{
						DatagramPacket dp = new DatagramPacket(byteArray, byteArray.length, InetAddress.getByName("255.255.255.255"), 4242);
						myS.send(dp);
					}catch (UnknownHostException e){
						System.err.println("Ops... Got an UnknownHostException error...");
					}catch (IOException e){
						System.err.println("Ops... Got an IOException error...");
					}

				} catch (NoSuchElementException e){}
			}

		}else{

			// /////////////////////////////////////////////////////////////////////////////////////////////
			// This is the reading loop thread 
			// /////////////////////////////////////////////////////////////////////////////////////////////
			reading_thread_up = true;
			for (int i=0; i<myMessageHandlers.length; i++){
				myMessageHandlers[i].setMuxDemux(this);
			}
			try{
				System.out.println("Now reading");
				while(true){
					byte[] receiveData = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					myS.receive(receivePacket);
					String message = new String( receivePacket.getData());
					InetAddress IPAddress = receivePacket.getAddress();
					int port = receivePacket.getPort();

					for (int i=0; i<myMessageHandlers.length; i++){
						myMessageHandlers[i].handleMessage(message);
					}
				}

			}catch (IOException e){ }		
			try{
				in.close();
				myS.close();
			}catch(IOException e){ }

		}
	}

	public void send(String s){
		outgoing.enqueue(s);
	}


	public static void main(String[] args) {
		System.out.println("Hi there");
		SimpleMessageHandler[] handlers = new SimpleMessageHandler[3];
		handlers[0] = new HelloSender();
		handlers[1]= new HelloReceiver();
		handlers[2]= new DebugReceiver();

		try {
			DatagramSocket mySocket = new DatagramSocket(4242);
			mySocket.setBroadcast(true);
			MuxDemuxSimple dm = new MuxDemuxSimple(handlers, mySocket);
			handlers[0].setMuxDemux(dm);
			new Thread(handlers[0]).start();
			new Thread(handlers[1]).start();
			new Thread(handlers[2]).start();

			// Launch reading Thread
			new Thread(dm).start();
			try {
			  Thread.sleep(100);
			} catch(InterruptedException ex) {}
			// Launch wwritting Thread
			new Thread(dm).start();

		} catch (SocketException e){
			System.err.println(e);
			System.err.println("Ops... Got an Socket exception error...");
		}
	}

}
