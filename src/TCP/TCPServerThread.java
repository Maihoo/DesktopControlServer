package TCP;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Base64;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;

public class TCPServerThread implements Runnable{
	
	public boolean running = true;
	public int screen = 2;
	int screenOffsetX = 0;
	int screenOffsetY = 0;
    ConnectionManager cm;
    static Socket socket;
    public Thread thread;
    public InputStream is = null;
    public BufferedReader br = null;
    public DataOutputStream dos = null;
    public OutputStream os = null;
    
    public TCPServerThread(ConnectionManager cm, Socket clientSocket) {
    	this.cm = cm;
    	this.socket = clientSocket;
    }
    
    public void captureScreen() throws Exception {
    	System.out.println("captureScreen evoked");
		BufferedImage image = new Robot().createScreenCapture(new Rectangle(screenOffsetX, screenOffsetY, 1920, 1080));
		
		if(screen == 1) {
			image = new Robot().createScreenCapture(new Rectangle(screenOffsetX, screenOffsetY, 1080, 1920));
		}//remove this statement if you have 3 horizontal monitors
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();        
		ImageIO.write(image, "jpg", baos);
		baos.flush();
		byte[] buffer = baos.toByteArray();
		
		byte[] buff = new byte[50000];
		
		DatagramSocket clientSocket = new DatagramSocket();       
		InetAddress IPAddress = InetAddress.getByName("192.168.178.53");
		
		byte[] size = ByteBuffer.allocate(4).putInt(buffer.length).array();
		clientSocket.send(new DatagramPacket(size, 16, IPAddress, 11111));
		
		int c=0;
        for(int i=0;i<buffer.length;i++){
            buff[c] = buffer[i];
            c++;
            if(i%533==0){
                DatagramPacket packet = new DatagramPacket(buff, buff.length, IPAddress, 11111);
                buff = new byte[50000];
                c=0;

                clientSocket.send(packet);

                System.out.println("sent a mini-packet");
            }
        }


		System.out.println(buffer.length);

		//DatagramPacket packet = new DatagramPacket(buffer, buffer.length, IPAddress, 11111);


		
		
		/*
		        
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", buffer);
        byte[] data = buffer.toByteArray();
        
        DatagramSocket ds = new DatagramSocket(11111);
        
        InetAddress ina = InetAddress.getByName("192.168.178.53");
        
        String data2 = "Message from server";
        byte[] buffer2 = data2.getBytes();
        
        DatagramPacket packet = new DatagramPacket(data, 50000, ina, 11111);
        
        
        ds.send(packet);
        ds.close();*/
    }
    
    public void startStream() {

    }

	public void run() {
		startStream();
        try {
        	is = socket.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            dos = new DataOutputStream(socket.getOutputStream());
			os = socket.getOutputStream();
        } catch (IOException e) {
            return;
        }
        String line;
        runloop:
        while (running) {
        	System.out.println("ready to recieve");
            try {
                line = br.readLine();
                
                System.out.println("recieved: " + line);
                
                while(line == null) {
                	System.out.println("Client crashed.");
                    running = false;
                    try {
                		cm.terminate();
                	}catch(Exception a) { System.out.println(a.toString());}
                    break runloop;
                }
                
                switch(line) {
                case "QUIT" :
                	System.out.println("Client crashed.");
                    running = false;
                    try {
                		cm.terminate();
                	}catch(Exception a) { System.out.println(a.toString());}
                    break runloop;
                case "check":
                	System.out.println("client wants to check");
          
                	dos.writeBytes("granted" + "\r\n");
                	dos.flush();
                	break;
                case "click2":
                	try {
						captureScreen();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
                	break;
                case "click":
                	try {
	                	System.out.println("client wants to send click");
	                	
	                	line = br.readLine();
	                	if(line.equalsIgnoreCase("")) line = br.readLine();
	                	
	                	String[] coords1 = line.split(":");
	                	
	                	Robot robot1 = null;
	            		try {
	            			robot1 = new Robot();
	            		} catch (AWTException e) { }
	            		
	            		if(coords1.length > 1) System.out.println("x: " + coords1[0] + " y: " + coords1[1]);
	            		
	            		int y = 0;
	            		int x = 0;
	            		
	            		x = Integer.parseInt(coords1[0]) + screenOffsetX;
	            		if(coords1.length > 1) y = Integer.parseInt(coords1[1]) + screenOffsetY;
	            	
	            		if(y == 0 && x == 0) {y = 500; x = 500; }
	            		
	            		robot1.mouseMove(x, 600);
	            		robot1.mouseMove(x, y);
	            		click(robot1);
                	}
                	 catch (Exception e) {
						System.out.println(e);
					}
            		break;
                case "doubleClick":
                	System.out.println("client wants to send double click");
           
                	line = br.readLine();
                	if(line.equalsIgnoreCase("")) line = br.readLine();
                	String[] coords2 = line.split(":");
                	
                	Robot robot = null;
            		try {
            			robot = new Robot();
            		} catch (AWTException e) { }
            		
            		System.out.println("x: " + coords2[0] + " y: " + coords2[1]);
            		
            		int x = Integer.parseInt(coords2[0]) + screenOffsetX;
            		int y = Integer.parseInt(coords2[1]) + screenOffsetY;

					if(y == 0 && x == 0) {y = 500; x = 500; }
            		
            		robot.mouseMove(x, y);
            		click(robot);
            		try{ thread.sleep(100);	} catch(Exception e) {}
            		click(robot);
            		        		
            		break;
                case "move":
                	try {
	                	System.out.println("client wants to send move");
	                	
	                	int temp = screen;
	                	
	                	line = br.readLine();
	                	if(line.equalsIgnoreCase("")) line = br.readLine();
	                	String[] coords = line.split(":");
	                	
	                	Robot robot1 = null;
	            		try {
	            			robot1 = new Robot();
	            		} catch (AWTException e) { }
	            		
	            		if(coords.length > 5) System.out.println("x: " + coords[0] + " y: " + coords[1] + " TO x: " + coords[2] + " y: " + coords[3] + " from screen " + coords[4] + " to " + coords[5]);
	            		
	            		changeToScreen(Integer.parseInt(coords[4]));
	            		
	            		int x1 = Integer.parseInt(coords[0]) + screenOffsetX;
	            		int y1 = Integer.parseInt(coords[1]) + screenOffsetY;
	            		
	            		changeToScreen(Integer.parseInt(coords[5]));
	            		
	            		int x2 = Integer.parseInt(coords[2]) + screenOffsetX;
	            		int y2 = Integer.parseInt(coords[3]) + screenOffsetY;
	            		
	            		dragMouse(robot1, x1, y1, x2, y2);
	            		
	            		changeToScreen(temp);
                	}
                	 catch (Exception e) {
						System.out.println(e);
					}
            		break;	
            		
                case "screenshot":
                	dos.writeBytes("reply from server" + "\r\n");
                	dos.flush();
                	
                	System.out.println("Screenshot Method");
                	long ts1 = System.currentTimeMillis();
                	
                	BufferedImage image = null;
                	
					try {
						image = new Robot().createScreenCapture(new Rectangle(screenOffsetX, screenOffsetY, 1920, 1080));
						
						if(screen == 1) {
							image = new Robot().createScreenCapture(new Rectangle(screenOffsetX, screenOffsetY, 1080, 1920));
						}//remove this statement if you have 3 horizontal monitors
						
						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
						
				        ImageIO.write(image, "jpg", byteArrayOutputStream);

				        byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
				        
				        System.out.println("SIZE:" + byteArrayOutputStream.size());
				        
				        dos.writeBytes(byteArrayOutputStream.size() + "\r\n");
				        dos.flush();
				        
				        final ServerSocket SSS = new ServerSocket(8888);
				        final Socket secondSocket = SSS.accept();
				        OutputStream os2 = secondSocket.getOutputStream();
				        
				        System.out.println("created Second Socket");
				        
				        os2.write(byteArrayOutputStream.toByteArray());
				        os2.flush();
				        
				        os2.close();
				        secondSocket.close();
				        SSS.close();
				        
				        System.out.println("TOTAL TIME: " + (System.currentTimeMillis() - ts1));
				                        	
					} catch (Exception e1) {
						e1.printStackTrace();
					}
                	break; 
            		
                /*case "screenshot":
                	
                	dos.writeBytes("reply from server" + "\r\n");
                	dos.flush();
                	
                	System.out.println("Screenshot Method");
                	long ts1 = System.currentTimeMillis();
                	
                	BufferedImage image = null;
                	
					try {
						image = new Robot().createScreenCapture(new Rectangle(screenOffsetX, screenOffsetY, 1920, 1080));
						
						if(screen == 1) {
							image = new Robot().createScreenCapture(new Rectangle(screenOffsetX, screenOffsetY, 1080, 1920));
						}//remove this statement if you have 3 horizontal monitors
						
						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
						
				        ImageIO.write(image, "jpg", byteArrayOutputStream);

				        byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
				        
				        System.out.println("SIZE:" + byteArrayOutputStream.size());
				        
				        os.write(size);
				        os.flush();
				        
				        os.write(byteArrayOutputStream.toByteArray());
				        os.flush();
				        
				        System.out.println("TOTAL TIME: " + (System.currentTimeMillis() - ts1));
				                        	
					} catch (Exception e1) {
						e1.printStackTrace();
					}
                	break; */
                case "enterText":
                	try {
	                	System.out.println("client wants to send text");
	                	
	                	dos.writeBytes("goAhead" + "\r\n");
	                	dos.flush();

	                	line = br.readLine();
	                	
	            		pressKey(line);
	            		
	            		line = br.readLine();
				        System.out.println("client sent: " + line);
						
				        //CodeSnipplet 1
				      
				        dos.writeBytes("successfully transferred everything :)" + "\r\n");
	                	dos.flush();
                	}
                	 catch (Exception e) {
						System.out.println(e);
					}
            		break;
                case "screenChangeOne":
                	System.out.println("Changing to screen one");
                	changeToScreen(1);
                	dos.writeBytes("success" + "\r\n");
                	dos.flush();
                	System.out.println("successfully switched to screen one");
                	break;
                case "screenChangeTwo":
                	System.out.println("Changing to screen two");
                	changeToScreen(2);
                	dos.writeBytes("success" + "\r\n");
                	dos.flush();
                	System.out.println("successfully switched to screen two");
                	break;
                case "screenChangeThree":
                	System.out.println("Changing to screen three");
                	changeToScreen(3);
                	dos.writeBytes("success" + "\r\n");
                	dos.flush();
                	System.out.println("successfully switched to screen three");
                	break;
                case "arrowleft":
                	System.out.println("arrowleft");
                	Robot robot1 = null;
            		try {
            			robot1 = new Robot();
            		} catch (AWTException e) { }
            		
            		robot1.keyPress(KeyEvent.VK_WINDOWS);
            		robot1.keyPress(KeyEvent.VK_SHIFT);
            		robot1.keyPress(KeyEvent.VK_LEFT);
            		robot1.keyRelease(KeyEvent.VK_WINDOWS);
            		robot1.keyRelease(KeyEvent.VK_SHIFT);
            		robot1.keyRelease(KeyEvent.VK_RIGHT);
            		
                	dos.writeBytes("success" + "\r\n");
                	dos.flush();
                	System.out.println("successfully arrowleft-ed");
                	break;
                case "arrowright":
                	System.out.println("arrowright");
                	Robot robot2 = null;
            		try {
            			robot2 = new Robot();
            		} catch (AWTException e) { }
            		
            		robot2.keyPress(KeyEvent.VK_WINDOWS);
            		robot2.keyPress(KeyEvent.VK_SHIFT);
            		robot2.keyPress(KeyEvent.VK_RIGHT);
            		robot2.keyRelease(KeyEvent.VK_WINDOWS);
            		robot2.keyRelease(KeyEvent.VK_SHIFT);
            		robot2.keyRelease(KeyEvent.VK_RIGHT);
            		
                	dos.writeBytes("success" + "\r\n");
                	dos.flush();
                	System.out.println("successfully arrowright-ed");
                	break;
                case "clear":
                	dos.flush();
                	break;
                default: 
                	System.out.println("ERROR LINE RECIEVED: " + line);
                    break;
                }

            } catch (IOException e) {
                System.out.println("Client crashed.");
                running = false;
                try {
            		cm.terminate();
            	}catch(Exception a) { System.out.println(a.toString());}
                break runloop;
            }
        }
    }
	
	public static void pressKey(String text) {
		Robot robot1 = null;
		try {
			robot1 = new Robot();
		} catch (AWTException e) { }
		
		for(int i = 0; i < text.length(); i++) {
			char letter = text.charAt(i);
			
			if (Character.isUpperCase(letter)) {
	            robot1.keyPress(KeyEvent.VK_SHIFT);
	        }
			
	        robot1.keyPress(Character.toUpperCase(letter));
	        robot1.keyRelease(Character.toUpperCase(letter));
	
	        if (Character.isUpperCase(letter)) {
	            robot1.keyRelease(KeyEvent.VK_SHIFT);
	        }
		}
		robot1.keyPress(KeyEvent.VK_ENTER);
	    robot1.keyRelease(KeyEvent.VK_ENTER);
	}
	
	public static BufferedImage scale(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth, double fHeight) {
		BufferedImage dbi = null;
		if(sbi != null) {
		    if(dWidth <= 0) dWidth = 1;
		    if(dHeight <= 0) dHeight = 1;
		    dbi = new BufferedImage(dWidth, dHeight, imageType);
		    Graphics2D g = dbi.createGraphics();
		    AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
		    g.drawRenderedImage(sbi, at);
		}
		return dbi;
	}
	
	public void changeToScreen(int scrn) {
		switch(scrn){
		case 1: 
			screenOffsetX = -1080;
			screenOffsetY = -310;
			break;
		case 3:
			screenOffsetX = 1920;
        	screenOffsetY = 0;
        	break;
		default:
			screenOffsetX = 0;
			screenOffsetY = 0;
			break;
		}
		screen = scrn;
	}
	
	public void dragMouse(Robot robot, int x1, int y1, int x2, int y2) {
		System.out.println("dragMouse: x: " + x1 + " y: " + y1 + " TO x: " + x2 + " y: " + y2);
		
		robot.mouseMove(x1, y1);
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.delay(2);
		robot.mouseMove(x2, y2);
		robot.delay(2);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}
	
	public static void sendScreenshot () throws Exception {
		OutputStream outputStream = socket.getOutputStream();
		
		File initialImage = new File("C://Users/Finn Ole/Desktop/test.png");
		
		BufferedImage image = ImageIO.read(initialImage);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", byteArrayOutputStream);

        byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
        outputStream.write(size);
        outputStream.write(byteArrayOutputStream.toByteArray());
        outputStream.flush();
        System.out.println("Flushed: " + System.currentTimeMillis());
        Thread.sleep(500);
        System.out.println("Closing: " + System.currentTimeMillis());
	}
	
	public static void click(Robot robot) {
		robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		robot.delay(2);
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}
}
