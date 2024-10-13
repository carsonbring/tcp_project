// Carson Bring || 10.13.2024 || CSCI 455
package networking.tcp_project;

import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TcpServerService {
	
	private final int port = 6789;
	private int numMessages;
	private ServerSocket welcomeSocket;
	private ExecutorService threadPool;
	
	//pretty colors
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_BLUE = "\u001B[34m";

	
	//using post construct to ensure method runs after bean is initialized by spring (same for client)
    @PostConstruct
    public void startServer() {
		numMessages = 0;
		threadPool = Executors.newCachedThreadPool();
		
		//If user kills process with ctrl-z or c, this cleans everything up
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown Hook: User killed Server...");
            killServer();
        }));
        new Thread(this::runServer).start();
    }

	//basically the main mehthod. Takes on new clients by creating a respective connection socket and assigns them a thread. 
    public void runServer() {
        try {
			welcomeSocket = new ServerSocket(port);
            System.out.println(ANSI_GREEN + "The TCP server running on port " + port + ANSI_RESET);
			
            while (!welcomeSocket.isClosed()) {
                Socket connectionSocket = welcomeSocket.accept();
				threadPool.submit(() -> handleClient(connectionSocket));
            }
        } catch (IOException e) {
			if (welcomeSocket != null && welcomeSocket.isClosed()) {
                System.out.println(ANSI_YELLOW + "Server Socket was closed by user,  shutting down \n" + ANSI_RESET);
            } else {
                e.printStackTrace();
            }
        }
    }

	//main method that handles client interaction. Each thread (besides the one assigned to runServer will be running an instance of this method)
	private void handleClient(Socket connectionSocket){
		try {
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            String clientSentence;
			int clientNumMessages;
			String clientIpAddress = connectionSocket.getInetAddress().getHostAddress();
			int clientPort = connectionSocket.getPort();

			//handles scenario where null is the input
			while ((clientSentence = inFromClient.readLine()) != null) {
				//breaks if 'exit'
				if (clientSentence.trim().equals("exit")){
					break;
				}
				clientNumMessages = updateNumMessages();
				System.out.println(ANSI_CYAN + "Message from client: " + clientSentence + ANSI_YELLOW +" | IP: " + ANSI_BLUE + clientIpAddress +  ANSI_YELLOW + "| Port: " + ANSI_BLUE + clientPort + ANSI_RESET);

				String outputString = "Total messages: " + clientNumMessages;
				outToClient.writeBytes(outputString + '\n');
				
			}
			System.out.println(ANSI_YELLOW + "Closed a connection... " + ANSI_RESET);
			connectionSocket.close();
		} catch (IOException e){
			System.out.println(ANSI_RED + "Error in handle client: "+ e + ANSI_RESET);
		}
	}
	
	//synchronized method allows for no two updates at same time. returns its own message count as source of truth.
	private synchronized int updateNumMessages() {
		numMessages++;
		return numMessages;
	}

	//kills all threads in pool and welcome socket
	@PreDestroy
    public void killServer() {
        try {
            if (welcomeSocket != null && !welcomeSocket.isClosed()) {
                welcomeSocket.close();
            }
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdownNow(); 
            }
            System.out.println(ANSI_YELLOW + "Server stopped without error" + ANSI_RESET);
        } catch (IOException e) {
            System.out.println(ANSI_RED + "Error while killing server: " + e + ANSI_RESET);
        }
    }

}

