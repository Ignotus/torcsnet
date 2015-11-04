import cicontest.algorithm.abstracts.AbstractRace;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.Driver;
import cicontest.torcs.controller.Human;

public class DefaultRace extends AbstractRace {

	public int[] runQualification(DefaultDriverGenome[] drivers, boolean withGUI){
		DefaultDriver[] driversList = new DefaultDriver[drivers.length + 1 ];
		for(int i=0; i<drivers.length; i++){
			driversList[i] = new DefaultDriver();
			driversList[i].loadGenome(drivers[i]);
		}
		return runQualification(driversList, withGUI);
	}

	
	public int[] runRace(DefaultDriverGenome[] drivers, boolean withGUI){
		int size = Math.min(10, drivers.length);
		DefaultDriver[] driversList = new DefaultDriver[size];
		for(int i=0; i<size; i++){
			driversList[i] = new DefaultDriver();
			driversList[i].loadGenome(drivers[i]);
		}
		return runRace(driversList, withGUI, true);
	}

	
	
	public void showBest(){
		if(DriversUtils.getStoredGenome() == null ){
			System.err.println("No best-genome known");
			return;
		}
		
		DefaultDriverGenome best = (DefaultDriverGenome) DriversUtils.getStoredGenome();
		DefaultDriver driver = new DefaultDriver();
		driver.loadGenome(best);
		
		DefaultDriver[] driversList = new DefaultDriver[1];
		driversList[0] = driver;
		runQualification(driversList, true);
	}
	
	public void showBestRace(){
		if(DriversUtils.getStoredGenome() == null ){
			System.err.println("No best-genome known");
			return;
		}
	
		DefaultDriver[] driversList = new DefaultDriver[1];
		
		for(int i=0; i<10; i++){
			DefaultDriverGenome best = (DefaultDriverGenome) DriversUtils.getStoredGenome();
			DefaultDriver driver = new DefaultDriver();
			driver.loadGenome(best);
			driversList[i] = driver;
		}
		
		runRace(driversList, true, true);
	}
	
	public void raceBest(){
		
		if(DriversUtils.getStoredGenome() == null ){
			System.err.println("No best-genome known");
			return;
		}
		
		Driver[] driversList = new Driver[10];
		for(int i=0; i<10; i++){
			DefaultDriverGenome best = (DefaultDriverGenome) DriversUtils.getStoredGenome();
			DefaultDriver driver = new DefaultDriver();
			driver.loadGenome(best);
			driversList[i] = driver;
		}
		driversList[0] = new Human();
		runRace(driversList, true, true);
	}
}
