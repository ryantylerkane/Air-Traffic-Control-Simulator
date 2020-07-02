
public class Controller extends Thread {
	
	public static long time = System.currentTimeMillis();
	private Station st;
	private int numShuttles; //The number of shuttles that have not yet finished three cycles.
	
	public Controller(int id, Station station, int shuttles) {
	setName("Controller-"+ id);
	st = station;
	numShuttles = shuttles;
	}
	
	public void msg(String m) {
	System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+": "+m);
	}
	
	public void run()
	{ 	
		while(numShuttles>Driver.shuttlesFinished)//Keep iterating until all of the Shuttles have completed three cycles.
		{	
				try {
					Thread.sleep(5000); //Open the recharge station every 'half hour'
					if(numShuttles>Driver.shuttlesFinished)//Don't output message if all Shuttles have terminated.
						msg("Has opened the recharge station.");
					st.openRecharge();
				} catch (InterruptedException e) {
					e.printStackTrace();
					}			
		}
		msg("Has reached the end of its run method.");
	}

}
