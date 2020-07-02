import java.io.IOException;
import java.net.ServerSocket;

public class Driver {

	public static Boolean timeComplete;
	public static long startTime;
	public static int shuttlesFinished; //Number of shuttles that have finished the required three cycles.
	
	public static void main(String args[]) throws NumberFormatException, IOException
	{
		/*NOTE: The time for a day was selected to be 60 seconds after assessing the worst case scenario for a single cycle (from cruise to cruise) took 50 seconds
		 * if a shuttle waited the max amount of random time and ended up in the back of the queue for both the recharge station and takeoff. Since we begin with cruising, the second cruise would be reached
		 * third cycle would be reached after 50 seconds, allowing a maximum of 40 seconds for a Shuttle to finish the rest of its code once the limit is reached.*/
		timeComplete = false;
		shuttlesFinished = 0;
		int numShuttles = Integer.parseInt(args[0]); //Obtain the numShuttles as the first (and only) program argument
		Station st; //Declare object that will help assure mutual exclusion of the recharge station between the Controller and the Shuttles.
		Runway run; //Declare object that will assure mutual exclusion of the takeoff tracks between the Supervisor and the Shuttles.
		Controller controller;
		Supervisor supervisor;
		
		st = new Station(3); //Set the number of recharge spaces to 3 (as given in the project specifications).
		run = new Runway(3); //Set the number of takeoff/landing tracks to three (as given in the project specifications).
		
		//Setup a Server thread so that we can begin listening for Client threads.
		if(args.length < 3)
		{
			System.err.println("Please provide the port number and host name as command line arguments and try again!");
			System.exit(1); //Exit the program with an error.
		}
		else
		{
			Server server = new Server(Integer.parseInt(args[2])); //Instantiate the server thread with the port number so that it establishes a connection.
			server.start(); //Have the server begin listening for client threads to connect.
			
			//Begin instantiating Shuttle threads
			ShuttleClient shuttles[] = new ShuttleClient[numShuttles]; //Declare an array that will be used to create and start each new Shuttle thread.
			
			for(int i = 0; i <numShuttles; i++)
			{
				//Obtain the host name and socket of the currently connected server from args[1] and args[2] respectively.
				shuttles[i] = new ShuttleClient(i+1, args[1], Integer.parseInt(args[2])); //Use i+1 as the name so that the shuttles can be numbered 1 through numShuttles.
				shuttles[i].start(); //Start the thread so its run method is executed. Every shuttle will cruise (sleep) to start.
			}
		}
		
		
		controller = new Controller(1, st, numShuttles); //Instantiate a new controller object.
		controller.start(); //Execute the run method of the controller.
		
		supervisor = new Supervisor(1, run, numShuttles); //Instantiate a new Supervisor object.
		supervisor.start(); //Execute the run method of the Supervisor.
		
		startTime = System.currentTimeMillis();
		
		while((System.currentTimeMillis()-startTime) < 60000) //Once a minute has passed, set timeComplete to false to indicate that the day has finished and shuttles can land.
		{	//See explanation as to why a minute was chosen for the day length above.
			timeComplete = false;
		}
			
		timeComplete = true; //Shuttles can now land.
		
	}
	
}
