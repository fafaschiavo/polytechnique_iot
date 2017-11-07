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

	Peer(String new_peerID, InetAddress new_peerIPAddress, int new_peerSeqNum, int expiration_delay){
		peerID = new_peerID;
		peerIPAddress = new_peerIPAddress;
		peerSeqNum = new_peerSeqNum;
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
		peerSeqNum = peerSeqNum + 1;
	}

	public String get_peer_id(){
		return peerID;
	}

	public Boolean is_peer_expired(){
		long current_time = System.currentTimeMillis();
		if (current_time > expirationTime) {
			return true;
		}else{
			return false;
		}
	}

}