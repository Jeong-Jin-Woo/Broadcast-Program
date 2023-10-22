// Client.Java Java Chatting Client �� Nicknam, IP, Port ��ȣ �Է��ϰ� �����ϴ� �κ�

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;

public class JavaUdpEchoClientMain extends JFrame {
	private JTextField tf_ID; // ID�� �Է¹�����
	private JTextField tf_IP; // IP�� �Է¹�����
	private JTextField tf_PORT; //PORT�� �Է¹�����
	private JPanel contentPane;
	private ImageIcon icon = new ImageIcon("src/HansungTv(600x400).png");
	
	public static void main(String[] args) {
		JavaUdpEchoClientMain client = new JavaUdpEchoClientMain();
		client.setVisible(true);
	}
	

	
	public JavaUdpEchoClientMain() // ȭ�� ����
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(900, 220, 615, 438);
		contentPane = new JPanel() {
			public void paintComponent(Graphics g) {
				g.drawImage(icon.getImage(), 0, 0, null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNewLabel = new JLabel("UserName");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(200, 254, 68, 34);
		contentPane.add(lblNewLabel);
		

		tf_ID = new JTextField();
		tf_ID.setHorizontalAlignment(SwingConstants.CENTER);
		tf_ID.setBounds(275, 261, 114, 21);
		contentPane.add(tf_ID);
		tf_ID.setColumns(10);
	

		JLabel lblServerIp = new JLabel("Server IP");
		lblServerIp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblServerIp.setBounds(210, 279, 58, 34);
		contentPane.add(lblServerIp);

		tf_IP = new JTextField();
		tf_IP.setText("127.0.0.1");
		tf_IP.setHorizontalAlignment(SwingConstants.CENTER);
		tf_IP.setColumns(10);
		tf_IP.setBounds(275, 286, 114, 21);
		contentPane.add(tf_IP);

		JLabel lblPort = new JLabel("Port");
		lblPort.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPort.setBounds(236, 305, 32, 34);
		contentPane.add(lblPort);

		tf_PORT = new JTextField();
		tf_PORT.setText("30000");
		tf_PORT.setHorizontalAlignment(SwingConstants.CENTER);
		tf_PORT.setColumns(10);
		tf_PORT.setBounds(275, 312, 114, 21);
		contentPane.add(tf_PORT);
		
		JButton btnNewButton = new JButton("Connect");
		btnNewButton.setFont(new Font("���� ��� Semilight", Font.BOLD, 17));
		btnNewButton.setForeground(new Color(255, 255, 255));
		btnNewButton.setBackground(new Color(135, 206, 250));
		btnNewButton.setBounds(205, 339, 184, 52);
		contentPane.add(btnNewButton);
		
		ConnectAction action = new ConnectAction();
		btnNewButton.addActionListener(action);
		tf_PORT.addActionListener(action);
		tf_ID.addActionListener(action);
		tf_IP.addActionListener(action);	
	}
	class ConnectAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {			
			String _id = tf_ID.getText().trim(); // ������ ���� �𸣴� ���� ���� trim() ���
			String _ip= tf_IP.getText().trim(); // ������ ������ �𸣹Ƿ� ��������
			int _port=Integer.parseInt(tf_PORT.getText().trim()); // ������ ������ �� int������ ��ȯ 			
			JavaUdpEchoClientView view = new JavaUdpEchoClientView(_id,_ip,_port);
			setVisible(false);						
		}
	}
}
