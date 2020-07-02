import java.util.Vector;
import java.util.Random;

public class Station { //Class object that will be used to maintain mutual exclusion between Shuttles and the Controller.

	private int numRecharge; //Number of Shuttles that can be in the garage at any time.
	private int reservoir; //Tank in which Shuttles will extract fuel.
	private Random random; //Random variable that will be used to generate random numbers.
	private int tankCapacity; //Will hold a random number meant to represent the amount of fuel that a Shuttle will require.
	private int numInObject; //Holds the number of threads currently waiting on object at waitingToRecharge(0).
	private int numFueled; //Holds the number of threads that are in the recharge station (across all objects).
	private Boolean rechargeIsAvailable; //Will indicate if the rechrageStation is available for Shuttles to enter.
	private Boolean reservoirIsEmpty; //Will be used to indicate whether more fuel is needed for the Shuttles.
	private int numWaitingToRecharge; //Keep track of how many shuttles are waiting to enter the recharge station so we can assure that there are groups of three ready to re-charge.
	public Vector<Object> waitingToRecharge; //Will hold the objects (groups of three shuttles) that the shuttles are waiting on to enter the recharge station.
	private Object reservoirEmpty; //Object that the Controller will wait on until fuel is needed by the Shuttles.
	private Object needFuel; //Object a Shuttle will wait on when more fuel is needed to refill.

	public Station(int recharge)
	{
		numRecharge = recharge;
		reservoir = 200;
		random = new Random();
		rechargeIsAvailable = false;
		reservoirIsEmpty = false;
		waitingToRecharge = new Vector<Object>();
		reservoirEmpty = new Object();
		needFuel = new Object();
		numFueled=0;
	}
	
	public void waitForRecharge()
	{
		Object myObject; //Will be used to represent the notification object a thread is waiting on.
		
		if(numWaitingToRecharge % numRecharge == 0) //Since the shuttles wait on the notification object in groups of numRecharge, we must check if we need to make a new object.
		{
			//All objects in the vector are full, so we need to make one of our own.
			myObject = new Object();
			waitingToRecharge.add(myObject); //Add the new object onto the end of the vector.
		}
		else //There is a group that is not full.
		{
			if(waitingToRecharge.size() ==0) //Need to check this again to avoid scenarios where a Shuttle enters just as an object is removed prior to numWaitingToRecharge is updated.
			{	//In this case, size of the Vector will not reflect the correct state of the waiting Shuttles.
				myObject = new Object();
				waitingToRecharge.add(myObject);
			}
			else
			{
				myObject = waitingToRecharge.get(waitingToRecharge.size()-1); //Add the Shuttle to the most recent object.
			}
		}
		numWaitingToRecharge++; //Indicate that a Shuttle is waiting to access the recharge station.
		synchronized (waitingToRecharge.get(waitingToRecharge.indexOf(myObject))) {
	         while (!rechargeIsAvailable || waitingToRecharge.indexOf(myObject) != 0)
				try {
					waitingToRecharge.get(waitingToRecharge.indexOf(myObject)).wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} //Wait on the notification object until the recharge station is open.
	         }
	}
	
	public void recharge() //Code to be executed by Shuttles after they are done cruising.
	{
		try
		{	
			tankCapacity = random.nextInt(100 - 50 + 1) + 50; //Generate a number between 50 and 100.
			if(reservoir-tankCapacity < 0) //Filling up would empty the reservoir.
			{
				reservoirIsEmpty = true; //Indicate the reservoir is empty so that the Controller can exit its wait loop.
				synchronized(reservoirEmpty)
				{
					reservoirEmpty.notify(); //Signal the Controller to re-fill the reservoir.
				}
				
				synchronized(needFuel)
				{
					while(reservoirIsEmpty)
					{
						needFuel.wait(); //Wait until the Controller re-fills the reservoir.
					}
				}
				
			}
				reservoir-=tankCapacity; //Remove a random number from 50-75 from the reservoir.
				numFueled++; //Indicate that a thread has been fueled.
				synchronized(waitingToRecharge.get(0))
				{
					waitingToRecharge.get(0).notify(); //Notify the next Shuttle in the group (if there even is one).
				}
			if(numFueled==numInObject || numFueled==3) //All threads from object zero are now all fueled.
			{
				//It is possible that a Shuttle arrives in the middle of recharging a set of three objects, causing numFueled to increase beofore a Shuttle
				//Waiting on the object is refueled. Check to make sure that numFueled does not exceed three if this does happen.
				rechargeIsAvailable = false; //Set this to false so that the next object does not enter the station before the Controller signals it. 
				numWaitingToRecharge-=numFueled;
				waitingToRecharge.remove(0); //Remove the group that was just refueled from the waiting Vector.
				numFueled = 0;
				synchronized(reservoirEmpty)
				{
					reservoirEmpty.notify(); //Signal the Controller that all shuttles are ready to leave.
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void openRecharge() throws InterruptedException //Code to be executed by the Controller every five seconds.
	{
		try
		{
			if(waitingToRecharge.size() > 0) //if there is a group of shuttles waiting to enter the charging station.
			{
				synchronized (waitingToRecharge.get(0)) 
				{
					if(numWaitingToRecharge < numRecharge) //The notified object must have fewer than numRecharge ships waiting.
					{
						//Since the Controller has to open the garage every half hour, let the ships in even if there number is less than the number of spots in the recharge station.
						numInObject = numWaitingToRecharge;
					}
					else //The object that was notified has numRecharge amount of Shuttles waiting on it.
					{
						numInObject = numRecharge;
					}
					rechargeIsAvailable = true; //Indicate that the recharge station is available for the next group of shuttles.
					waitingToRecharge.get(0).notify(); //Notify first shuttle in the group. The platoon policy will be used to get the rest of the Shuttles waiting on the object.
				}
				
				//waitToRefuel();
				//notifyRefuel();	
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void waitToRefuel() throws InterruptedException
	{
		synchronized(reservoirEmpty)
		{
			while(!reservoirIsEmpty && rechargeIsAvailable)
			{
				reservoirEmpty.wait(); //Wait until a Shuttle requires more fuel in the reservoir or all of the shuttles are done.
			}
		}
		
		reservoir = 200; //Refill the reservoir.
		reservoirIsEmpty = false; //Indicate that it is refilled so that the waiting shuttle exits its loop.
		
	}
	
	public void notifyRefueled()
	{
		synchronized(needFuel)
		{
			needFuel.notify(); //Notify the waiting Shuttle that there is available fuel.
		}
	}
}
	

