public class Shuttle extends Thread {
	
	public static long time = System.currentTimeMillis();
	private int cruiseNum; 
	private Station st;
	private Runway run;
	
	public Shuttle(int id, Station station, Runway runway) {
		cruiseNum = 1; //Initialize a variable that will be used to keep count of the number of cruises a Shuttle has reached.
		setName("Shuttle-"+ id);
		st = station;
		run = runway;
	}
	
	
	public void msg(String m) {
	System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
	}
	
	public void run()
	{
		try {
		while(!Driver.timeComplete)//Have the shuttles keep cycling until every shuttle has completed at least three trips.
		{ 
				//Each shuttle will begin by sleeping to represent cruising in the air.
				msg("Is cruising (Trip #"+cruiseNum+").");
				Thread.sleep((long)(Math.random()*8000)); //Use random number * 8000 to represent a value between 0 to 8 seconds to represent cruising.
				msg("Has landed and is moving to the recharge station.");
				Thread.sleep((long)(Math.random()*2000)); //Use random number * 2000 to represent a value between 0 to 2 seconds to represent moving to the recharge station.
				msg("Is waiting to access the recharge station.");
				st.waitForRecharge();
				st.recharge(); //Once the shuttle is done cruising, it must refuel.
				msg("Is now refueled and is moving toward the runway.");
				Thread.sleep((long)(Math.random()*2000)); //Simulate moving to takeoff area.
				msg("Has reached the runway and is waiting to takeoff.");
				run.takeoff(); //Plane takes off from runway if one is available.
				msg("Is taking off.");
				cruiseNum++; //Increment the number of cruises by one.
		}
		Thread.sleep((long)(Math.random()*2000)); //Sleep for a random amount of time to avoid having two threads finish at the exact same time. Such a scenario results in the static variable incrementing improperly.
		Driver.shuttlesFinished++; //Mark the shuttle as finished so Controller and Supervisor know when to terminate.
		msg("Has reached the end of its run method.");
		}
		catch (InterruptedException e) { //Not expected to be reached.
				e.printStackTrace();
		} 
	}
}
