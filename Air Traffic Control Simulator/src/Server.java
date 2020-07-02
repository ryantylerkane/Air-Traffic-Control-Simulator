
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread{

	private ServerSocket serverSocket;
	private String msgFromClient;
	public static long time = System.currentTimeMillis();
	private Runway run;
	private Station st;
	
	public Server(int socketNum) throws IOException //Parameterized constructor that takes the port number as an argument.
	{
		setName("Main Server");
		serverSocket = new ServerSocket(socketNum);	//Create a new socket on the server using socket provided from command line.
		run = new Runway(3); //Create a runway with three takeoff/landing tracks as per Project #1 specs.
		st = new Station(3); //Create a station with three recharge stations as per Project #1 specs.
	}
	
	public void run()
	{
		while(true) //NOTE: In order to have the thread act as a true server, it will remain active even even after all of the client thread have finished running the project #1 code.
		{ //However, the ClientServer threads will terminate once its Client reaches the end of its run method in order to conserve resources.
			try {
				Socket clientSocket = serverSocket.accept(); //Server will remain here waiting for clients if there are not any looking to connect.
				
				//Establish input and output streams to communicate with the client threads.
				PrintWriter outputToClient = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outputToClient.println("Connection accepted. Setting up new ClientServer thread."); //Indicate to the client that a new thread is being made to manage its methods.
				
				if((msgFromClient = inputFromClient.readLine()) != null)
				{
					msg("Client sent- " + msgFromClient + "."); //Output the Clients message to the console.
				}
				ClientServer subServer = new ClientServer(run, st, clientSocket, inputFromClient.readLine()); //Hand the socket containing the Client to the ClientServer thread and the name of the Client thread.
				subServer.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void msg(String m) {
		System.out.println(getName()+": "+m);
		}
	
	public static void main (String args[]) throws NumberFormatException, IOException
	{
		if(args.length<1)
		{
			System.err.println("Please pass the port number as a command line argument and try again!");
			System.exit(1);
		}
		else
		{
			Server server = new Server(Integer.parseInt(args[0])); //Instantiate the server thread with the port number so that it establishes a connection.
			server.start(); //Have the server begin listening for client threads to connect.
		}
	}
}
