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
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

public class MuxDemuxSimple implements Runnable{

	private String myID = null;
    private DatagramSocket myS = null;
    private BufferedReader in;
    private SimpleMessageHandler[] myMessageHandlers;
    private SynchronizedListQueue outgoing = new SynchronizedListQueue();
    private Boolean reading_thread_up = false;
    private HashMap<String, Peer> peer_table = new HashMap<String, Peer>();

	MuxDemuxSimple(SimpleMessageHandler[] h, DatagramSocket s, String constructor_ID){
		myS = s;
		myMessageHandlers = h;
		myID = constructor_ID;
	}

	public void run(){
		if (reading_thread_up) {

			// /////////////////////////////////////////////////////////////////////////////////////////////
			// This is the writting loop thread 
			// /////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("Writting Thread Started...");
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
				System.out.println("Reading Thread Started...");
				while(true){
					byte[] receiveData = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					myS.receive(receivePacket);
					String message = new String( receivePacket.getData());
					InetAddress ip_address = receivePacket.getAddress();
					int port = receivePacket.getPort();

					for (int i=0; i<myMessageHandlers.length; i++){
						myMessageHandlers[i].handleMessage(message, ip_address);
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

	public void touch_new_peer(String new_peerID, InetAddress new_peerIPAddress, int new_peerSeqNum, int expiration_delay){
		if (!new_peerID.equals(myID)) {
			if (peer_table.get(new_peerID) == null) {
				Peer new_peer = new Peer(new_peerID, new_peerIPAddress, new_peerSeqNum, expiration_delay);
				peer_table.put(new_peerID, new_peer);
			}else{
				Peer existing_peer = peer_table.get(new_peerID);
				existing_peer.update_peer_state(new_peerSeqNum, expiration_delay);
			}		
		}

	}

	public String[] get_valid_peers(){
		int valid_counter = 0;
		List<String> valid_peers_list = new ArrayList<String>();

		Iterator it = peer_table.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			if (!peer_table.get(pair.getKey()).is_peer_expired()) {
				valid_peers_list.add(peer_table.get(pair.getKey()).get_peer_id());
			}else{
				it.remove();
			}
		}

		String[] valid_peers_array = new String[valid_peers_list.size()];
		valid_peers_array = valid_peers_list.toArray(valid_peers_array);
		return valid_peers_array;
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java MuxDemuxSimple YOUR_ID");
			System.exit(-1);
		}

		String myID = args[0];
		System.out.println("==========================================");
		System.out.println("Hi there! This computer's ID is: " + myID);
		System.out.println("==========================================");
		SimpleMessageHandler[] handlers = new SimpleMessageHandler[3];
		handlers[0] = new HelloSender(myID);
		handlers[1]= new HelloReceiver(myID);
		handlers[2]= new DebugReceiver();

		try {
			DatagramSocket mySocket = new DatagramSocket(4242);
			mySocket.setBroadcast(true);
			MuxDemuxSimple dm = new MuxDemuxSimple(handlers, mySocket, myID);
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
