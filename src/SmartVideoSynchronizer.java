import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class SmartVideoSynchronizer implements Runnable{
	
	Socket soc=null;
	ConcurrentHashMap<Integer, serverData> videoData;

	
	
	public SmartVideoSynchronizer(Socket socket, ConcurrentHashMap<Integer, serverData> videos) {
		this.soc = socket;
		this.videoData = videos;
	}
	public void run() {
		try {
		    PrintWriter pw = new PrintWriter(soc.getOutputStream(), true);
		    BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
		    String str = br.readLine();
		    if(str.equals("Welcome")) {
		    	String ipAddress = br.readLine();
		    	int port = Integer.parseInt(br.readLine());
		    	int videoId = Integer.parseInt(br.readLine());
		    	System.out.println(ipAddress+" "+port+" "+videoId);
		    	serverData s = new serverData(ipAddress, port, videoId);
		    	videoData.put(videoId, s);
		    	pw.close();
		    	br.close();
		    	soc.close();
		    }
		    else if(str.equals("Request")) {
		    	int videoId = Integer.parseInt(br.readLine());
		    	int noOfChunks = Integer.parseInt(br.readLine());
		    	TreeSet<Integer> chunks = new TreeSet<Integer>();
		    	for(int i=0;i<noOfChunks;i++) {
		    		chunks.add(Integer.parseInt(br.readLine()));
		    	}
		    	requestChunks(videoId,chunks);
		    	pw.close();
		    	br.close();
		    	soc.close();
		    }

		}
		catch(Exception E) {
			E.printStackTrace();
		}

	}

	private void requestChunks(int videoId, TreeSet<Integer> chunks) {
		serverData sd = videoData.get(videoId);
		try {
			Socket senderConnect = new Socket(sd.getIpAddress(), sd.getPort());
			PrintWriter pw = new PrintWriter(senderConnect.getOutputStream(), true);
		    BufferedReader br = new BufferedReader(new InputStreamReader(senderConnect.getInputStream()));
		    pw.println(chunks.size());
		    for(int i: chunks)
		    	pw.println(i);
		    for(int i:chunks)
		    	System.out.println(br.readLine());
		    pw.close();
		    br.close();
		    senderConnect.close();
		}
		catch(Exception E) {
			E.printStackTrace();
		}
	}
	public static void main(String[] args) throws Exception {
		ConcurrentHashMap<Integer, serverData> videos = new ConcurrentHashMap<Integer, serverData>();
		ServerSocket serverSocket = new ServerSocket(8192);
		while(true){
			Socket socket = serverSocket.accept();
			SmartVideoSynchronizer rec = new SmartVideoSynchronizer(socket,videos);
			Thread thread=new Thread(rec);
			thread.start();
		}
	}
		
	
	class serverData{
		String ipAddress;
		int port;
		int videoId;
		serverData(String ipAddress, int port, int videoId){
			this.ipAddress = ipAddress;
			this.port = port;
			this.videoId = videoId;
		}
		protected String getIpAddress() {
			return ipAddress;
		}
		protected int getPort() {
			return port;
		}
		protected int getVideoId() {
			return videoId;
		}

	}
	
}