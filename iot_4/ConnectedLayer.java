import java.io.*;
import java.net.*;

public class ConnectedLayer implements Layer {
    
	public static String destination_host;
	public static int destination_port;
	public static Layer upward_layer;
	public static int connection_id;
	private static int packet_number = 0;
	private static int last_received_packet;

	private static int thread_pool_size = 10000; 
	private static Thread new_send_thread[] = new Thread[thread_pool_size];
	private static send_stop_and_wait new_send[] = new send_stop_and_wait[thread_pool_size];

	public ConnectedLayer(String Host, int Port, int id){
		destination_host = Host;
		destination_port = Port;
		connection_id = id;
		GroundLayer.deliverTo(this);
		String initial_packet = connection_id + ";" + packet_number + ";--HELLO--";

		new_send[packet_number] = new send_stop_and_wait(initial_packet, destination_host, destination_port);
		new_send_thread[packet_number] = new Thread(new_send[packet_number]);
		new_send_thread[packet_number].start();
	}

	@Override
	public void send(String payload){
		packet_number = packet_number + 1;
		String aggregated_payload = connection_id + ";" + packet_number + ";" + payload;

		new_send[packet_number] = new send_stop_and_wait(aggregated_payload, destination_host, destination_port);
		new_send_thread[packet_number] = new Thread(new_send[packet_number]);
		new_send_thread[packet_number].start();
	}

	public void send_ack(int connectionId, int packetNumber){
		String aggregated_ack = connectionId + ";" + packetNumber + ";" + "--ACK--";
		GroundLayer.send(aggregated_ack, destination_host, destination_port);
	}

	@Override
	public synchronized void receive(String payload, String source){
		String[] payload_array = payload.split(";");

		if (payload_array[2].trim().equals("--ACK--")) {
			new_send[Integer.parseInt(payload_array[1])].ack_received();
			new_send[Integer.parseInt(payload_array[1])].notify();
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


class send_stop_and_wait implements Runnable {

	int standard_timeout = 1000;
	String payload;
	String dest_host;
	int dest_port;
	Boolean not_acknoledged = true;

    public send_stop_and_wait(String aggregated_payload, String destination_host, int destination_port) {
		payload = aggregated_payload;
		dest_host = destination_host;
		dest_port = destination_port;
    }

    public void ack_received(){
    	not_acknoledged = false;
    }

    public synchronized void run () {
    	do {
			GroundLayer.send(payload, dest_host, dest_port);
			
			try{
				wait(standard_timeout);
			}catch (Exception e){
				System.err.println("Got an Exception while waiting for ack...");
			}

    	} while(not_acknoledged);

    }
}





