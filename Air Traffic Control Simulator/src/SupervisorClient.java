import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SupervisorClient extends Thread{

	int id;
	public static long time;
	String msgFromServer; //Will be used to hold any incoming messages from the Server or ClientServer.
	Socket serverSocket; //Socket that will be used to connect to the same host/port as the server.
	PrintWriter output; //Will be used to write messages to the server.
	BufferedReader input; //Will be used to retrieve messages from the server.
	int numShuttles;
	String threadType = "Supervisor";
	
	public SupervisorClient(int i, String hostName, int socket, int shuttles)
	{
		
	    try {
	    	id = i;
	    	setName("Supervisor-"+ id);
	    	serverSocket = new Socket(hostName, socket); //Connect to the socket the main server resides on.
			output = new PrintWriter(serverSocket.getOutputStream(), true); //Establish output to the main server.
			input = new BufferedReader(new InputStreamReader(serverSocket.getInputStream())); //InputStream to receive input from the main server.
			numShuttles = shuttles;
	    } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try {
			establishConnection();
			time = System.currentTimeMillis();
			while(numShuttles>Driver.shuttlesFinished) //Keep iterating until all of the Shuttles have completed three cycles.
			{
				Thread.sleep(7000);//Sleep for seven seconds.
				waitForMethod(0);
				if(numShuttles>Driver.shuttlesFinished) //Don't output the message if all of the shuttles have terminated.
					msg("Has signaled the shuttles for takeoff.");
				while(msgFromServer.equals("More shuttles to process."))
				{
					waitForMethod(1);
					waitForMethod(2);
				}
				/*In order for the project to run successfully using code from the first project,
				 * waitForMethod(1) and waitForMethod(2) must run continuously until all the Shuttles 
				 * eligible to take off have done so. In order for this to happen, the ClientServer must continuously check
				 * the remaining number of Shuttles waiting to takeoff. In spirit of the assignment, rather than 
				 * monitoring this variable on the Client side, the Server will report its value through messages to
				 * the Client thread.*/
			}
			msg("Has reached the end of its run method.");
			output.println("I'm finished.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
	}
	
	public void msg(String m) {
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
		}
	
	private void establishConnection() throws IOException
	{
		if((msgFromServer = input.readLine()) != null)
		{
			System.out.println(getName() + ": Server sent- " + msgFromServer);
			System.out.println();
		}
		
		output.println("Thank you. Awaiting connection to ClientServer.");
		output.println(getName()); //Send the name of the thread to the Server so that it can be used in naming the ClientServer thread.
		if((msgFromServer = input.readLine()) != null) //Receive message from ClientServer to indicate a successful connection. 
		{
			System.out.println(getName() + ": ClientServer sent- " + msgFromServer);
			System.out.println();
		}
	}
	
	private void waitForMethod(int methodNum) throws IOException
	{
		msgFromServer = "";
		output.println(threadType);
		output.println(methodNum);
		while(!msgFromServer.equals("Method executed.") && !msgFromServer.equals("More shuttles to process.")) //Wait until ClientServer indicates that the method is complete.
		{
			msgFromServer = input.readLine();
		}
		
	}
	
}
