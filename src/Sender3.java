import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.imageio.ImageIO;

public class Sender3 implements Runnable{
	final static String INDEXING_PATH = "/home/sdnip/Downloads/SDNVideo/imageData/thumb";
	static int fps = 30;
	static Buffer buffer = new Buffer(4);
	static int chunkId = 1;
	static int frameId = 1;
	static int videoId = 3;
	static Chunk chunk = new Chunk(chunkId,videoId);
	static Socket socket;
	static ObjectOutputStream os;
	static int serverPort = 8196;
	static String serverIP = "192.168.2.1";
	static String smartServerIP = "192.168.1.1"; 
	static String senderIp = "192.168.1.1";
			
	static Boolean check = false;

	Socket serverSocket = null;

	Sender3(Socket serverSocket){
		this.serverSocket = serverSocket;
	}

	public void run() {
		try {
			PrintWriter pw = new PrintWriter(serverSocket.getOutputStream(), true);
			BufferedReader br = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			int noOfChunks = Integer.parseInt(br.readLine());
			for(int i=0;i<noOfChunks;i++) {
				int chunkId = Integer.parseInt(br.readLine());
				if(buffer.containsId(chunkId)) {
					Chunk chunk = buffer.getChunk(chunkId);
					sendChunk(chunk);
					ZonedDateTime ldt = ZonedDateTime.now();
					pw.println("Chunk Successfully Sent "+chunkId + " " + videoId + " --- " + ldt);
				}
				else {
					pw.println("Missing Chunk "+chunkId+" at Source" + " " + videoId);
				}
			}
			pw.close();
			br.close();
			serverSocket.close();
		}
		catch(Exception E) {

		}
	}

	private void sendChunk(Chunk chunk2) {
		try {
			ZonedDateTime ldt = ZonedDateTime.now();
			System.out.println("Requested Chunk "+chunk2.getId()+ " " + videoId + " --- "+ ldt);
			Socket chunkSocket = new Socket(serverIP, 8193);
			ObjectOutputStream os = new ObjectOutputStream(chunkSocket.getOutputStream());
			List<Frame> frames = chunk2.getFrames();
			for(Frame frame: frames) {
				ZonedDateTime ldt1 = ZonedDateTime.now();
				System.out.println("Resending Frame "+frame.getId()+" --- "+ldt1);
				os.writeObject(frame);
			}
			os.close();
			chunkSocket.close();
		}
		catch(Exception E) {
			//E.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {

		Runnable videoReader = new Runnable() {

			@Override
			public void run() {
				try {
					sendWelcome();
					socket = new Socket(serverIP, 8193);
					os = new ObjectOutputStream(socket.getOutputStream());
					readStream();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			private void sendWelcome() {
				try {
					Socket socket = new Socket(smartServerIP,8192);
					PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
					pw.println("Welcome");
					pw.println(senderIp);
					pw.println(serverPort);
					pw.println(videoId);
					pw.close();
					socket.close();
				}
				catch(Exception E) {
					E.printStackTrace();
				}

			}

		};
		new Thread(videoReader).start();
		
		
		Runnable keepAlive = new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Socket soc = new Socket();
						soc.connect(new InetSocketAddress(serverIP, 8990),100);
						soc.setSoTimeout(100);
						soc.close();
						check = true;
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						check = false;
					} catch (IOException e) {
						check = false;
					}
					catch(Exception E) {
						check = false;
					}
					
				}
			}
			
		};
		new Thread(keepAlive).start();
		

		ServerSocket serverSocket = new ServerSocket(serverPort);
		while(true){
			Socket socket = serverSocket.accept();
			Sender3 sec = new Sender3(socket);
			Thread thread=new Thread(sec);
			thread.start();
		}
		//readImageData();

	}

	private static void readStream() {
		File img = null;
		long startTime = Instant.now().toEpochMilli();
		while(true) {
			if((frameId-1)%fps==0&&frameId>1) {
				buffer.addChunk(chunk);
				chunk = new Chunk(++chunkId, videoId);
				long stopTime = Instant.now().toEpochMilli();
				long executionTime = stopTime - startTime;
				if(executionTime<1000)
					try {
						Thread.sleep(1000-executionTime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				startTime = Instant.now().toEpochMilli();
			}
			try {
				int len = (int)(Math.log10(frameId)+1);
				String padding = "";
				for(int i=4;i>len;i--) {
					padding = padding.concat("0");
				}
				padding = padding.concat(Integer.toString(frameId));
				String path = INDEXING_PATH+padding+".jpg";
				ZonedDateTime ldt2 = ZonedDateTime.now();
				img = new File(path);
				//byte[] imgFile = Files.readAllBytes(img.toPath());
				String frameType = "";
				if((frameId%fps)==1)
					frameType = "I";
				else
					frameType = "P";
				Frame frame = new Frame(frameId, frameType, videoId, chunkId);
				frameId++;
				chunk.addFrame(frame);
				if(check)
					os.writeObject(frame);
				System.out.println(frameId +" - "+chunkId+" - "+videoId+" --- "+ldt2);			
			} catch (IOException e) {
				e.printStackTrace();
				if(!img.exists()) {
					try {
						socket.close();
						os.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					System.exit(0);
				}
				else {
					try {
						socket = new Socket(serverIP, 8193);
						os = new ObjectOutputStream(socket.getOutputStream());
					} catch (UnknownHostException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
	}
}
