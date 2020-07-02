import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ControllerClient extends Thread{

	int id;
	public static long time;
	String msgFromServer; //Will be used to hold any incoming messages from the Server or ClientServer.
	Socket serverSocket; //Socket that will be used to connect to the same host/port as the server.
	PrintWriter output; //Will be used to write messages to the server.
	BufferedReader input; //Will be used to retrieve messages from the server.
	private int numShuttles; //The number of shuttles that have not yet finished three cycles.
	String threadType = "Controller";
	public ControllerClient(int i, String hostName, int socket, int shuttles)
	{
		
	    try {
	    	id = i;
	    	setName("Controller-"+ id);
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
			time =  System.currentTimeMillis();
			while(numShuttles>Driver.shuttlesFinished)//Keep iterating until all of the Shuttles have completed three cycles.
			{	
				Thread.sleep(5000); //Open the recharge station every 'half hour'
				waitForMethod(0);
				if(numShuttles>Driver.shuttlesFinished)//Don't output message if all Shuttles have terminated.
					msg("Has opened the recharge station."); //There will be a scenario where the garage will still open despite no Shuttle waiting. This was left in the output to show that the Controller was still working.
				//If we receive a message from the server saying there are no Shuttles waiting to refuel then there is no point in executing the rest of the code.
				if(!msgFromServer.equals("Station is empty."))
				{
					waitForMethod(1);
					waitForMethod(2);
				}
				//Else, station is empty, so sleep again.
						
				/*NOTE: This could have more easily been done by keeping the station object as a
				 * class member in this thread. However, having the ClientServer check the value and
				 * send a message over was done in spirit of the project.*/ 			
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
		while(!msgFromServer.equals("Method executed.") && !msgFromServer.equals("Station is empty.")) //Wait until ClientServer indicates that the method is complete.
		{
			msgFromServer = input.readLine();
		}
	}
	
}
