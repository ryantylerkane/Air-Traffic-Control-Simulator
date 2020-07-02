import java.io.IOException;


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
		ControllerClient controller;
		SupervisorClient supervisor;
		
		//Setup a Server thread so that we can begin listening for Client threads.
		if(args.length < 3)
		{
			System.err.println("Please provide the host name and port number as command line arguments and try again!");
			System.exit(1); //Exit the program with an error.
		}
		else
		{
			ShuttleClient shuttles[] = new ShuttleClient[numShuttles]; //Declare an array that will be used to create and start each new Shuttle thread.
			
			for(int i = 0; i <numShuttles; i++)
			{
				shuttles[i] = new ShuttleClient(i+1, args[1], Integer.parseInt(args[2])); //Use i+1 as the name so that the shuttles can be numbered 1 through numShuttles. Obtain the host name and port from the command line arguments.
				shuttles[i].start(); //Start the thread so its run method is executed and a connection is made to the Server.
			}
		}
		
		controller = new ControllerClient(1, args[1], Integer.parseInt(args[2]), numShuttles); //Instantiate a new controller object.
		controller.start(); //Execute the run method of the controller.
		
		supervisor = new SupervisorClient(1, args[1], Integer.parseInt(args[2]), numShuttles); //Instantiate a new Supervisor object.
		supervisor.start(); //Execute the run method of the Supervisor.
		
		startTime = System.currentTimeMillis();
		
		while((System.currentTimeMillis()-startTime) < 63000) //Once a minute has passed, set timeComplete to false to indicate that the day has finished and shuttles can land. An extra three seconds was added to compensate for the additional time for setup resulting from creating ClientServer threads prior to execution.
		{	//See explanation as to why a minute was chosen for the day length above.
			timeComplete = false;
		}
			
		timeComplete = true; //Shuttles can now land.
		
	}
	
}
