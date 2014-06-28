/*
This is the main class that just creates a new thread for the Receiver class and starts it.
Contributors: Venkata Jaswanth
*/

public class Main {
	public static void main(String args[]) {
		Thread mainThread = new Thread(new Receiver(1500));
		mainThread.start();
		System.out.println("Server started successfully!");
	}
}
