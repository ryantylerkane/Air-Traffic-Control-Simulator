
public class Supervisor extends Thread{
	
public static long time = System.currentTimeMillis();
private Runway run;
private int numShuttles;

	public Supervisor(int id, Runway runway, int shuttles) 
	{
		setName("Supervisor-"+ id);
		run = runway;
		numShuttles = shuttles;
	}
	
	public void msg(String m) {
	System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
	}
	
	
	public void run()
	{
		while(numShuttles>Driver.shuttlesFinished) //Keep iterating until all of the Shuttles have completed three cycles.
		{
			try {
				Thread.sleep(7000);//Sleep for seven seconds.
				if(numShuttles>Driver.shuttlesFinished) //Don't output the message if all of the shuttles have terminated.
					msg("Has signaled the shuttles for takeoff.");
				run.signalTakeoff();
			} catch (InterruptedException e) { //Thread should not be interrupted, this will likely not be reached.
				e.printStackTrace();
			}
		}
		msg("Has reached the end of its run method.");
	}

}
