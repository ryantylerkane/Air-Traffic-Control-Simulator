import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientServer extends Thread{
	
	//Declare all three client thread types so that the ClientServer has access to all of its methods.
	//Shuttle shuttle;
	//Controller controller;
	//Supervisor supervisor;
	//Declare both classes responsible for maintaining the monitor to allow for mutual exclusivity.
	Runway run;
	Station st;
	//Declare a Socket that will be used to hold the already established connection to the Client.
	Socket clientSocket;
	//Writer and Reader that will place output into the stream and retrieve input from the stream respectively.
	PrintWriter outputToClient;
	BufferedReader inputFromClient;
	String msgFromClient;
	String threadType;
	
	public static long time = System.currentTimeMillis();
	
	public ClientServer(Runway runway, Station station, Socket client, String clientName)
	{
		run = runway;
		st = station;
		clientSocket = client;
		setName(clientName+"'s Server"); //Set the name of the thread to the *ClientName*Server.
		try { //Create the input and output streams to the Client so that incoming requests can be handled.
			outputToClient = new PrintWriter(clientSocket.getOutputStream(), true);
			inputFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outputToClient.println("ClientServer is now active.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		String nextClientMsg;
		
		while(true)
		{
			try {
					nextClientMsg = inputFromClient.readLine();
				if(nextClientMsg.equals("I'm finished.")) //If the Thread reaches the end of its run method exit the run method (thus terminating the thread) in order to conserve resources.
				{ 
					break;
				}
				else
				{
					runClientMethod(nextClientMsg,Integer.parseInt(inputFromClient.readLine()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		msg("Has reached the end of its run method.");
	}
	
	private void runClientMethod(String type, int method) throws InterruptedException
	{
		threadType = type;
		
		if(threadType.equals("Shuttle"))
		{
			switch(method)
			{
				case 0:
					st.waitForRecharge(); 
					break;
				
				case 1:
					st.recharge();
					break;
				
				case 2:
					run.takeoff();
					break;
			}
			
			outputToClient.println("Method executed.");
			
		}
		
		else if(threadType.equals("Controller"))
		{
				switch(method)
				{
				case 0:
					if(st.waitingToRecharge.size() <= 0) //If there are no shuttles waiting to enter, we must not try to run any of the methods.
					{
						outputToClient.println("Station is empty."); //Output empty message so that no other methods are executed.
					}
					else
					{
						st.openRecharge(); 
						outputToClient.println("Method executed.");
					}
					break;
				
				case 1:
					st.waitToRefuel();
					outputToClient.println("Method executed.");
					break;
				
				case 2:
					st.notifyRefueled();
					outputToClient.println("Method executed.");
					break;
				}
				
		}
		else //The threadType is Supervisor.
		{
			
			switch(method)
			{
			case 0:
				run.findNumToTakeoff();
				if(run.numToTakeoff != 0) //If there are more shuttles to process, let the Client know so that it can recall methods 1 and 2 again.
				{
					outputToClient.println("More shuttles to process.");
				}
				else
				{
					outputToClient.println("Method executed."); //All of the Shuttles have taken off.
				}
				break;
			
			case 1:
				run.signalClearForTakeoff();
				if(run.numToTakeoff != 0) //If there are more shuttles to process, let the Client know so that it can recall methods 1 and 2 again.
				{
					outputToClient.println("More shuttles to process.");
				}
				else
				{
					outputToClient.println("Method executed."); //All of the Shuttles have taken off.
				}
				break;
			
			case 2:
				run.signalTakeoff();
				if(run.numToTakeoff != 0) //If there are more shuttles to process, let the Client know so that it can recall methods 1 and 2 again.
				{
					outputToClient.println("More shuttles to process.");
				}
				else
				{
					outputToClient.println("Method executed."); //All of the Shuttles have taken off.
				}
				break;
			}
		}
		
	}
	
	public void msg(String m) {
		System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
		}
}
