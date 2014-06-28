import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
/*
Contributors: Vishesh Kandhari, Venkata Jaswanth
*/

import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/*This class is the heart of the server application.*/
public class Receiver implements Runnable {
	int port;
	int factorX, factorY;
	double plotx, ploty;
	String data;
	Point p;
	double mobile_width = 320;
	double mobile_height = 480;

	public Receiver(int port) {
		this.port = port;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth();
		double height = screenSize.getHeight();
		factorX = (int) (width / mobile_width);
		factorY = (int) (height / mobile_height);
	}

	@Override
	public void run() {
		Robot robot = null;
		try {
			robot = new Robot();
		} catch (AWTException e) {
			System.out.println("Couldn't instantiate Robot object!");
			e.printStackTrace();
			return;
		}
		ServerSocket server = null;
		Socket client = null;
		BufferedReader br = null;
		try {
			server = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Failed to start server on port " + port);
			e.printStackTrace();
			return;
		}
		try {
			
			int cx = 0, cy = 0, dx = 0, dy = 0, bx = 0, by = 0;
			Coords prevCoords1 = null, prevCoords2 = null;
			String[] ar = null, as = null;
			
			//An infinite loop. In every iteration a new socket is opened, data is fetched and corresponding mouse event is triggered. Then the socket is closed.
			while (true) {
				System.out.println("SEV");
				client = server.accept();
				//Getting the input stream for the socket
				br = new BufferedReader(new InputStreamReader(
						client.getInputStream()));
				//We are only interested with the data passed in the GET request. The line for that starts with GET.
				data=br.readLine();
				while(data!=null && !data.startsWith("GET"))
				{
					data=br.readLine();
				}
				System.out.println("Received : " + data);
				
				//Extracting the useful data from the string
				data = data.split("\\?")[1].split(" ")[0];
				System.out.println("Command : " + data);
				// "action#x:y"
				ar = data.split("@");
				
				//Taking actions according to the action specified in the fetched data.
				switch (ar[0]) {
				//Touch event on the touch pad
				case "MouseDown":
					p = MouseInfo.getPointerInfo().getLocation();
					bx = p.x;
					by = p.y;
					prevCoords1 = null;
					break;
				//Left mouse button press
				case "MousePress":
					robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					break;
				//Left mouse button release
				case "MouseRelease":
					robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					break;
				//Mouse move by dragging on touch pad
				case "MouseMove":
					p = MouseInfo.getPointerInfo().getLocation();
					bx = p.x;
					by = p.y;
					as = ar[1].split(":");
					cx = Integer.parseInt(as[0]);
					cy = Integer.parseInt(as[1]);
					if (prevCoords1 == null) {
						prevCoords1 = new Coords(cx, cy);
					} else {
						prevCoords2 = new Coords(cx, cy);
						dx = prevCoords2.x - prevCoords1.x;
						dy = prevCoords2.y - prevCoords1.y;
						bx += dx * 0.5;
						by += dy * 0.5;
						//plotx = bx * 0.2;
						//ploty = by * 0.2;
						robot.mouseMove(bx, by);
					}
					break;
				//Right click button release
				case "MouseRightClick":
					prevCoords1 = null;
					robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
					robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
					break;
				}
				//For closing server
				if (data.equals("BYE"))
					break;
				client.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			System.out.println("Caught : " + data);
			server.close();
		} catch (IOException e) {
			System.out.println("Failed to shutdown server!");
			e.printStackTrace();
		}
	}
}

//Class for the mouse co-ordinates
class Coords {
	int x, y;

	Coords(int x, int y) {
		this.x = x;
		this.y = y;
	}
}