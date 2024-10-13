// Carson Bring || 10.13.2024 || CSCI 455
package networking.tcp_project_client;

import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.net.Socket;

@Service
public class TcpClientService {
	
	private final String address = "localhost";
	private final int port = 6789;
	private volatile boolean running = true;
	
	//pretty colors 
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_CYAN = "\u001B[36m";

	//post construct for same reason explained in server
	@PostConstruct
    public void startClient() {
        new Thread(this::startSession).start();
    }

	//main method that handles the session
	public void startSession(){
		
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		String message;
		
		try (Socket clientSocket = new Socket(address, port) ){
			
			DataOutputStream serverOutputStream = new DataOutputStream(clientSocket.getOutputStream());
			BufferedReader serverInputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			System.out.println(ANSI_GREEN +"Connected to server. Enter your message. Enter 'exit' to quit" + ANSI_RESET);

			//running boolean flag is used to clean up if user kills process
			while (running && (message = inputReader.readLine()) != null){
				if (message.equalsIgnoreCase("exit")){
					break;
				}
				
				serverOutputStream.writeBytes(message + '\n');
				System.out.println(ANSI_CYAN + "Response from server: " + serverInputStream.readLine() +ANSI_RESET);
				System.out.println(ANSI_GREEN + "Send another message. Enter 'exit' to quit: " +ANSI_RESET);
			}
			
			serverOutputStream.writeBytes("exit\n");
			
			//sleeping for 2 seconds
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace(); 
			}
			
			clientSocket.close();
			
			System.out.println(ANSI_YELLOW + "Closed client socket" + ANSI_RESET);
		   
		} catch (IOException e) {
			System.out.println(ANSI_RED + "Error initializing socket connection " + e + ANSI_RESET);
		}
	}
	@PreDestroy
	public void killSession() {
		running = false;
	}
}
