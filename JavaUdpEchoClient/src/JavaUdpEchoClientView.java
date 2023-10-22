// MainView.java : Java Chatting Client 의 핵심부분
// read keyboard --> write to network (Thread 로 처리)
// read network --> write to textArea

import java.awt.Canvas;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.ScrollPaneConstants;

public class JavaUdpEchoClientView extends JFrame {
	JPanel panel;
	private String UserName;
	private JButton btnNewButton; // 종료
	private Vector UserVec = new Vector(); // 연결된 사용자를 저장할 벡터
	private JLabel lblUserName;
	// private JTextArea textArea;
	private JTextPane textArea_list;
	
	private JFrame frame;
	//private JPanel contentPane;
	private JTextField textField; // 보낼 메세지 쓰는곳
	private String id;
	private InetAddress ip_addr;
	private int port;
	private Canvas canvas;
	JButton sendBtn; // 전송버튼
	JTextArea textArea; // 수신된 메세지를 나타낼 변수
	JTextArea textAreaNotice; // 공지 메시지
	private JList<String> list;
	private DatagramSocket udp_socket; // 연결소켓
	private DatagramPacket outgoing;
	private DatagramPacket incoming;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private static final int BUF_LEN = 4096; // Windows 처럼 BUF_LEN 을 정의
	MulticastSocket socket; //메시지 받는 멀티캐스트소켓
	MulticastSocket vsocket; //화면정보 받는 멀티캐스트소켓
	MulticastSocket statsocket; // 로그인 로그아웃 보내는 멀티캐스트소켓
	MulticastSocket micsocket;
	String token = "|"; // |로 메시지 프로토콜 구분함
	String statclientdata;
	final int w = Toolkit.getDefaultToolkit().getScreenSize().width,
			h = Toolkit.getDefaultToolkit().getScreenSize().height;
	byte[] data;
	StringBuffer clientdata;
	MulticastSocket statsocket_recv; // User list 받는 멀티캐스트 소켓
	File file = new File("src/ReadyScreen.jpg");
	
	public JavaUdpEchoClientView(String id, String ip, int port)
	{
		this.id = id;
		try {
			this.ip_addr = InetAddress.getByName(ip);
			data = ("100" + token + this.id + token + "가 입장하셨습니다.").getBytes();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		this.port = port;
		InitScreen();
		textArea.append("Connecting: " + ip + " " + port + "\n");
		textArea.append("채팅방에 오신것을 환영합니다!\n");
		
		try {
			outgoing = new DatagramPacket(new byte[1],1,ip_addr,30000); 
			InetAddress address = InetAddress.getByName("230.0.0.1");
			
			socket = new MulticastSocket(1234); //전체 사용자에게 message 수신
			socket.joinGroup(address); //joinGroup으로 group이 되어 메시지를 수신할수있음
			vsocket = new MulticastSocket(1235); // 전체 사용자에게 서버 화면 수신
			vsocket.joinGroup(address);
			statsocket = new MulticastSocket(10000); // 서버에게 login, logout 정보 보냄
			statsocket.joinGroup(address);
			statsocket_recv = new MulticastSocket(10001); // 서버에게 list내역 받음
			statsocket_recv.joinGroup(address);
			micsocket = new MulticastSocket(3000); //서버에게 sound 정보 수신
			micsocket.joinGroup(address);
			udp_socket = new DatagramSocket(); // 서버 연결된 Datagram소켓
			
		} catch (IOException e) {
			textArea.append("소켓 생성 에러!!\n");
		}
		
		StartNetStat(); //login, logout, list 프로토콜 보냄
		StartNetView(); //화면 송출
		StartNetwork(); //message 수신,송신
		StartNetMic(); //sound 정보 수신
	}
	
	public void StartNetMic() {
		Thread th = new Thread(new Runnable() {
			@SuppressWarnings("resource")
			@Override
			public void run() {
			    try {
				    AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true); //서버와 같은 format
				    SourceDataLine speakers; //SourceDataLine은 데이터가 기록될 수 있는 데이터 라인
				    
				    ByteArrayOutputStream out = new ByteArrayOutputStream();

				    int bytesRead = 0;
				    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
				    speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo); //스피커의 믹서의 정보를 저장 
					speakers.open(format); //format형식으로 스피커를 열어둠
				    speakers.start();
			        //InetAddress address = InetAddress.getByName(hostname);
			        //DatagramSocket socket = new DatagramSocket();

			        //DatagramSocket serverSocket = new DatagramSocket(5555);
			        byte[] receiveData = new byte[1024];
			        byte[] sendData = new byte[1024];

			        while (true) {

			            byte[] buffer = new byte[1024];
			            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
			            micsocket.receive(response);

			            out.write(response.getData(), 0, response.getData().length);
			            speakers.write(response.getData(), 0, response.getData().length);
			            String quote = new String(buffer, 0, response.getLength());

			            System.out.println(quote.getBytes().toString()); 
			        }

			    } catch (SocketTimeoutException ex) {
			        System.out.println("Timeout error: " + ex.getMessage());
			        ex.printStackTrace();
			    } catch (IOException ex) {
			        System.out.println("Client error: " + ex.getMessage());
			        ex.printStackTrace();
			    } catch (LineUnavailableException e) {
					e.printStackTrace();
				}
			}
			
		});
		th.start();
	}
	public void StartNetStat() {
		Thread th = new Thread(new Runnable() {
			@SuppressWarnings("resource")
			@Override
			public void run() {
				try {
					outgoing = new DatagramPacket(data,data.length,InetAddress.getByName("230.0.0.1"),10000); //10000 port에 user name 보냄
					statsocket.send(outgoing);
					while(true) {
						byte[] recvData = new byte[1000];
						incoming = new DatagramPacket(recvData,recvData.length);
						statsocket_recv.receive(incoming); //user name을 받음
						statclientdata = new String(incoming.getData());
			            StringTokenizer st = new StringTokenizer(statclientdata, "["+","+"]"+" ");
			            UserVec.removeAllElements(); // UserVec을 전부 없애고
			            while(st.hasMoreTokens()) {
			            	UserVec.add(st.nextToken()); //다시 추가
							if(UserVec.elementAt(0).equals("방송종료")) { //Server가 방송종료 라는 메시지가 오면 Client창 모두 종료
								System.exit(0);
							}
			            }
			            UserVec.remove(UserVec.lastElement()); //빈 공백 제거
						list.setListData(UserVec); //유저들 리스트에 다시 생성
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		th.start();
	}
	

	public void StartNetView() {
		Thread th = new Thread(new Runnable() {
			
			@SuppressWarnings("resource")
			@Override
			public void run() {
				// TODO Auto-generated method stub
				byte[] receivedata = new byte[65507];
				while (true) {
					
					for(int i=0; i<receivedata.length; i++)
					{
						receivedata[i] = 0;
					}					
					try {
						//DatagramSocket clientsocket = new DatagramSocket(1234);
						//udp_socket.receive(udp_packet); // packet 수신
						//BufferedInputStream bin = new BufferedInputStream(is);
						//디코딩 and resize 둘다
						//ObjectInputStream ois = new ObjectInputStream(bin); //?
						//byte[] data = recv_packet.getData();
						//디코딩 하려고했지만 인코딩부분에서 받는 size가 커져서 보내는쪽에서 인코딩을 안해버림.
						while(true) {
							DatagramPacket recv_packet = new DatagramPacket(receivedata,receivedata.length);
							vsocket.receive(recv_packet); // 화면 수신
							ByteArrayInputStream inputStreaming = new ByteArrayInputStream(recv_packet.getData()); //ByteArrayInputStream형태로 저장
							panel.getGraphics().drawImage(ImageIO.read(inputStreaming), 0, 0, panel.getWidth(), panel.getHeight(), frame);
							//image를 패널에 그려줌.
						}
						//디코딩 and resize 둘다
						
						/*
						//BufferedImage
						while (true) {
							panel.getGraphics().drawImage(ImageIO.read(ImageIO.createImageInputStream(bin)), 0, 0, panel.getWidth(), panel.getHeight(), frame);
						}//BufferedImage
							*/
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		th.start();
	}
	
	public void StartNetwork() { // 실직 적인 메소드 연결부분
		Thread th = new Thread(new Runnable() { // 스레드를 돌려서 서버로부터 메세지를 수신
			public void run() {
				byte[] bb = new byte[BUF_LEN];
				DatagramPacket udp_packet = new DatagramPacket(bb, bb.length);
				while (true) {
					for(int i=0; i<bb.length; i++)
					{
						bb[i] = 0;
					}
					try {
						//udp_socket.receive(udp_packet); // packet 수신
						socket.receive(udp_packet);			//다른 user 메시지 수신	
					} catch (IOException e) {
						e.printStackTrace();
					}
					String msg = new String(udp_packet.getData());
					msg = msg.trim();
					textArea.append(msg + "\n");
					textArea.setCaretPosition(textArea.getText().length());
				} // while문 끝
			}// run메소드 끝
		});
		th.start();
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

	
	public void send_Message(String str) { // 서버로 메세지를 보내는 메소드
		byte[] bb = new byte[BUF_LEN];
		bb = str.getBytes();
		DatagramPacket udp_packet = new DatagramPacket(bb, bb.length, ip_addr, port);	
		try {
			udp_socket.send(udp_packet);
		} catch (IOException e) {
			textArea.append("메세지 송신 에러!!\n");
		}
	}

	public void InitScreen() { // 화면구성 메소드
		frame = new JFrame("Client");
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setBounds(770, 220, 777, 433);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		frame.getContentPane().add(panel);
		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		panel.setBounds(5, 5, 493, 387);

		JScrollPane scrollPane_list = new JScrollPane();
		scrollPane_list.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane_list.setBounds(501, 31, 256, 90);
		frame.getContentPane().add(scrollPane_list);

		list = new JList<String>(UserVec);
		list.setBounds(298, 0, 188, 363);
		scrollPane_list.setViewportView(list);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(501, 123, 256, 180);
		frame.getContentPane().add(scrollPane);

		JTextArea textArea_User = new JTextArea();
		textArea_User.setText("\uC0AC\uC6A9\uC790");
		textArea_User.setFont(new Font("굴림", Font.BOLD, 17));
		textArea_User.setBackground(new Color(255, 255, 255));
		textArea_User.setBounds(501, 5, 256, 24);
		textArea_User.setEnabled(false); // 사용자가 수정못하게 막는다
		textArea_User.setDisabledTextColor(new Color(0,0,0));
		frame.getContentPane().add(textArea_User);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		//textArea.setForeground(new Color(255,0,0));
		textArea.setDisabledTextColor(new Color(0,0,0));
		textField = new JTextField();
		textField.setBounds(501, 305, 183, 40);

		frame.getContentPane().add(textField);
		textField.setColumns(10);
		sendBtn = new JButton("전   송");
		sendBtn.setForeground(new Color(255, 255, 255));
		sendBtn.setBackground(new Color(135, 206, 250));
		sendBtn.setBounds(687, 305, 70, 40);
		frame.getContentPane().add(sendBtn);
		textArea.setEnabled(false); // 사용자가 수정못하게 막는다

		btnNewButton = new JButton("\uC885 \uB8CC");
		btnNewButton.setForeground(new Color(255, 255, 255));
		btnNewButton.setBackground(new Color(135, 206, 250));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { // 종료 액션이벤트 처리
				try {
					data = ("400" + token + id + token + "가 퇴장하셨습니다.").getBytes();
					outgoing = new DatagramPacket(data, data.length,InetAddress.getByName("230.0.0.1"),10000);
					statsocket.send(outgoing); //서버에게 logout 처리
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		btnNewButton.setBounds(501, 347, 256, 45);
		frame.getContentPane().add(btnNewButton);
		
		frame.addWindowListener(new java.awt.event.WindowAdapter() { // 클라이언트가 상단 X버튼을 누를때
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if (JOptionPane.showConfirmDialog(frame, 
		            "창을 닫으시겠습니까?", "종료", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
						try {
							data = ("400" + token + id + token + "가 퇴장하셨습니다.").getBytes();
							outgoing = new DatagramPacket(data, data.length,InetAddress.getByName("230.0.0.1"),10000);
							statsocket.send(outgoing); //서버에게 logout 처리
				            System.exit(0);
						} catch (IOException e) {
							e.printStackTrace();
						}
		        }
		        else
	                  remove(JOptionPane.QUESTION_MESSAGE);
		    }
		});
		
		lblUserName = new JLabel("Name");
		lblUserName.setFont(new Font("굴림", Font.PLAIN, 14));
		lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
		lblUserName.setBounds(993, 547, 67, 40);
		frame.getContentPane().add(lblUserName);

		frame.setVisible(true);
		
		Myaction action = new Myaction();
		sendBtn.addActionListener(action); // 내부클래스로 액션 리스너를 상속받은 클래스로
		textField.addActionListener(action);
	}

	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			// 액션 이벤트가 sendBtn일때 또는 textField 에세 Enter key 치면
			if (e.getSource() == sendBtn || e.getSource() == textField) 
			{
				String msg = null;
				msg = String.format("[%s] %s", id, textField.getText());
				send_Message(msg); // [user1] msg~
				textField.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				textField.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다				
			}
		}
	}
}
