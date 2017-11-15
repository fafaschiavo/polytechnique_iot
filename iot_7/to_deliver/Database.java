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
import java.util.Vector;

public class Database{

	private Vector table = new Vector(); // Better use a Vector to ensure a thread safe class 
	private int sequence_number = -1;

	Database(){}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Return current sequence number
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public int getDatabaseSequenceNumber(){
		return sequence_number;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Empty the database, preparing it to receive the peer's dump
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public void clear_database(){
		table.clear();
		sequence_number = -1;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Receives updates and increment sequence number
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public void add_to_database(String element){
		table.add(element);
		sequence_number = sequence_number + 1;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Set new squence number - will be called after done receciving dump to establish current version 
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public void set_new_sequence_number(int seqNumber){
		sequence_number = seqNumber;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Return a dump of the current database - important for brain dumping myy ownn database to 
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public Vector get_database_dump(){
		return table;
	}

}