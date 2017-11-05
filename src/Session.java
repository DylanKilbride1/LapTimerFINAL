

import java.util.Vector;



public class Session {

	Vector<Lap> laps = new Vector<Lap>();
	LapTimer lt;
	float totalTime = 0;
	float average = 0;
	
	public Session() {

		laps = new Vector<Lap>();

	}
	
	public void addLap(Lap newLap) {
		
		laps.add(newLap);
		
	}
	
	public void display(){      /****** For testing only ******/
		
		for (Lap aLap : laps) {   
		    System.out.println(aLap.getId() + " " + aLap.getLapTime());
		}
		
	}
	
	public float calculateAverageTime() {
		
		/* This method should calculate the
		   average time of all laps in the 
		   collection. It needs to return a 
		   float value */
		
		for (Lap aLap : laps) {   
		   totalTime = totalTime + aLap.getLapTime();
		}
		
		//System.out.println(totalTime / laps.size()); // Working correct until here
		
		average = totalTime / laps.size();
		
		return average;
	}
	
	
	public Lap getFastestLap() {
		
		/* This method should step through the
		   collection, and return the Lap object
		   whose lap time is smallest (fastest). */
		Lap shortestLap = laps.firstElement();
		
		
		for (Lap aLap : laps) {   
			   if(aLap.getLapTime() < shortestLap.getLapTime()){
				   shortestLap = aLap;
			   }
			}
		
		return shortestLap;   
		
	}
	
	public Lap getSlowestLap() {
		
		/* This method should step through the
		   collection, and return the Lap object
		   whose lap time is largest (slowest). */
		
		Lap longestLap = laps.firstElement();
		
		
		for (Lap aLap : laps) {   
			   if(aLap.getLapTime() > longestLap.getLapTime()){
				   longestLap = aLap;
			   }
			}
		
		return longestLap;   
		
	}
	
	public void setAverage(){
		average = 0;
	}
	
	public void setTotalTime(){
		totalTime = 0;
	}
}
	