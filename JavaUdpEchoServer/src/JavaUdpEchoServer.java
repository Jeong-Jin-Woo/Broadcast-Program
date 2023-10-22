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
	private JTextField textField; // ����� PORT��ȣ �Է�
	private JButton Start; // ������ �����Ų ��ư
	JTextArea textArea; // Ŭ���̾�Ʈ �� ���� �޽��� ���
	JTextArea ListtextArea; // "�����"
	private int Port; // ��Ʈ��ȣ
	private Vector UserVec = new Vector(); // ����� ����ڸ� ������ ����
	private static final int BUF_LEN = 4096; // Windows ó�� BUF_LEN �� ����
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	public BufferedOutputStream bout;
	JButton sendBtn; // ���۹�ư
	JButton VideoOnOff; // ȭ������ on/off ��ư
	JButton MikeOnOff; // ����ũ on/off ��ư
	private String clientdata;
	private byte[] outgoingdata;
	private DatagramPacket incoming;
	private DatagramPacket outgoing;
	private DatagramSocket udp_socket; // �������
	private DatagramPacket udp_packet; // �������
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
		Start = new JButton("���� ����");
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
		Start.addActionListener(action); // ����Ŭ������ �׼� �����ʸ� ��ӹ��� Ŭ������
		textField.addActionListener(action);
		sendBtn.addActionListener(action); // ����Ŭ������ �׼� �����ʸ� ��ӹ��� Ŭ������
		textField_1.addActionListener(action);
		VideoOnOff.addActionListener(action);
		MikeOnOff.addActionListener(action);
		Start.setBounds(0, 325, 294, 38);
		contentPane.add(Start);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        if (JOptionPane.showConfirmDialog(contentPane, 
		            "����� �����Ͻðڽ��ϱ�?", "����", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		        	//������ â������ ��� Ŭ���̾�Ʈ �ݱ�
					try {
						outgoingdata = new String("[�������]").getBytes();
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
	
	class Myaction implements ActionListener // ����Ŭ������ �׼� �̺�Ʈ ó�� Ŭ����
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == VideoOnOff) { // On ��ư�� ������ ȭ�� ����
				if (VideoOnOff.getIcon().equals(ScreenOff)) {
					VideoOnOff.setIcon(ScreenOn);
					ViewStart(); // ������ ȭ���� �����ϴ� ������
				} else {
					VideoOnOff.setIcon(ScreenOff);
				}
			}

			if (e.getSource() == MikeOnOff) {
				if (MikeOnOff.getIcon().equals(MikeOff)) {
					MikeOnOff.setIcon(MikeOn);
					MicStart(); // ����ũ�Ҹ��� Client�鿡�� ������ ������
				} else {
					MikeOnOff.setIcon(MikeOff);
				}
			}
			// �׼� �̺�Ʈ�� sendBtn�϶� �Ǵ� textField ���� Enter key ġ��
			if (e.getSource() == Start || e.getSource() == textField) {
				VideoOnOff.setEnabled(true);
				MikeOnOff.setEnabled(true);
				if (textField.getText().equals("") || textField.getText().length() == 0)// textField�� ���� ������� ������
				{
					textField.setText("��Ʈ��ȣ�� �Է����ּ���");
					textField.requestFocus(); // ��Ŀ���� �ٽ� textField�� �־��ش�

				} else {
					try {
						Port = Integer.parseInt(textField.getText()); // ���ڷ� �Է����� ������ ���� �߻� ��Ʈ�� ���� ����.
						mssocket = new MulticastSocket();
						msvsocket = new MulticastSocket();
						msmsocket = new MulticastSocket();

						StatStart(); //login, logout �����޴� ������
						ServerStart(); // ����ڰ� ����ε� ��Ʈ��ȣ�� �־����� ���� ���������� �޼ҵ� ȣ��
						ViewStart(); // ����غ��� ȭ���� �����ϴ� ������
					} catch (Exception er) {
						// ����ڰ� ���ڷ� �Է����� �ʾ����ÿ��� ���Է��� �䱸�Ѵ�
						textField.setText("���ڷ� �Է����ּ���");
						textField.requestFocus(); // ��Ŀ���� �ٽ� textField�� �־��ش�
					}
				} // else �� ��
			}
			if (e.getSource() == sendBtn || e.getSource() == textField_1) {
				String msg = null;
				msg = String.format("[%s] %s", id, textField_1.getText());
				send_Message(udp_socket, udp_packet, msg);
				textField_1.setText(""); // �޼����� ������ ���� �޼��� ����â�� ����.
				textField_1.requestFocus(); // �޼����� ������ Ŀ���� �ٽ� �ؽ�Ʈ �ʵ�� ��ġ��Ų��
				AppendText(msg);
			}
		}
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

	public void AppendText(String str) { //�ð� + �޽��� (���� ä��â���� ���)
		// textArea.append("����ڷκ��� ���� �޼��� : " + str+"\n");
		LocalTime now = LocalTime.now();
		int hour = now.getHour();
		int minute = now.getMinute();
		textArea.append("[" + hour + " : " + minute + "]" + str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void send_Message(DatagramSocket udp_socket, DatagramPacket udp_packet, String msgStr) { // Ŭ���̾�Ʈ�� �޼����� ������ �޼ҵ�
		byte[] buffer = new byte[BUF_LEN];
		String sendMsg = msgStr;
		buffer = sendMsg.getBytes();
		int port = 1234;
		try {
			// [����] https://happyourlife.tistory.com/102 ��Ƽĳ����
			InetAddress NetAddress = InetAddress.getByName("230.0.0.1");
			DatagramPacket server_udp_packet = new DatagramPacket(buffer, buffer.length, NetAddress, port);
			// echo back
			// udp_socket.send(server_udp_packet);
			mssocket.send(server_udp_packet); 

			Thread.sleep(100); //����ȭ�� �Ͼ�� �ֱ⿡ 0.1�� sleep
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static byte[] encodingBase64(byte[] targetBytes) { // Base64 ���ڵ�
		Encoder encoder = Base64.getEncoder(); 
		return encoder.encode(targetBytes);
	}

	public static BufferedImage resize(BufferedImage inputStream, int width, int height) throws IOException { //�̹������� �ػ� ����
		BufferedImage outputImage = new BufferedImage(width, height, inputStream.getType());
		Graphics2D graphics2D = outputImage.createGraphics();
		graphics2D.drawImage(inputStream, 0, 0, width, height, null);
		graphics2D.dispose();
		return outputImage;
	}

	private BufferedImage imageToBufferedImage(Image im) { //image�� BufferedImage�� �ٲ���
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
				//[����] https://runebook.dev/ko/docs/openjdk/java.desktop/javax/sound/sampled/audioformat
				format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
				try {

					microphone = AudioSystem.getTargetDataLine(format);

					DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); //�ͼ��� ������ ������
					microphone = (TargetDataLine) AudioSystem.getLine(info); //�ͼ��� ������ ����.
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
						// speakers.write(data, 0, numBytesRead); // ������ ����Ŀ�� ����
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

	// ��ó: https://jinseongsoft.tistory.com/260 [���� ����Ʈ]
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
					BufferedImage image; // ��ũ������ ����� ���۰���
					Robot r = new Robot(); // ��ũ������ ��� �κ�Ŭ����
					// BufferedOutputStream bout = new
					// BufferedOutputStream(socket.getOutputStream());
					// byte[] buffer;
					// ObjectOutputStream ois = new ObjectOutputStream(new BufferedOutputStream(os));

					while (true) {
						//��ũ������ �����鼭 �ٷ� ����
						image = resize(
								r.createScreenCapture(new Rectangle(0, 0, screenSize.width / 2, screenSize.height)),
								640, 480); // ȭ�� ��ũ���� 16:9 1008,567
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						// ����
						// image = resize(image , screenSize.width, screenSize.height);
						ImageIO.write(image, "jpeg", baos);
						baos.flush();
						// buffer = baos.toByteArray();

						DatagramPacket packet = new DatagramPacket(baos.toByteArray(), baos.toByteArray().length,
								NetAddress, 1235); //��Ŷ�� ������ ����
						System.out.println(baos.toByteArray().length);
						if (baos.toByteArray().length > 65507) { // buffer�� ũ�Ⱑ �ʹ� ũ�� error�� ��.. 65507 �ִ� ������ �۽�
							baos.reset(); // ������ �ʱ�ȭ
							continue;
						} // https://forum.snmp.app/t/length-of-original-trap-in-incoming-buffer-and-transportlistener-interface/141/3

						msvsocket.send(packet); // ȭ�������� �۽�

						// ois.writeObject(encodingBase64(baos.toByteArray())); //���ڵ��Ϸ��������� ����� �뷫 20000byte���� Ŀ���� ���� ����.
						// ois.flush();
						if (VideoOnOff.getIcon().equals(ScreenOff)) { //ȭ��۽� Off (ȭ���غ��� ȭ�� �۽�)
							baos.close();
							while (true) {
								ByteArrayOutputStream baosed = null;
								try {
									fileimage = ImageIO.read(file); //ȭ���غ��� ����
									image = resize(imageToBufferedImage(fileimage), 1280, 720); //����
									baosed = new ByteArrayOutputStream();
									ImageIO.write(image, "jpeg", baosed);
									baosed.flush();
									packet = new DatagramPacket(baosed.toByteArray(), baosed.toByteArray().length,
											InetAddress.getByName("230.0.0.1"), 1235);
									System.out.println(baosed.toByteArray().length);
									msvsocket.send(packet);//�ٽ� �ѷ���
									Thread.sleep(500); //ȭ��۽�ó�� �غ��߻����� �Ѹ��� ������ user�� ȿ���� ���������� ȿ���� ������. sleep�� 0.5�� ���ش�.
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

	private void List() { // Server List�� ǥ��
		list.setListData(UserVec);
		// AppendText(UserVec.toString()); //[host, user1] ���
		// UserVec������ Client���� ������
		try {
			outgoingdata = new String(UserVec.toString()).getBytes();
			outgoing = new DatagramPacket(outgoingdata, outgoingdata.length, InetAddress.getByName("230.0.0.1"), 10001);
			msstatsocket.send(outgoing); //login �Ǵ� logout�Ҷ� ���Դ� ���� ����, �����Ŀ� �ٽ� send
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void StatStart() { // ����,����.. �޽����� �޴� Thread
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
					// ���� �޽����� �޴� �κ�
					try {
						byte[] recvData = new byte[1000];
						// incoming.setLength(incoming.getData().length);
						incoming = new DatagramPacket(recvData, recvData.length);
						msstatsocket.receive(incoming); //login �Ǵ� logout�ÿ� �޴� ����name�� ����
						clientdata = new String(incoming.getData());
						// System.out.println(clientdata);
						// AppendText(clientdata);
						StringTokenizer st = new StringTokenizer(clientdata, "|"); // Ŭ���̾�Ʈ���� ������ token�� |�̾��� ���� �߶���
						int command = Integer.parseInt(st.nextToken());
						String name = st.nextToken();
						String text = st.nextToken();

						switch (command) {
						case 100: // login ó��
							// AppendText(name+text); //user�� �����ϼ̽��ϴ�
							UserVec.add(name);
							// UserVec�� ����
							List();
							break;
						case 200: // list ó��
							List();
							break;
						case 400: // logout ó��
							// AppendText(name+text); //user�� �����ϼ̽��ϴ�.
							for (int i = 0; i < UserVec.size(); i++)
								if (name.equals(UserVec.elementAt(i))) {
									UserVec.remove(i);
									break;
								}
							List();
							// ���⼭ outgoing ó��
							break;
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					} // ���� �޽����� �޴� �κ� ��
				}
			}
		});
		th.start();
	}

	// @SuppressWarnings("resource")
	private void ServerStart() {
		Thread th = new Thread(new Runnable() { // ����� ������ ���� ������
			@SuppressWarnings("resource")
			@Override
			public void run() {
				// socket = new ServerSocket(Port); // ������ ��Ʈ ���ºκ�
				Start.setText("����������");
				Start.setEnabled(false); // ������ ���̻� �����Ű�� �� �ϰ� ���´�
				textField.setEnabled(false); // ���̻� ��Ʈ��ȣ ������ �ϰ� ���´�
				udp_socket = null;
				// incoming = new DatagramPacket(new byte[60000],60000);

				try {
					udp_socket = new DatagramSocket(Port); //30000�� ��Ʈ datagramsocket
					mssocket = new MulticastSocket(); // client���� ���� �޽����� ��ü���� �ѷ�����ϴ� ����
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
						udp_socket.receive(udp_packet); //Ŭ���̾�Ʈ���� �޽����� ����
					} catch (IOException e) {
						e.printStackTrace();
					}
					String msgStr = new String(bb);
					// textArea.append(udp_packet.getPort()+" : ");
					AppendText(msgStr); //������ Ŭ���̾�Ʈ�� ���� �޽����� ���

					send_Message(udp_socket, udp_packet, msgStr); //��Ƽĳ������ ���Ͽ� Ŭ���̾�Ʈ�޽����� �ѷ��ִ� �޼ҵ� (��ü�޽����� ��)
				}
			}
		});
		th.start();
	}
}
