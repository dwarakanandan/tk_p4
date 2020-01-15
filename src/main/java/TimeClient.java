import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class TimeClient {
	private static String hostUrl = "127.0.0.1";
	private static int PORT = 27780;
	private static String CLIENT_TAG = "[CLIENT]: ";
	private Double minD;
	private NTPRequest minNTPrequest;
	private Socket socket;

	public TimeClient() {

		try {
			
			for (int i = 0; i < 10; i++) {
				NTPRequest ntpRequest = new NTPRequest();
				socket = new Socket(InetAddress.getByName(hostUrl), PORT);

				// Send an NTP Request by setting the current client time as T1 in the ntpRequest
				long ntpRequestTimestamp = System.currentTimeMillis();
				ntpRequest.setT1(ntpRequestTimestamp);
				System.out.println(CLIENT_TAG + "Sending NTP Request at: " + ntpRequestTimestamp + "\n");
				sendNTPRequest(ntpRequest);

				// Receive the NTP Response and the current client time as T4
				ObjectInputStream ntpResponseInputStream = new ObjectInputStream(socket.getInputStream());
				try {
					ntpRequest = (NTPRequest) ntpResponseInputStream.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				long ntpResponseTimestamp = System.currentTimeMillis();
				ntpRequest.setT4(ntpResponseTimestamp);
				System.out.println(CLIENT_TAG + "Got NTP Response at: " + ntpResponseTimestamp + "\n");
				
				// We now have an NTPRequest object with all 4 values T1,T2,T3,T4 set.
				// Calculate Offset and Delay
				ntpRequest.calculateOandD();
				System.out.println(CLIENT_TAG + "Iteration: " + i + " Estimated offset = " + ntpRequest.o + " , Delay = " + ntpRequest.d + "\n");
				
				// If the current m and m' pair has the minimum overall delay. Cache it
				if ( (i == 0) || ntpRequest.d < this.minD) {
					this.minD = ntpRequest.d;
					this.minNTPrequest = ntpRequest;
				}

				socket.close();

				// Wait 350 milliseconds before sending the next request
				threadSleep(350);
			}
			socket.close();

			System.out.println(CLIENT_TAG + "Estimated offset corresponding to minimum delay = " + this.minNTPrequest.o + " , Minumum Delay = " + this.minNTPrequest.d + "\n");

			if (this.minNTPrequest.o >= 0) {
				System.out.println(CLIENT_TAG + "Server is approximately " + this.minNTPrequest.o + " milliseconds ahead of self\n");
			} else {
				System.out.println(CLIENT_TAG + "Server is approximately " + this.minNTPrequest.o + " milliseconds behind self\n");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private void sendNTPRequest(NTPRequest request) {
		try {
			// Artificially induced communication delay of 10 - 100 milli seconds
			Random random = new Random();
			threadSleep(10 + random.nextInt(90));

			// Send the NTP request object in which T1 is already set
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
			objectOutputStream.writeObject(request);
			objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void threadSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new TimeClient();
	}

}
