import java.util.Vector;

public class Runway {

	private int numTracks;
	private Vector<Object> waitingToTakeoff; //Vector that will hold objects containing Shuttles waiting for a takeoff track.
	private Boolean trackAvailable; //In
	public int numToTakeoff;
	private Object clearForTakeoff;
	private Boolean trackClear;
	
	public Runway(int tracks)
	{
		numTracks = tracks;
		waitingToTakeoff = new Vector<Object>();
		trackAvailable = false;
		trackClear = false;
		clearForTakeoff = new Object();
	}
	
	public void takeoff() //Code to be executed by the Shuttles.
	{
		try
		{
			Object readyForTakeoff = new Object();
			waitingToTakeoff.add(readyForTakeoff);
			synchronized(waitingToTakeoff.get(waitingToTakeoff.indexOf(readyForTakeoff)))
			{
				while(!trackAvailable || waitingToTakeoff.indexOf(readyForTakeoff) > 0) //Use !trackAvaialble to make sure that the Shuttle at Vector position 0 does not takeoff unless notified by the Supervisor.
				{
					waitingToTakeoff.get(waitingToTakeoff.indexOf(readyForTakeoff)).wait();
				}
			}
			
			numToTakeoff--; //Indicates that a Shuttle has taken off.
			trackClear = true; //Track is clear for the next Shuttle.
			
			synchronized(clearForTakeoff)
			{
				clearForTakeoff.notify();
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void signalTakeoff() //Code to be executed by the Supervisor.
	{
		//findNumToTakeoff();
		
		try {
			//signalClearForTakeoff();
			
			synchronized(clearForTakeoff)
			{
				while(!trackClear)
				{	//Since we must signal the Shuttles one by one, the Supervisor must wait until the first shuttle clears the runway before it can signal the next.
					clearForTakeoff.wait();
				}
			}
			
			trackClear = false; //A plane was just signaled to take off, make trackClear false to avoid having the next Shuttle takeoff when element zero is removed from the Vector.
			waitingToTakeoff.removeElementAt(0);
		
		trackAvailable = false; //Set trackAvaialble to false once all three Shuttle have taken off to make sure elementAt(0) continues to wait.
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
		
}
	
	public void findNumToTakeoff()
	{
		if(waitingToTakeoff.size() < numTracks && waitingToTakeoff.size() > 0) //There are 1-3 Shuttles waiting to take off.
		{
			numToTakeoff = waitingToTakeoff.size();
		}
		else if(waitingToTakeoff.size() >= numTracks) //Three or greater shuttles are waiting to takeoff.
		{
			numToTakeoff = numTracks; //Set the number of Shuttles to take off equal to the number of tracks (3)
		}
		
	}
	
	public void signalClearForTakeoff() throws InterruptedException
	{
		trackAvailable = true; //Allow the Shuttle at position zero of the Vector to move to the takeOff track.
		synchronized(waitingToTakeoff.elementAt(0)) 
		{
			waitingToTakeoff.elementAt(0).notify(); //Signal to the first element in the queue that they can take off.
		}
		
	}
	
}
