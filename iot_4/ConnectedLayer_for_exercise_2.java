import java.io.*;
import java.net.*;

public class ConnectedLayer implements Layer {
    
	public static String destination_host;
	public static int destination_port;
	public static Layer upward_layer;
	public static int connection_id;
	private static int packet_number = 0;
	private static int last_received_packet;

	public ConnectedLayer(String Host, int Port, int id){
		destination_host = Host;
		destination_port = Port;
		connection_id = id;
		GroundLayer.deliverTo(this);
		String initial_packet = connection_id + ";" + packet_number + ";--HELLO--";
		GroundLayer.send(initial_packet, destination_host, destination_port);
	}

	@Override
	public void send(String payload){
		packet_number = packet_number + 1;

		// connectionId;packetNumber;payload
		String aggregated_payload = connection_id + ";" + packet_number + ";" + payload;
		GroundLayer.send(aggregated_payload, destination_host, destination_port);
	}

	public void send_ack(int connectionId, int packetNumber){
		// connectionId;packetNumber;--ACK--
		String aggregated_ack = connectionId + ";" + packetNumber + ";" + "--ACK--";
		GroundLayer.send(aggregated_ack, destination_host, destination_port);
	}

	@Override
	public void receive(String payload, String source){
		String[] payload_array = payload.split(";");
		// System.out.println(payload_array[0]); // connection ID
		// System.out.println(payload_array[1]); // packet number received
		// System.out.println(payload_array[2]); // clean payload

		if (payload_array[2].trim().equals("--ACK--")) {
			
		}else{
			last_received_packet = Integer.parseInt(payload_array[1]);
			String payloar_to_forward = payload_array[2];
			upward_layer.receive(payloar_to_forward, source);
			send_ack(Integer.parseInt(payload_array[0]), Integer.parseInt(payload_array[1]));
		}
	}

	@Override
	public void deliverTo(Layer above_layer){
		upward_layer = above_layer;
	}

	@Override
	public void close(){

	}

}
