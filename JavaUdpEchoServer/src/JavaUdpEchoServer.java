// Java Chatting Server

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.border.CompoundBorder;

public class JavaUdpEchoServer extends JFrame {
	MulticastSocket mssocket;
	private JPanel contentPane;
	private JTextField textField; // 사용할 PORT번호 입력
	private JButton Start; // 서버를 실행시킨 버튼
	JTextArea textArea; // 클라이언트 및 서버 메시지 출력
	JTextArea ListtextArea; // "사용자"
	private int Port; // 포트번호
	private Vector UserVec = new Vector(); // 연결된 사용자를 저장할 벡터
	private static final int BUF_LEN = 4096; // Windows 처럼 BUF_LEN 을 정의
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	public BufferedOutputStream bout;
	JButton sendBtn; // 전송버튼
	JButton VideoOnOff; // 화면전송 on/off 버튼
	JButton MikeOnOff; // 마이크 on/off 버튼
	private String clientdata;
	private byte[] outgoingdata;
	private DatagramPacket incoming;
	private DatagramPacket outgoing;
	private DatagramSocket udp_socket; // 연결소켓
	private DatagramPacket udp_packet; // 연결소켓
	private String id = "Host"; 
	private Socket client_socket;
	private JTextField textField_1;
	private JList<String> list;
	private Image fileimage;
	private byte[] imagebuffer;

	MulticastSocket msvsocket;
	MulticastSocket msstatsocket;
	MulticastSocket msmsocket;

	TargetDataLine microphone;
	SourceDataLine speakers;
	AudioFormat format;

	File file = new File("src/ReadyScreen.PNG");
	ImageIcon ScreenOff = new ImageIcon("src/ScreenOff.png");
	ImageIcon ScreenOn = new ImageIcon("src/ScreenON.png");
	ImageIcon MikeOff = new ImageIcon("src/MikeOff.png");
	ImageIcon MikeOn = new ImageIcon("src/MikeOn.png");
	ImageIcon SoundOff = new ImageIcon("src/SoundOff.png");
	ImageIcon SoundOn = new ImageIcon("src/SoundOn.png");

	public static void main(String[] args) {
		JavaUdpEchoServer frame = new JavaUdpEchoServer();
		frame.setVisible(true);
	}

	public JavaUdpEchoServer() {
		setBackground(Color.PINK);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 505, 445);
		contentPane = new JPanel();
		contentPane.setBackground(Color.BLACK);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 294, 254);
		scrollPane.setBorder(new CompoundBorder(new LineBorder(new Color(128, 128, 128)), null));
		contentPane.add(scrollPane);

		ListtextArea = new JTextArea();
		ListtextArea.setFont(new Font("Monospaced", Font.BOLD, 13));
		ListtextArea.setForeground(new Color(255, 255, 255));
		ListtextArea.setBackground(new Color(0, 0, 0));
		ListtextArea.setText("\uC0AC\uC6A9\uC790");
		ListtextArea.setEditable(false);
		ListtextArea.setBounds(292, 1, 197, 21);
		contentPane.add(ListtextArea);
		textArea = new JTextArea();
		textArea.setBackground(new Color(0, 0, 0));
		textArea.setForeground(new Color(255, 255, 255));
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		textField = new JTextField();
		textField.setForeground(new Color(255, 255, 255));
		textField.setBackground(new Color(0, 0, 0));
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setText("30000");
		textField.setBounds(98, 293, 196, 30);
		contentPane.add(textField);
		textField.setColumns(10);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setForeground(Color.WHITE);
		lblNewLabel.setBackground(Color.WHITE);
		lblNewLabel.setBounds(12, 293, 98, 30);
		contentPane.add(lblNewLabel);
		Start = new JButton("서버 실행");
		Start.setForeground(new Color(255, 255, 255));
		Start.setBackground(new Color(0, 0, 0));

		textField_1 = new JTextField();
		textField_1.setBackground(Color.WHITE);
		textField_1.setForeground(Color.BLACK);
		textField_1.setBounds(0, 255, 214, 37);
		contentPane.add(textField_1);
		textField_1.setColumns(10);

		sendBtn = new JButton("\uC804 \uC1A1");
		sendBtn.setForeground(new Color(255, 255, 255));
		sendBtn.setBackground(new Color(0, 0, 0));
		sendBtn.setBounds(215, 254, 79, 39);
		contentPane.add(sendBtn);

		VideoOnOff = new JButton(ScreenOff);
		VideoOnOff.setForeground(new Color(0, 0, 0));
		VideoOnOff.setBackground(new Color(0, 0, 0));
		VideoOnOff.setBounds(0, 365, 148, 40);
		contentPane.add(VideoOnOff);

		MikeOnOff = new JButton(MikeOff);
		MikeOnOff.setBackground(new Color(0, 0, 0));
		MikeOnOff.setBounds(146, 365, 148, 40);
		contentPane.add(MikeOnOff);

		Myaction action = new Myaction();
		Start.addActionListener(action); // 내부클래스로 액션 리스너를 상속받은 클래스로
		textField.addActionListener(action);
		sendBtn.addActionListener(action); // 내부클래스로 액션 리스너를 상속받은 클래스로
		textField_1.addActionListener(action);
		VideoOnOff.addActionListener(action);
		MikeOnOff.addActionListener(action);
		Start.setBounds(0, 325, 294, 38);
		contentPane.add(Start);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if (JOptionPane.showConfirmDialog(contentPane, 
		            "방송을 종료하시겠습니까?", "종료", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		        	//서버가 창닫으면 모든 클라이언트 닫기
					try {
						outgoingdata = new String("[방송종료]").getBytes();
						outgoing = new DatagramPacket(outgoingdata, outgoingdata.length, InetAddress.getByName("230.0.0.1"), 10001);
						msstatsocket.send(outgoing);
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.exit(0);
		        }
		        else
	                  remove(JOptionPane.QUESTION_MESSAGE);
		    }
		});
		

		JScrollPane scrollPane_list = new JScrollPane();
		scrollPane_list.setBounds(292, 21, 200, 387);
		list = new JList<String>(UserVec);
		list.setBorder(new CompoundBorder(new LineBorder(new Color(128, 128, 128)), null));
		list.setBackground(new Color(0, 0, 0));
		list.setForeground(new Color(255, 255, 255));
		UserVec.add(0, "Host");
		list.setBounds(298, 0, 188, 363);
		scrollPane_list.setViewportView(list);
		contentPane.add(scrollPane_list);

		VideoOnOff.setEnabled(false);
		MikeOnOff.setEnabled(false);
	}
	
	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == VideoOnOff) { // On 버튼을 누르면 화면 송출
				if (VideoOnOff.getIcon().equals(ScreenOff)) {
					VideoOnOff.setIcon(ScreenOn);
					ViewStart(); // 서버의 화면을 송출하는 스레드
				} else {
					VideoOnOff.setIcon(ScreenOff);
				}
			}

			if (e.getSource() == MikeOnOff) {
				if (MikeOnOff.getIcon().equals(MikeOff)) {
					MikeOnOff.setIcon(MikeOn);
					MicStart(); // 마이크소리를 Client들에게 보내는 스레드
				} else {
					MikeOnOff.setIcon(MikeOff);
				}
			}
			// 액션 이벤트가 sendBtn일때 또는 textField 에서 Enter key 치면
			if (e.getSource() == Start || e.getSource() == textField) {
				VideoOnOff.setEnabled(true);
				MikeOnOff.setEnabled(true);
				if (textField.getText().equals("") || textField.getText().length() == 0)// textField에 값이 들어있지 않을때
				{
					textField.setText("포트번호를 입력해주세요");
					textField.requestFocus(); // 포커스를 다시 textField에 넣어준다

				} else {
					try {
						Port = Integer.parseInt(textField.getText()); // 숫자로 입력하지 않으면 에러 발생 포트를 열수 없다.
						mssocket = new MulticastSocket();
						msvsocket = new MulticastSocket();
						msmsocket = new MulticastSocket();

						StatStart(); //login, logout 정보받는 스레드
						ServerStart(); // 사용자가 제대로된 포트번호를 넣었을때 서버 실행을위헤 메소드 호출
						ViewStart(); // 방송준비중 화면을 송출하는 스레드
					} catch (Exception er) {
						// 사용자가 숫자로 입력하지 않았을시에는 재입력을 요구한다
						textField.setText("숫자로 입력해주세요");
						textField.requestFocus(); // 포커스를 다시 textField에 넣어준다
					}
				} // else 문 끝
			}
			if (e.getSource() == sendBtn || e.getSource() == textField_1) {
				String msg = null;
				msg = String.format("[%s] %s", id, textField_1.getText());
				send_Message(udp_socket, udp_packet, msg);
				textField_1.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				textField_1.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
				AppendText(msg);
			}
		}
	}

	// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
	public byte[] MakePacket(String msg) {
		byte[] packet = new byte[BUF_LEN];
		byte[] bb = null;
		int i;
		for (i = 0; i < BUF_LEN; i++)
			packet[i] = 0;
		try {
			bb = msg.getBytes("euc-kr");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		for (i = 0; i < bb.length; i++)
			packet[i] = bb[i];
		return packet;
	}

	public void AppendText(String str) { //시간 + 메시지 (서버 채팅창에만 출력)
		// textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
		LocalTime now = LocalTime.now();
		int hour = now.getHour();
		int minute = now.getMinute();
		textArea.append("[" + hour + " : " + minute + "]" + str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void send_Message(DatagramSocket udp_socket, DatagramPacket udp_packet, String msgStr) { // 클라이언트로 메세지를 보내는 메소드
		byte[] buffer = new byte[BUF_LEN];
		String sendMsg = msgStr;
		buffer = sendMsg.getBytes();
		int port = 1234;
		try {
			// [참고] https://happyourlife.tistory.com/102 멀티캐스팅
			InetAddress NetAddress = InetAddress.getByName("230.0.0.1");
			DatagramPacket server_udp_packet = new DatagramPacket(buffer, buffer.length, NetAddress, port);
			// echo back
			// udp_socket.send(server_udp_packet);
			mssocket.send(server_udp_packet); 

			Thread.sleep(100); //과부화가 일어날수 있기에 0.1초 sleep
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static byte[] encodingBase64(byte[] targetBytes) { // Base64 인코딩
		Encoder encoder = Base64.getEncoder(); 
		return encoder.encode(targetBytes);
	}

	public static BufferedImage resize(BufferedImage inputStream, int width, int height) throws IOException { //이미지파일 해상도 줄임
		BufferedImage outputImage = new BufferedImage(width, height, inputStream.getType());
		Graphics2D graphics2D = outputImage.createGraphics();
		graphics2D.drawImage(inputStream, 0, 0, width, height, null);
		graphics2D.dispose();
		return outputImage;
	}

	private BufferedImage imageToBufferedImage(Image im) { //image를 BufferedImage로 바꿔줌
		BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics bg = bi.getGraphics();
		bg.drawImage(im, 0, 0, null);
		bg.dispose();
		return bi;
	}
	
	private void MicStart() {
		Thread th = new Thread(new Runnable() {
			@SuppressWarnings("resource")
			@Override
			public void run() {
				//[참고] https://runebook.dev/ko/docs/openjdk/java.desktop/javax/sound/sampled/audioformat
				format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
				try {

					microphone = AudioSystem.getTargetDataLine(format);

					DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); //믹서의 정보를 가져옴
					microphone = (TargetDataLine) AudioSystem.getLine(info); //믹서의 정보를 받음.
					microphone.open(format);

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					int numBytesRead;
					int CHUNK_SIZE = 1024;
					byte[] data = new byte[microphone.getBufferSize() / 5];
					microphone.start();

					//DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
					//speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
					//speakers.open(format);
					//speakers.start();

					// Configure the ip and port
					// String hostname = "localhost";
					int port = 3000;

					// InetAddress address = InetAddress.getByName(hostname);
					// DatagramSocket socket = new DatagramSocket();

					while (true) {
						//byte[] buffer = new byte[1024];
						numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
						// bytesRead += numBytesRead;
						// write the mic data to a stream for use later
						out.write(data, 0, numBytesRead);
						// speakers.write(data, 0, numBytesRead); // 서버의 스피커를 막음
						DatagramPacket request = new DatagramPacket(data, numBytesRead,
								InetAddress.getByName("230.0.0.1"), port);
						msmsocket.send(request);
						if (MikeOnOff.getIcon().equals(MikeOff)) {
							if (microphone.isOpen() || speakers.isOpen()) {
								microphone.close();
								speakers.close();
								out.close();
							}
							while (true) {
								if (MikeOnOff.getIcon().equals(MikeOn)) {
									break;
								}
							}
							break;
						}
					}

				} catch (LineUnavailableException | IOException e) {
					e.printStackTrace();
				}
			}
		});
		th.start();
	}

	// 출처: https://jinseongsoft.tistory.com/260 [진성 소프트]
	private void ViewStart() {
		Thread th = new Thread(new Runnable() {
			int port = 1234;

			@SuppressWarnings("resource")
			@Override
			public void run() {
				try {
					// DatagramSocket clientSocket = new DatagramSocket(30001);
					InetAddress NetAddress = InetAddress.getByName("230.0.0.1");
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Dimension screenSize = toolkit.getScreenSize(); 
					BufferedImage image; // 스크린샷이 저장될 버퍼공간
					Robot r = new Robot(); // 스크린샷을 찍는 로봇클래스
					// BufferedOutputStream bout = new
					// BufferedOutputStream(socket.getOutputStream());
					// byte[] buffer;
					// ObjectOutputStream ois = new ObjectOutputStream(new BufferedOutputStream(os));

					while (true) {
						//스크린샷을 찍으면서 바로 압축
						image = resize(
								r.createScreenCapture(new Rectangle(0, 0, screenSize.width / 2, screenSize.height)),
								640, 480); // 화면 스크린샷 16:9 1008,567
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						// 압축
						// image = resize(image , screenSize.width, screenSize.height);
						ImageIO.write(image, "jpeg", baos);
						baos.flush();
						// buffer = baos.toByteArray();

						DatagramPacket packet = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length,
								NetAddress, 1235); //패킷에 정보를 담음
						System.out.println(baos.toByteArray().length);
						if (baos.toByteArray().length > 65507) { // buffer의 크기가 너무 크면 error가 남.. 65507 최대 사이즈 송신
							baos.reset(); // 넘을시 초기화
							continue;
						} // https://forum.snmp.app/t/length-of-original-trap-in-incoming-buffer-and-transportlistener-interface/141/3

						msvsocket.send(packet); // 화면정보를 송신

						// ois.writeObject(encodingBase64(baos.toByteArray())); //인코딩하려고했지만 사이즈가 대략 20000byte가량 커져서 쓰지 않음.
						// ois.flush();
						if (VideoOnOff.getIcon().equals(ScreenOff)) { //화면송신 Off (화면준비중 화면 송신)
							baos.close();
							while (true) {
								ByteArrayOutputStream baosed = null;
								try {
									fileimage = ImageIO.read(file); //화면준비중 사진
									image = resize(imageToBufferedImage(fileimage), 1280, 720); //압축
									baosed = new ByteArrayOutputStream();
									ImageIO.write(image, "jpeg", baosed);
									baosed.flush();
									packet = new DatagramPacket(baosed.toByteArray(), baosed.toByteArray().length,
											InetAddress.getByName("230.0.0.1"), 1235);
									System.out.println(baosed.toByteArray().length);
									msvsocket.send(packet);//다시 뿌려줌
									Thread.sleep(500); //화면송신처럼 준비중사진을 뿌리면 들어오는 user는 효과가 빠르겠지만 효율이 떨어짐. sleep을 0.5초 해준다.
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								if (VideoOnOff.getIcon().equals(ScreenOn)) {
									baosed.close();
									break;
								}
							}
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		th.start();
	}

	private void List() { // Server List에 표시
		list.setListData(UserVec);
		// AppendText(UserVec.toString()); //[host, user1] 출력
		// UserVec정보를 Client에게 보내기
		try {
			outgoingdata = new String(UserVec.toString()).getBytes();
			outgoing = new DatagramPacket(outgoingdata, outgoingdata.length, InetAddress.getByName("230.0.0.1"), 10001);
			msstatsocket.send(outgoing); //login 또는 logout할때 들어왔던 정보 수신, 설정후에 다시 send
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void StatStart() { // 입장,퇴장.. 메시지를 받는 Thread
		Thread th = new Thread(new Runnable() {
			@SuppressWarnings("resource")
			@Override
			public void run() {
				try {
					msstatsocket = new MulticastSocket(10000);
					msstatsocket.joinGroup(InetAddress.getByName("230.0.0.1"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				while (true) {
					// 입장 메시지를 받는 부분
					try {
						byte[] recvData = new byte[1000];
						// incoming.setLength(incoming.getData().length);
						incoming = new DatagramPacket(recvData, recvData.length);
						msstatsocket.receive(incoming); //login 또는 logout시에 받는 유저name을 받음
						clientdata = new String(incoming.getData());
						// System.out.println(clientdata);
						// AppendText(clientdata);
						StringTokenizer st = new StringTokenizer(clientdata, "|"); // 클라이언트에서 보내온 token이 |이었던 것을 잘라줌
						int command = Integer.parseInt(st.nextToken());
						String name = st.nextToken();
						String text = st.nextToken();

						switch (command) {
						case 100: // login 처리
							// AppendText(name+text); //user가 입장하셨습니다
							UserVec.add(name);
							// UserVec에 저장
							List();
							break;
						case 200: // list 처리
							List();
							break;
						case 400: // logout 처리
							// AppendText(name+text); //user가 퇴장하셨습니다.
							for (int i = 0; i < UserVec.size(); i++)
								if (name.equals(UserVec.elementAt(i))) {
									UserVec.remove(i);
									break;
								}
							List();
							// 여기서 outgoing 처리
							break;
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					} // 입장 메시지를 받는 부분 끝
				}
			}
		});
		th.start();
	}

	// @SuppressWarnings("resource")
	private void ServerStart() {
		Thread th = new Thread(new Runnable() { // 사용자 접속을 받을 스레드
			@SuppressWarnings("resource")
			@Override
			public void run() {
				// socket = new ServerSocket(Port); // 서버가 포트 여는부분
				Start.setText("서버실행중");
				Start.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
				textField.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
				udp_socket = null;
				// incoming = new DatagramPacket(new byte[60000],60000);

				try {
					udp_socket = new DatagramSocket(Port); //30000번 포트 datagramsocket
					mssocket = new MulticastSocket(); // client에게 받은 메시지를 전체에게 뿌려줘야하는 소켓
				} catch (IOException e) {
					e.printStackTrace();
				}

				byte[] bb = new byte[BUF_LEN];
				udp_packet = new DatagramPacket(bb, bb.length);
				while (true) {

					for (int i = 0; i < bb.length; i++) {
						bb[i] = 0;
					}

					try {
						udp_socket.receive(udp_packet); //클라이언트에게 메시지를 받음
					} catch (IOException e) {
						e.printStackTrace();
					}
					String msgStr = new String(bb);
					// textArea.append(udp_packet.getPort()+" : ");
					AppendText(msgStr); //서버에 클라이언트가 보낸 메시지를 띄움

					send_Message(udp_socket, udp_packet, msgStr); //멀티캐스팅한 소켓에 클라이언트메시지를 뿌려주는 메소드 (전체메시지로 감)
				}
			}
		});
		th.start();
	}
}
