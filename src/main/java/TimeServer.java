import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TimeServer {
	private static int PORT = 27780;
	private ServerSocket serverSocket;

	public TimeServer() {
		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("Server started on port: " + PORT);

			// Keep the server running indefnitely and delegate any new  NTP requests to the Executor Thread Pool
			while (true) {
				NTPRequestHandler ntpRequestHandler = new NTPRequestHandler(serverSocket.accept());
				ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
				executor.execute(ntpRequestHandler);
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				serverSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
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
		new TimeServer();
	}

	private class NTPRequestHandler implements Runnable {
		private Socket client;
		private String SERVER_TAG = "[SERVER]:";

		public NTPRequestHandler(Socket client) {
			this.client = client;
		}

		// Induce an artificial offset of 1100 milliseconds on the server
		private long getCurrentServerTime() {
			return System.currentTimeMillis() + 1100L;
		}

		@Override
		public void run() {
			
			try {
				// Receive the NTPRequest sent by the client and set the current server time as T2
				ObjectInputStream objectInputStream = new ObjectInputStream(this.client.getInputStream());
				NTPRequest ntpRequest = (NTPRequest) objectInputStream.readObject();
				long ntpRequestTimestamp = getCurrentServerTime();
				ntpRequest.setT2(ntpRequestTimestamp);
				System.out.println("[Thread-" + Thread.currentThread().getId() + "]" + SERVER_TAG + "Got NTP Request at :"+ ntpRequestTimestamp + "\n");
				
				// Set the current server time as T3 and send the NTP response
				long ntpResponseTimestamp = getCurrentServerTime();
				ntpRequest.setT3(ntpResponseTimestamp);
				System.out.println("[Thread-" + Thread.currentThread().getId() + "]" + SERVER_TAG + "Sending NTP Response at :"+ ntpResponseTimestamp + "\n");
				sendNTPAnswer(ntpRequest);
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

		}

		private void sendNTPAnswer(NTPRequest request) {
			try {
				// Artificially induced communication delay of 10 - 100 milli seconds
				Random random = new Random();
				threadSleep(10 + random.nextInt(90));

				// Send the NTP request object in which T1,T2,T3 is already set
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(this.client.getOutputStream());
				objectOutputStream.writeObject(request);
				objectOutputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
