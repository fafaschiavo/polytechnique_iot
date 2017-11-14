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

public class Peer{

	private String peerID;
	private InetAddress peerIPAddress;
	private int peerSeqNum;
	private long expirationTime;
	private String peerState;
	private int available_sequence_number;

	Peer(String new_peerID, InetAddress new_peerIPAddress, int new_peerSeqNum, int expiration_delay){
		peerID = new_peerID;
		peerIPAddress = new_peerIPAddress;
		peerSeqNum = -1;
		available_sequence_number = -1;
		peerState = "heard";
		expirationTime = System.currentTimeMillis();
		expirationTime = expirationTime + (expiration_delay*1000);
	}

	public void update_peer_state(int new_peerSeqNum, int expiration_delay){
		expirationTime = System.currentTimeMillis( );
		expirationTime = expirationTime + (expiration_delay*1000);
		if (new_peerSeqNum != peerSeqNum) {
			peerState = "inconsistent";
		}
		if (peerState.equals("inconsistent")) {
			peerState = "inconsistent";
		}
		if (new_peerSeqNum == peerSeqNum && peerState.equals("synchronised")) {
			peerState = "synchronised";
		}
		// available_sequence_number = new_peerSeqNum;

	}

	public void set_as_synchronized(int new_peerSeqNum){
		peerSeqNum = new_peerSeqNum;
		peerState = "synchronised";
	}

	public String get_peer_id(){
		return peerID;
	}

	public int get_peer_sequence_number(){
		return peerSeqNum;
	}

	public int get_peer_available_sequence_number(){
		return available_sequence_number;
	}

	public Boolean is_peer_expired(){
		long current_time = System.currentTimeMillis();
		if (current_time > expirationTime) {
			return true;
		}else{
			return false;
		}
	}

	public Boolean is_peer_inconsistent(){
		if (peerState.equals("inconsistent")) {
			return true;
		}else{
			return false;
		}
	}

}