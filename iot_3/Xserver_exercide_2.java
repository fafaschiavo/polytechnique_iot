import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;

public class Xserver {

	public static void main(String []args) throws Exception {

		int thread_pool_size = 20;
		int thread_index = 0;
		Boolean is_threads_done[] = new Boolean[thread_pool_size];
		Thread myThreads[] = new Thread[thread_pool_size];

		for(int i=0; i<thread_pool_size; i++){
			is_threads_done[i] = true;
		}

		if (args.length == 0) {
			System.err.println("Usage: java Xserver [port to listen]");
			System.exit(-1);
		}

		String port_to_listen_string = args[0];
		int port_to_listen = Integer.parseInt(port_to_listen_string);

		final ServerSocket server = new ServerSocket(port_to_listen);
		System.out.format("Listening for connection on port %d .... \n", port_to_listen);
		while(true){
			final Socket client_socket = server.accept();
			// handleConnection(client_socket);
			handleConnection_in_thread handleConnection_in_thread_object = new handleConnection_in_thread(client_socket);  
			myThreads[thread_index] = new Thread(handleConnection_in_thread_object);
			myThreads[thread_index].start();

			do {
				thread_index = thread_pool_size;
				for(int i=0; i<thread_pool_size; i++){
				  try{
				    if (!myThreads[i].isAlive()) {
				      is_threads_done[i] = true;
				    }else{
				      is_threads_done[i] = false;
				    }

				  }catch(NullPointerException e){
				    is_threads_done[i] = true;
				  }

				  if (is_threads_done[i]) {
				    thread_index = i;
				  }
				}

				if (thread_index == thread_pool_size) {
				  try {
				      Thread.sleep(100);
				  } catch(Exception e) {}
				}
			} while (thread_index == thread_pool_size);

		}

	}
}




class handleConnection_in_thread implements Runnable {
 
	private Socket my_socket;  

	public handleConnection_in_thread(Socket client_socket) {
		my_socket = client_socket;
	}

	public void run () {

		try{
			InputStreamReader isr =  new  InputStreamReader(my_socket.getInputStream());
			BufferedReader reader = new BufferedReader(isr);
			Boolean is_http_1_protocol = false;
			Boolean is_hostname_line_correct = false;
			String line = reader.readLine();
			String requested_path = "";
			while (!line.isEmpty()) {

				if (line.contains("HTTP/1.1")) {
					is_http_1_protocol = true;
					requested_path = line.split("GET ")[1].split(" HTTP")[0];
				}

				if (line.contains("Host:")) {
					is_hostname_line_correct = true;
				}
				
				System.out.println(line);
				line = reader.readLine();
			}

			if (is_http_1_protocol && is_hostname_line_correct && (requested_path != "")) {
				if (requested_path.equals("/")) {	
					String message = "Hello World! Welcome to my server!";
					String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + message;
					my_socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
					my_socket.close();
				}else{
					try{
						String relative_path = requested_path.substring(1);
						File file = new File(relative_path);
						FileInputStream fis = new FileInputStream(file);
						byte[] data = new byte[(int) file.length()];
						fis.read(data);
						fis.close();
						String file_content = new String(data, "UTF-8");

						String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + file_content;
						my_socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
						my_socket.close();
					}catch(java.io.FileNotFoundException e){
						String message = "Hey! Didn't find what you asked for! What is " + requested_path;
						String httpResponse = "HTTP/1.1 404 BAD REQUEST\r\n\r\n" + message;
						my_socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
						my_socket.close();
					}
				}
			}else{
				String message = "Hey! There is something wrong with your request!";
				String httpResponse = "HTTP/1.1 400 BAD REQUEST\r\n\r\n" + message;
				my_socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
				my_socket.close();
			}

		} catch (Exception e){
			System.err.println(e);
		}

	}

}



