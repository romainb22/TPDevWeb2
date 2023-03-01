package tp2;

import java.util.Random;

public class Car implements Runnable {

	public final int WAITING = 0;
	public final int PARKED = 1;
	public final int LEAVING = 2;

	private final String PLATE;
	private final int DURATION;
	private final int PATIENCE;
	private final Parking choosenParking;
	private volatile int state;

	public Car(Parking parking) {
		PLATE = randomPlateGenerator();
		DURATION = new Random().nextInt(30000);
		PATIENCE = new Random().nextInt(DURATION + DURATION / 2);
		choosenParking = parking;
	}

	private String randomPlateGenerator() {
		String plate = "";
		Random r = new Random();
		plate += (char) (r.nextInt(26) + 'A');
		plate += (char) (r.nextInt(26) + 'A');
		plate += '-';
		plate += (char) (r.nextInt(9) + '0');
		plate += (char) (r.nextInt(9) + '0');
		plate += (char) (r.nextInt(9) + '0');
		plate += '-';
		plate += (char) (r.nextInt(26) + 'A');
		plate += (char) (r.nextInt(26) + 'A');
		return plate;
	}

	@Override public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			this.parkingArrival();
			long time = 0;
			long date = System.currentTimeMillis();
			//Voiture dans file d'attente
			while(state == WAITING) {
				if(time > PATIENCE) {
					departureParkingImpatience();
				} else {
					time = System.currentTimeMillis() - date;
				}
			}
			//Voiture garee
			while(state == WAITING) {
				try {
					Thread.sleep(DURATION);
				} catch(InterruptedException e) {
					throw new RuntimeException(e);
				}
				this.departureParking();
			}

		}
	}

	public String getPLATE() {
		return PLATE;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public synchronized void parkingArrival() {
		choosenParking.carArrival(this);
		state = WAITING;
		System.out.println("Beep Beep, the car matriculated " + getPLATE() + " is waiting");
	}

	public synchronized void departureParkingImpatience() {
		choosenParking.carDepartureImpatience(this);
		this.state = LEAVING;
		Thread.currentThread().interrupt();
		System.out.println("The car matriculated " + getPLATE() + " has grown tired of waiting, it's now long gone");
	}

	public synchronized void departureParking() {
		choosenParking.carDeparture(this);
		this.state = LEAVING;
		System.out.println("The car matriculated " + getPLATE() + " is leaving the parking");
		Thread.currentThread().interrupt();
		System.out.println("The queue process is not anymore on stand by");
	}

}
