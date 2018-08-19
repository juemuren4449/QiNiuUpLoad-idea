package com.juemuren;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.TransferHandler;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.SystemColor;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

public class QiNiuUpLoad {

	private static JFrame frame;
	private String filePath;// �ļ�·��
	private Auth auth;// ��ţ��֤
	private static String bucketname;// ��ţ�ռ���
	private static JFrame settingFrame;// ���ý���
	private static File congigFile;// �����ļ�
	private static String ACCESS_KEY = "";// ACCESS_KEY
	private static String SECRET_KEY = "";// SECRET_KEY
	private static String beforeName = "";// �ļ�ǰ׺
	private static String WWW = "";// ��ţ����
	private JTextPane jTextPane;// �ļ��ϴ����

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					QiNiuUpLoad window = new QiNiuUpLoad();
					window.frame.setVisible(true);
					window.getPanel();

					// �˵�bar
					JMenuBar menuBar = new JMenuBar();
					frame.setJMenuBar(menuBar);

					// ���ò˵�
					JMenu menuSetting = new JMenu("����");
					menuSetting.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
					menuSetting.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							openSetting();// ������
						}

					});
					menuBar.add(menuSetting);

					// ���ڲ˵�
					JMenu menuAbout = new JMenu("����");
					menuAbout.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
					menuBar.add(menuAbout);
					menuAbout.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							try {
								Icon icon = new ImageIcon(ImageIO.read(this.getClass().getResource("/img/qiniu.png")));
								JOptionPane.showMessageDialog(frame,
										"�����ˣ���Ĺ��4449\n��Ŀ��ַ��https://github.com/juemuren4449/QiNiuUpLoad", "����", 1, icon);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public QiNiuUpLoad() {
		congigFile = new File("config.data");
		getConfig();// ��ȡ����
		initialize();
	}

	// ��ȡ����
	private void getConfig() {
		if (congigFile != null && congigFile.exists()) {
			try {
				FileInputStream fis = new FileInputStream(congigFile);
				byte[] b = new byte[1024];
				int len = 0;
				while ((len = fis.read(b)) != -1) {
					String string = new String(b, 0, len);
					// System.out.println(string);
					String[] strings = string.split("\n");
					ACCESS_KEY = strings[0];
					SECRET_KEY = strings[1];
					bucketname = strings[2];
					WWW = strings[3];
					System.out.println(ACCESS_KEY + ", " + SECRET_KEY + ", " + bucketname + ", " + WWW);
					if (strings.length == 5) {
						beforeName = strings[4];
						System.out.println(strings[4]);
						System.out.println(beforeName);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				jTextPane.setText("��������ʧ�ܣ������������������д������Ϣ");
			}
		}
	}

	// ��ȡ�ļ���
	public String getFileName(String filePath) {
		File mFile = new File(filePath);
		return mFile.getName().replace("%", "").replace("#", "");// �ļ������ܰ���%��#�����Զ�ɾ���Ƿ��ַ�
	}

	public String getUpToken() {
		return auth.uploadToken(bucketname);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		Font f = new Font("Microsoft YaHei UI", Font.PLAIN, 12);
		UIManager.put("Label.font", f);
		UIManager.put("Button.font", f);
		UIManager.put("TitledBorder.font", f);

		frame = new JFrame();
		frame.setTitle("��ţͼ���ϴ����");
		frame.setBounds(400, 200, 550, 350);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			frame.setIconImage(ImageIO.read(this.getClass().getResource("/img/qiniu.png")));// ����ͼ��
		} catch (IOException e) {
			e.printStackTrace();
		}

		jTextPane = getPanel();
		frame.getContentPane().add(jTextPane);
	}

	public JTextPane getPanel() {
		JTextPane fileTarget = new JTextPane();
		fileTarget.setMargin(new Insets(60, 3, 3, 3));

		// ��������ˮƽ������ʾ
		StyledDocument doc = fileTarget.getStyledDocument();
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
		doc.setParagraphAttributes(0, doc.getLength(), center, false);

		fileTarget.setAlignmentX(0.5f);
		fileTarget.setToolTipText("���ļ��϶����˴��ϴ�");
		fileTarget.setEditable(false);
		fileTarget.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
		fileTarget.setText("���ļ��϶����˴��ϴ�");
		fileTarget.setBackground(UIManager.getColor("Button.background"));
		fileTarget.setDragEnabled(true);
		fileTarget.setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = 1L;

			public boolean importData(JComponent c, Transferable t) {
				try {
					Object o = t.getTransferData(DataFlavor.javaFileListFlavor);
					// �˴�����ļ�/�ļ��е������Լ�·��
					System.out.println(o.toString());
					filePath = o.toString().substring(1, o.toString().length() - 1);

					if (filePath != null && !"".equals(filePath)) {
						// getKey();
						if (ACCESS_KEY != null && SECRET_KEY != null && !"".equals(ACCESS_KEY) && !"".equals(SECRET_KEY)
								&& WWW != null && !"".equals(WWW)) {
							auth = Auth.create(ACCESS_KEY, SECRET_KEY);
						}
						fileTarget.setDragEnabled(false);
						new Thread(new Runnable() {

							@Override
							public void run() {
								Response res;
								try {
									// ����put�����ϴ�
									if (auth != null) {
										fileTarget.setText("�ϴ���...\n\n��ȴ��ϴ���ɺ��ٽ��в���");
										res = new UploadManager().put(filePath, beforeName + EncryptHelper.FileNameToMD5(),
												getUpToken());
										System.out.println(res.bodyString());
										String json = res.bodyString();
										Result result = new Gson().fromJson(json, Result.class);
										String fileName;
										if (result.getKey().contains("\\")) {
											fileName = result.getKey().replace("\\", "\\\\");
										} else {
											fileName = result.getKey();
										}
										System.out.println("fileName=" + fileName);

										fileTarget.setText(WWW + fileName + "\n\n" + "�����Ѿ����Ƶ�������\n\n�϶��ļ����˴������ϴ�");

										// �洢��������
										StringSelection stsel = new StringSelection(WWW + fileName);
										Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stsel, stsel);
										fileTarget.setDragEnabled(true);
									} else {
										JOptionPane.showMessageDialog(frame,
												"�������ò鿴�Ƿ�������ACCESS_KEY��SECRET_KEY���ռ����ƺ���ţ����");
										openSetting();
									}
								} catch (QiniuException e) {
									e.printStackTrace();
									fileTarget.setText("�ϴ�ʧ�ܣ��������ϴ�");
								}

							}
						}).start();
					}
					return true;
				} catch (UnsupportedFlavorException ufe) {
					ufe.printStackTrace();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}

			public boolean canImport(JComponent c, DataFlavor[] flavors) {
				return true;
			}
		});
		return fileTarget;
	}

	// ������
	private static void openSetting() {
		JDialog jDialog = new JDialog(frame, "����", true);
		jDialog.getContentPane().setBackground(UIManager.getColor("Button.background"));
		jDialog.setBounds(400, 200, 550, 350);
		jDialog.getContentPane().setLayout(null);

		// ������ACCESS_KEY
		JTextArea textAccessKey = new JTextArea();
		textAccessKey.setEditable(false);
		textAccessKey.setDisabledTextColor(Color.black);
		textAccessKey.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
		textAccessKey.setBackground(UIManager.getColor("Button.background"));
		textAccessKey.setEnabled(false);
		textAccessKey.setText("������ACCESS_KEY��");
		textAccessKey.setBounds(26, 36, 121, 21);
		jDialog.getContentPane().add(textAccessKey);

		// ACCESS_KEY
		JEditorPane editAccessKey = new JEditorPane();
		editAccessKey.setBounds(157, 36, 335, 21);
		editAccessKey.requestFocus();
		jDialog.getContentPane().add(editAccessKey);

		// ����SECRET_KEY
		JTextArea textSecretKey = new JTextArea();
		textSecretKey.setDisabledTextColor(Color.black);
		textSecretKey.setText("������SECRET_KEY��");
		textSecretKey.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
		textSecretKey.setEnabled(false);
		textSecretKey.setEditable(false);
		textSecretKey.setBackground(UIManager.getColor("Button.background"));
		textSecretKey.setBounds(26, 79, 121, 21);
		jDialog.getContentPane().add(textSecretKey);

		// SECRET_KEY
		JEditorPane editSecretKey = new JEditorPane();
		editSecretKey.setBounds(157, 79, 335, 21);
		jDialog.getContentPane().add(editSecretKey);

		// ����ռ���
		JTextArea textName = new JTextArea();
		textName.setText("������ռ�����");
		textName.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
		textName.setEnabled(false);
		textName.setEditable(false);
		textName.setDisabledTextColor(Color.black);
		textName.setBackground(UIManager.getColor("Button.background"));
		textName.setBounds(26, 118, 121, 21);
		jDialog.getContentPane().add(textName);

		// �ռ���
		JEditorPane editName = new JEditorPane();
		editName.setBounds(157, 118, 335, 21);
		jDialog.getContentPane().add(editName);

		// ������ţ�洢�ؼ�������
		JTextArea textWWW = new JTextArea();
		textWWW.setText("��������ţ������");
		textWWW.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
		textWWW.setEnabled(false);
		textWWW.setEditable(false);
		textWWW.setDisabledTextColor(Color.black);
		textWWW.setBackground(UIManager.getColor("Button.background"));
		textWWW.setBounds(26, 162, 121, 21);
		jDialog.getContentPane().add(textWWW);

		// ǰ׺��
		JEditorPane editWWW = new JEditorPane();
		editWWW.setBounds(157, 162, 335, 21);
		jDialog.getContentPane().add(editWWW);

		// ����ǰ׺��
		JTextArea textBefore = new JTextArea();
		textBefore.setText("������ǰ׺����");
		textBefore.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
		textBefore.setEnabled(false);
		textBefore.setEditable(false);
		textBefore.setDisabledTextColor(Color.black);
		textBefore.setBackground(UIManager.getColor("Button.background"));
		textBefore.setBounds(26, 202, 121, 21);
		jDialog.getContentPane().add(textBefore);

		// ǰ׺��
		JEditorPane editBefore = new JEditorPane();
		editBefore.setBounds(157, 202, 335, 21);
		jDialog.getContentPane().add(editBefore);

		if (ACCESS_KEY != null && !"".equals(ACCESS_KEY)) {
			editAccessKey.setText(ACCESS_KEY);
		}
		if (SECRET_KEY != null && !"".equals(SECRET_KEY)) {
			editSecretKey.setText(SECRET_KEY);
		}
		if (bucketname != null && !"".equals(bucketname)) {
			editName.setText(bucketname);
		}
		if (WWW != null && !"".equals(WWW)) {
			editWWW.setText(WWW);
		}
		if (beforeName != null && !"".equals(beforeName)) {
			if (!beforeName.endsWith("_")) {
				beforeName = beforeName + "_";
			}
			editBefore.setText(beforeName);
		}
		// ȷ����ť
		JButton btnOk = new JButton("ȷ��");
		btnOk.setBounds(135, 261, 93, 23);
		btnOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					if (!congigFile.exists()) {
						congigFile.createNewFile();
					}
					if (!"".equals(editAccessKey.getText().trim())) {
						if (!"".equals(editSecretKey.getText().trim())) {
							if (!"".equals(editName.getText().trim())) {
								if (!"".equals(editWWW.getText().trim())) {
									if (!"".equals(editBefore.getText().trim())) {
										beforeName = editBefore.getText().trim() + "_";
									}
									// ��ֵ������
									ACCESS_KEY = editAccessKey.getText().trim();
									SECRET_KEY = editSecretKey.getText().trim();
									bucketname = editName.getText().trim();

									if (editWWW.getText().trim().endsWith("/")) {
										WWW = editWWW.getText().trim();
									} else {
										WWW = editWWW.getText().trim() + "/";
									}

									System.out.println(ACCESS_KEY + ", " + SECRET_KEY + ", " + bucketname + WWW);

									// ����Ϣд�뵽�����ļ�
									FileOutputStream fos = new FileOutputStream(congigFile);
									fos.write((ACCESS_KEY + "\n" + SECRET_KEY + "\n" + bucketname + "\n" + WWW + "\n"
											+ beforeName).getBytes());
									fos.close();
									jDialog.setVisible(false);
									JOptionPane.showMessageDialog(frame, "���óɹ���", "��ʾ", 1);
								} else {
									JOptionPane.showMessageDialog(frame, "��������ţ����", "��ʾ", 2);
								}
							} else {
								JOptionPane.showMessageDialog(frame, "������洢�ռ���", "��ʾ", 2);
							}
						} else {
							JOptionPane.showMessageDialog(frame, "������SECRET_KEY", "��ʾ", 2);
						}
					} else {
						JOptionPane.showMessageDialog(frame, "������ACCESS_KEY", "��ʾ", 2);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		jDialog.getContentPane().add(btnOk);

		// ȡ����ť
		JButton btnCancle = new JButton("ȡ��");
		btnCancle.setBounds(255, 261, 93, 23);
		btnCancle.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				jDialog.setVisible(false);
			}
		});
		jDialog.getContentPane().add(btnCancle);

		jDialog.setVisible(true);
	}
}
