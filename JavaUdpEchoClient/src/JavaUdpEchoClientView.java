// MainView.java : Java Chatting Client �� �ٽɺκ�
// read keyboard --> write to network (Thread �� ó��)
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
	private JButton btnNewButton; // ����
	private Vector UserVec = new Vector(); // ����� ����ڸ� ������ ����
	private JLabel lblUserName;
	// private JTextArea textArea;
	private JTextPane textArea_list;
	
	private JFrame frame;
	//private JPanel contentPane;
	private JTextField textField; // ���� �޼��� ���°�
	private String id;
	private InetAddress ip_addr;
	private int port;
	private Canvas canvas;
	JButton sendBtn; // ���۹�ư
	JTextArea textArea; // ���ŵ� �޼����� ��Ÿ�� ����
	JTextArea textAreaNotice; // ���� �޽���
	private JList<String> list;
	private DatagramSocket udp_socket; // �������
	private DatagramPacket outgoing;
	private DatagramPacket incoming;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private static final int BUF_LEN = 4096; // Windows ó�� BUF_LEN �� ����
	MulticastSocket socket; //�޽��� �޴� ��Ƽĳ��Ʈ����
	MulticastSocket vsocket; //ȭ������ �޴� ��Ƽĳ��Ʈ����
	MulticastSocket statsocket; // �α��� �α׾ƿ� ������ ��Ƽĳ��Ʈ����
	MulticastSocket micsocket;
	String token = "|"; // |�� �޽��� �������� ������
	String statclientdata;
	final int w = Toolkit.getDefaultToolkit().getScreenSize().width,
			h = Toolkit.getDefaultToolkit().getScreenSize().height;
	byte[] data;
	StringBuffer clientdata;
	MulticastSocket statsocket_recv; // User list �޴� ��Ƽĳ��Ʈ ����
	File file = new File("src/ReadyScreen.jpg");
	
	public JavaUdpEchoClientView(String id, String ip, int port)
	{
		this.id = id;
		try {
			this.ip_addr = InetAddress.getByName(ip);
			data = ("100" + token + this.id + token + "�� �����ϼ̽��ϴ�.").getBytes();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		this.port = port;
		InitScreen();
		textArea.append("Connecting: " + ip + " " + port + "\n");
		textArea.append("ä�ù濡 ���Ű��� ȯ���մϴ�!\n");
		
		try {
			outgoing = new DatagramPacket(new byte[1],1,ip_addr,30000); 
			InetAddress address = InetAddress.getByName("230.0.0.1");
			
			socket = new MulticastSocket(1234); //��ü ����ڿ��� message ����
			socket.joinGroup(address); //joinGroup���� group�� �Ǿ� �޽����� �����Ҽ�����
			vsocket = new MulticastSocket(1235); // ��ü ����ڿ��� ���� ȭ�� ����
			vsocket.joinGroup(address);
			statsocket = new MulticastSocket(10000); // �������� login, logout ���� ����
			statsocket.joinGroup(address);
			statsocket_recv = new MulticastSocket(10001); // �������� list���� ����
			statsocket_recv.joinGroup(address);
			micsocket = new MulticastSocket(3000); //�������� sound ���� ����
			micsocket.joinGroup(address);
			udp_socket = new DatagramSocket(); // ���� ����� Datagram����
			
		} catch (IOException e) {
			textArea.append("���� ���� ����!!\n");
		}
		
		StartNetStat(); //login, logout, list �������� ����
		StartNetView(); //ȭ�� ����
		StartNetwork(); //message ����,�۽�
		StartNetMic(); //sound ���� ����
	}
	
	public void StartNetMic() {
		Thread th = new Thread(new Runnable() {
			@SuppressWarnings("resource")
			@Override
			public void run() {
			    try {
				    AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true); //������ ���� format
				    SourceDataLine speakers; //SourceDataLine�� �����Ͱ� ��ϵ� �� �ִ� ������ ����
				    
				    ByteArrayOutputStream out = new ByteArrayOutputStream();

				    int bytesRead = 0;
				    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
				    speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo); //����Ŀ�� �ͼ��� ������ ���� 
					speakers.open(format); //format�������� ����Ŀ�� �����
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
					outgoing = new DatagramPacket(data,data.length,InetAddress.getByName("230.0.0.1"),10000); //10000 port�� user name ����
					statsocket.send(outgoing);
					while(true) {
						byte[] recvData = new byte[1000];
						incoming = new DatagramPacket(recvData,recvData.length);
						statsocket_recv.receive(incoming); //user name�� ����
						statclientdata = new String(incoming.getData());
			            StringTokenizer st = new StringTokenizer(statclientdata, "["+","+"]"+" ");
			            UserVec.removeAllElements(); // UserVec�� ���� ���ְ�
			            while(st.hasMoreTokens()) {
			            	UserVec.add(st.nextToken()); //�ٽ� �߰�
							if(UserVec.elementAt(0).equals("�������")) { //Server�� ������� ��� �޽����� ���� Clientâ ��� ����
								System.exit(0);
							}
			            }
			            UserVec.remove(UserVec.lastElement()); //�� ���� ����
						list.setListData(UserVec); //������ ����Ʈ�� �ٽ� ����
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
						//udp_socket.receive(udp_packet); // packet ����
						//BufferedInputStream bin = new BufferedInputStream(is);
						//���ڵ� and resize �Ѵ�
						//ObjectInputStream ois = new ObjectInputStream(bin); //?
						//byte[] data = recv_packet.getData();
						//���ڵ� �Ϸ��������� ���ڵ��κп��� �޴� size�� Ŀ���� �������ʿ��� ���ڵ��� ���ع���.
						while(true) {
							DatagramPacket recv_packet = new DatagramPacket(receivedata,receivedata.length);
							vsocket.receive(recv_packet); // ȭ�� ����
							ByteArrayInputStream inputStreaming = new ByteArrayInputStream(recv_packet.getData()); //ByteArrayInputStream���·� ����
							panel.getGraphics().drawImage(ImageIO.read(inputStreaming), 0, 0, panel.getWidth(), panel.getHeight(), frame);
							//image�� �гο� �׷���.
						}
						//���ڵ� and resize �Ѵ�
						
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
	
	public void StartNetwork() { // ���� ���� �޼ҵ� ����κ�
		Thread th = new Thread(new Runnable() { // �����带 ������ �����κ��� �޼����� ����
			public void run() {
				byte[] bb = new byte[BUF_LEN];
				DatagramPacket udp_packet = new DatagramPacket(bb, bb.length);
				while (true) {
					for(int i=0; i<bb.length; i++)
					{
						bb[i] = 0;
					}
					try {
						//udp_socket.receive(udp_packet); // packet ����
						socket.receive(udp_packet);			//�ٸ� user �޽��� ����	
					} catch (IOException e) {
						e.printStackTrace();
					}
					String msg = new String(udp_packet.getData());
					msg = msg.trim();
					textArea.append(msg + "\n");
					textArea.setCaretPosition(textArea.getText().length());
				} // while�� ��
			}// run�޼ҵ� ��
		});
		th.start();
	}
	
	// Windows ó�� message ������ ������ �κ��� NULL �� ����� ���� �Լ�
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

	
	public void send_Message(String str) { // ������ �޼����� ������ �޼ҵ�
		byte[] bb = new byte[BUF_LEN];
		bb = str.getBytes();
		DatagramPacket udp_packet = new DatagramPacket(bb, bb.length, ip_addr, port);	
		try {
			udp_socket.send(udp_packet);
		} catch (IOException e) {
			textArea.append("�޼��� �۽� ����!!\n");
		}
	}

	public void InitScreen() { // ȭ�鱸�� �޼ҵ�
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
		textArea_User.setFont(new Font("����", Font.BOLD, 17));
		textArea_User.setBackground(new Color(255, 255, 255));
		textArea_User.setBounds(501, 5, 256, 24);
		textArea_User.setEnabled(false); // ����ڰ� �������ϰ� ���´�
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
		sendBtn = new JButton("��   ��");
		sendBtn.setForeground(new Color(255, 255, 255));
		sendBtn.setBackground(new Color(135, 206, 250));
		sendBtn.setBounds(687, 305, 70, 40);
		frame.getContentPane().add(sendBtn);
		textArea.setEnabled(false); // ����ڰ� �������ϰ� ���´�

		btnNewButton = new JButton("\uC885 \uB8CC");
		btnNewButton.setForeground(new Color(255, 255, 255));
		btnNewButton.setBackground(new Color(135, 206, 250));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { // ���� �׼��̺�Ʈ ó��
				try {
					data = ("400" + token + id + token + "�� �����ϼ̽��ϴ�.").getBytes();
					outgoing = new DatagramPacket(data, data.length,InetAddress.getByName("230.0.0.1"),10000);
					statsocket.send(outgoing); //�������� logout ó��
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				System.exit(0);
			}
		});
		btnNewButton.setBounds(501, 347, 256, 45);
		frame.getContentPane().add(btnNewButton);
		
		frame.addWindowListener(new java.awt.event.WindowAdapter() { // Ŭ���̾�Ʈ�� ��� X��ư�� ������
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if (JOptionPane.showConfirmDialog(frame, 
		            "â�� �����ðڽ��ϱ�?", "����", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
						try {
							data = ("400" + token + id + token + "�� �����ϼ̽��ϴ�.").getBytes();
							outgoing = new DatagramPacket(data, data.length,InetAddress.getByName("230.0.0.1"),10000);
							statsocket.send(outgoing); //�������� logout ó��
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
		lblUserName.setFont(new Font("����", Font.PLAIN, 14));
		lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
		lblUserName.setBounds(993, 547, 67, 40);
		frame.getContentPane().add(lblUserName);

		frame.setVisible(true);
		
		Myaction action = new Myaction();
		sendBtn.addActionListener(action); // ����Ŭ������ �׼� �����ʸ� ��ӹ��� Ŭ������
		textField.addActionListener(action);
	}

	class Myaction implements ActionListener // ����Ŭ������ �׼� �̺�Ʈ ó�� Ŭ����
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			// �׼� �̺�Ʈ�� sendBtn�϶� �Ǵ� textField ���� Enter key ġ��
			if (e.getSource() == sendBtn || e.getSource() == textField) 
			{
				String msg = null;
				msg = String.format("[%s] %s", id, textField.getText());
				send_Message(msg); // [user1] msg~
				textField.setText(""); // �޼����� ������ ���� �޼��� ����â�� ����.
				textField.requestFocus(); // �޼����� ������ Ŀ���� �ٽ� �ؽ�Ʈ �ʵ�� ��ġ��Ų��				
			}
		}
	}
}
