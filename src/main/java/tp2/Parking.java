package tp2;

import java.util.ArrayList;
import java.util.LinkedList;

public class Parking implements Runnable {

	private final int CAPACITY = 2;
	private volatile ArrayList<Car> parked;
	private volatile LinkedList<Car> queue;

	public Parking() {
		parked = new ArrayList<>();
		queue = new LinkedList<>();
	}

	public static void main(String[] args) {
		Parking park = new Parking();
		Car c1 = new Car(park);
		Car c2 = new Car(park);
		Car c3 = new Car(park);
		Car c4 = new Car(park);
		Thread threadC1 = new Thread(c1);
		Thread threadC2 = new Thread(c2);
		Thread threadC3 = new Thread(c3);
		Thread threadC4 = new Thread(c4);
		Thread threadPark = new Thread(park);
		threadPark.start();
		threadC1.start();
		threadC2.start();
		threadC3.start();
		threadC4.start();
		try {
			threadC1.join();
			threadC2.join();
			threadC3.join();
			threadC4.join();
		} catch(InterruptedException e) {
			throw new RuntimeException(e);
		}
		threadPark.interrupt();
	}

	public synchronized boolean isFull() {
		return parked.size() >= CAPACITY;
	}

	public synchronized boolean hasQueue() {
		return queue.size() >= 0;
	}

	@Override public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			if(hasQueue()) {
				carArrival(queue.poll());
			}
		}
		System.out.println("Fin du thread du parking");
	}

	public synchronized void carArrival(Car car) {
		if(car == null) {
			return;
		}
		if(isFull()) {
			System.out.println("The parking is full, adding car matriculated " + car.getPLATE() + " to queue");
			try {
				wait();
			} catch(InterruptedException e) {
				throw new RuntimeException(e);
			}
		} else {
			queue.remove(car);
			parked.add(car);
			System.out.println("The car matriculated " + car.getPLATE() + " is parked on space: " + parked.indexOf(car));
			car.setState(car.PARKED);
		}
	}

	public synchronized void carDeparture(Car car) {
		parked.remove(car);
		notifyAll();
	}

	public synchronized  void carDepartureImpatience(Car car) {
		this.queue.remove(car);
	}
}
