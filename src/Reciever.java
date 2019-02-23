import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class Reciever implements Runnable{


	Socket soc=null;
	static String smartServerIP = "192.168.1.1";
	ConcurrentHashMap<Integer, String> expectedFrames;
	ConcurrentHashMap<Integer, ArrayList<Integer>> missingFrames;
	Reciever(Socket soc, ConcurrentHashMap<Integer, String> expectedFrames, ConcurrentHashMap<Integer, ArrayList<Integer>> missingFrames2){
		this.soc=soc;
		this.expectedFrames = expectedFrames;
		this.missingFrames = missingFrames2;
	}
	
	public void run() {
		try {
			ObjectOutputStream os = new ObjectOutputStream(soc.getOutputStream());
			ObjectInputStream is = new ObjectInputStream(soc.getInputStream());
			Frame frame;
			while((frame = (Frame) is.readObject())!=null) {
				int vId = frame.getVideoId();
				int fId = frame.getId();
				int cId = frame.getChunkId();
				if(expectedFrames.containsKey(vId)) {
					String frameData = expectedFrames.get(vId);
					ArrayList<Integer> missingFID = missingFrames.get(vId);
					ZonedDateTime ldt = ZonedDateTime.now();
					String[] tokens = frameData.split("_");
					int exp_FID = Integer.parseInt(tokens[0]);
					int exp_CID = Integer.parseInt(tokens[1]);
					System.out.println("Expected: "+exp_FID+": Recieved: "+fId+ "Video: " +vId+ " -------- "+ldt);
					if(exp_FID!=fId && exp_FID<fId) {
						if(!missingFID.contains(fId)) {
							System.out.println("Expected FrameId "+exp_FID+": Recieved FrameId: "+fId+ "Video: " +vId+ " -------- "+ldt);
							System.out.println(missingFID);
							TreeSet<Integer> chunkSet = new TreeSet<>();
							ZonedDateTime ldt1 = ZonedDateTime.now();
							System.out.print("Request Chunks "+" --- " + ldt1);
							for(int i=exp_CID; i<=cId; i++) {
								chunkSet.add(i);
								for(int j = 1; j<=30; j++) {
									missingFID.add((i-1)*30+j); //What chunks to request 
								}
							}
							missingFrames.put(vId, missingFID);

							requestChunks(vId, chunkSet);

						}
					}
					else {
						System.out.println(fId);
					}
					expectedFrames.put(vId, fId+1+"_"+cId);
				}
				else {
					System.out.println("First Frame"+frame.getId());
					expectedFrames.put(vId, fId+1+"_"+cId);
					ArrayList<Integer> mFrames = new ArrayList<>();
					missingFrames.put(vId, mFrames);
					

				}				
			}

		}
		catch(Exception E) {
			E.printStackTrace();
		}
		finally{
			try {
				soc.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	private void requestChunks(int videoId, TreeSet<Integer> chunkSet) {
		try {
			Socket syncserver = new Socket(smartServerIP, 8192);
			PrintWriter pw = new PrintWriter(syncserver.getOutputStream(), true);
			BufferedReader br = new BufferedReader(new InputStreamReader(syncserver.getInputStream()));
			pw.println("Request");
			pw.println(videoId);
			pw.println(chunkSet.size());
			ZonedDateTime ldt = ZonedDateTime.now();
			System.out.println("\nRequesting "+chunkSet.size()+" Chunks "+ " " +videoId +" --- " + ldt);
			for(int i: chunkSet) {
				pw.println(i);
				ZonedDateTime ldt1 = ZonedDateTime.now();
				System.out.println("Sent Chunk Request "+i+" --- " + ldt1);
			}
			br.close();
			pw.close();
			syncserver.close();
		}
		catch(Exception E) {
			E.printStackTrace();
		}
	}
	public static void main(String[] args) throws Exception {

		Runnable keepAlive = new Runnable(){
			public void run(){
				try{
					ServerSocket ssoc = new ServerSocket(8990);
					while(true){
						Socket socket = ssoc.accept();
						keepAliveServer rec=new keepAliveServer(socket);
						Thread thread=new Thread(rec);
						thread.start();
					}
				}
				catch(Exception E){E.printStackTrace();}
			}
		};
		new Thread(keepAlive).start();
		ConcurrentHashMap<Integer, String> expectedFrames = new ConcurrentHashMap<Integer, String>();
		ConcurrentHashMap<Integer, ArrayList<Integer>> missingFrames = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
		
		ServerSocket serverSocket = new ServerSocket(8193);
		while(true){
			Socket socket = serverSocket.accept();
			Reciever rec=new Reciever(socket,expectedFrames,missingFrames);
			Thread thread=new Thread(rec);
			thread.start();
		}
	}
}


class keepAliveServer implements Runnable{
	Socket soc = null;	
	keepAliveServer(Socket soc){
		this.soc=soc;

	}
	public void run() {
	}

}
