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
				long ntpRequestTimestamp = System.currentTimeMillis();
				ntpRequest.setT1(ntpRequestTimestamp);
				System.out.println(CLIENT_TAG + "Sending NTP Request at: " + ntpRequestTimestamp + "\n");
				sendNTPRequest(ntpRequest);
				ObjectInputStream ntpResponseInputStream = new ObjectInputStream(socket.getInputStream());
				try {
					ntpRequest = (NTPRequest) ntpResponseInputStream.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				long ntpResponseTimestamp = System.currentTimeMillis();
				ntpRequest.setT4(ntpResponseTimestamp);
				System.out.println(CLIENT_TAG + "Got NTP Response at: " + ntpResponseTimestamp + "\n");
				ntpRequest.calculateOandD();
				System.out.println(CLIENT_TAG + "Iteration: " + i + " Estimated offset = " + ntpRequest.o + " , Delay = " + ntpRequest.d + "\n");
				if ( (i == 0) || ntpRequest.d < this.minD) {
					this.minD = ntpRequest.d;
					this.minNTPrequest = ntpRequest;
				}
				socket.close();
				threadSleep(350);
			}
			socket.close();

			System.out.println(CLIENT_TAG + "Estimated offset corresponding to minimum delay = " + this.minNTPrequest.o + " , Minumum Delay = " + this.minNTPrequest.d + "\n");

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private void sendNTPRequest(NTPRequest request) {
		try {
			Random random = new Random();
			threadSleep(10 + random.nextInt(90));

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
