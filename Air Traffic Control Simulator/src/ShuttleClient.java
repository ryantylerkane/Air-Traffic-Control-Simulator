import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ShuttleClient extends Thread{

	public static long time;
	private int cruiseNum =1; 
	private int id;
	private String msgFromServer; //Will be used to hold any incoming messages from the Server or ClientServer.
	private Socket serverSocket; //Socket that will be used to connect to the same host/port as the server.
	private PrintWriter output; //Will be used to write messages to the server.
	private BufferedReader input; //Will be used to retrieve messages from the server.
	private String threadType = "Shuttle";
	
	public ShuttleClient(int i, String hostName, int socket)
	{
	    try {
	    	id = i;
	    	setName("Shuttle-"+ id);
	    	serverSocket = new Socket(hostName, socket); //Connect to the socket the main server resides on.
			output = new PrintWriter(serverSocket.getOutputStream(), true); //Establish output to the main server.
			input = new BufferedReader(new InputStreamReader(serverSocket.getInputStream())); //InputStream to receive input from the main server.
	    } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try {
			
			establishConnection();
			time= System.currentTimeMillis();
			Thread.sleep(1000); //Have the thread sleep for a second so that all of the output showing setting up Client and ClientServer threads will not intertwine with the cruise output.
			while(!Driver.timeComplete)//Have the shuttles keep cycling until every shuttle has completed at least three trips.
			{ 
					msg("Is cruising (Trip #"+cruiseNum+")."); //Each shuttle will begin by sleeping to represent cruising in the air. 
					Thread.sleep((long)(Math.random()*8000)); //Use random number * 8000 to represent a value between 0 to 8 seconds to represent cruising.
					msg("Has landed and is moving to the recharge station.");
					Thread.sleep((long)(Math.random()*2000)); //Use random number * 2000 to represent a value between 0 to 2 seconds to represent moving to the recharge station.
					msg("Is waiting to access the recharge station.");
					waitForMethod(0); //Request st.waitForRecharge()
					waitForMethod(1);//Request st.recharge();
					msg("Is now refueled and is moving toward the runway.");
					Thread.sleep((long)(Math.random()*2000)); //Simulate moving to takeoff area.
					msg("Has reached the runway and is waiting to takeoff.");
					waitForMethod(2);// Request run.takeoff();
					msg("Is taking off.");
					cruiseNum++; //Increment the number of cruises by one.
			}
			Thread.sleep((long)(Math.random()*2000)); //Sleep for a random amount of time to avoid having two threads finish at the exact same time. Such a scenario results in the static variable incrementing improperly.
			Driver.shuttlesFinished++; //Mark the shuttle as finished so Controller and Supervisor know when to terminate.
			msg("Has reached the end of its run method.");
			output.println("I'm finished.");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void msg(String m) {
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
		}
	
	private void establishConnection()
	{
		try 
		{
			if((msgFromServer = input.readLine()) != null) //Receive message from main Server after connecting.
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
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void waitForMethod(int methodNum) throws IOException
	{
		msgFromServer = "";
		output.println(threadType);
		output.println(methodNum);
		while(!msgFromServer.equals("Method executed.")) //Wait until ClientServer indicates that the method is complete.
		{
			
			msgFromServer = input.readLine();
		}
	}
	
}
