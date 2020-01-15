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

		private long getCurrentServerTime() {
			return System.currentTimeMillis() + 1100L;
		}

		@Override
		public void run() {
			
			try {
				ObjectInputStream objectInputStream = new ObjectInputStream(this.client.getInputStream());
				NTPRequest ntpRequest = (NTPRequest) objectInputStream.readObject();
				long ntpRequestTimestamp = getCurrentServerTime();
				ntpRequest.setT2(ntpRequestTimestamp);
				System.out.println("[Thread-" + Thread.currentThread().getId() + "]" + SERVER_TAG + "Got NTP Request at :"+ ntpRequestTimestamp + "\n");
				sendNTPAnswer(ntpRequest);
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

		}

		private void sendNTPAnswer(NTPRequest request) {
			try {
				Random random = new Random();
				threadSleep(10 + random.nextInt(90));

				ObjectOutputStream objectOutputStream = new ObjectOutputStream(this.client.getOutputStream());
				long ntpResponseTimestamp = getCurrentServerTime();
				request.setT3(ntpResponseTimestamp);
				System.out.println("[Thread-" + Thread.currentThread().getId() + "]" + SERVER_TAG + "Sending NTP Response at :"+ ntpResponseTimestamp + "\n");
				objectOutputStream.writeObject(request);
				objectOutputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
