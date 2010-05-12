package org.openintents.wifiqr.desktop;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.html.ImageView;

import android.net.wifi.WifiConfiguration;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.ByteMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class WQRDesktop extends JFrame {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6119683655676347239L;



	public WQRDesktop(){
		super();
		init();
		pack();
		setVisible(true);
	}
	
	
	
	public static void main(String[] args){
		new WQRDesktop();
	}
	
	private String[] secTypes={"WPA","WPA2"};

	JTextField 	tfSSID		= 	new JTextField(10);
	JTextField 	tfPasswd	= 	new JTextField(10);

	JComboBox	cbSecType	=	new JComboBox(secTypes);

	JLabel 		encodedImageLabel= new JLabel();
	
	protected int mSelectedSecType=0;

	protected boolean includeDirectDownloadLink=true;
	protected boolean includeMarketDownloadLink=false;
	
	BufferedImage encodedWifiConfig=null;
	

	
	Graphics graphics2d=getGraphics();

	
	protected void init() {
	    setSize(350, 300);
	    center();

	    setLayout(new BorderLayout());
	    JPanel panel= new JPanel(new GridBagLayout());
			    // Add the menu bar.
		JMenuBar mb = new JMenuBar();
		JMenu file = new JMenu("File", true);

	//	file.add(new FilePrintAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
//		file.add(new FilePageSetupAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK|Event.SHIFT_MASK));
		file.addSeparator();
		file.add(new FileQuitAction()).setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Event.CTRL_MASK));
		mb.add(file);
	
	    setJMenuBar(mb);
	
	    GridBagConstraints gbc = new GridBagConstraints();

	    JLabel label = new JLabel("SSID",JLabel.LEFT);
	    gbc.gridx=0;
	    gbc.gridy=0;
	    gbc.gridwidth=1;
	    tfSSID.setText(" ");
	    panel.add(label,gbc);
	    gbc.gridx=2;
	    panel.add(tfSSID,gbc);
	    
	    gbc.gridx=0;
	    gbc.gridy++;
	    label=new JLabel("Password",JLabel.LEFT);
	    panel.add(label,gbc);
	    gbc.gridx=2;
	    panel.add(tfPasswd,gbc);

	    gbc.gridy++;
	    label=new JLabel("Encryption Type",JLabel.LEFT);
	    gbc.gridx=0;
	    panel.add(label,gbc);
	    gbc.gridx=1;
	    panel.add(cbSecType,gbc);

	    gbc.gridy++;
	    gbc.gridx=0;
	    panel.add(encodedImageLabel,gbc);
	    
	    
	    JButton printButton= new JButton();
	    printButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				WQRDesktop.this.createCode();
				WQRDesktop.this.printPage();
			}
		});
	    printButton.setText("Print Code");
	    gbc.gridx=200;
	    gbc.gridy=250;
	    panel.add(printButton,gbc);
	    
	    
	    
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(panel);
	    
	    addWindowListener(new WindowAdapter() {
	      public void windowClosing(WindowEvent e) {
	        System.exit(0);
	      }
	    });
	  }
	

	protected void createCode(){
		int secType=cbSecType.getSelectedIndex();

		WifiConfiguration wificonfig= new WifiConfiguration();
		wificonfig.password=tfPasswd.getText();
		wificonfig.SSID=tfSSID.getText();
		
		BufferedImage image=encodedWifiConfig=QRHelper.createTextCode("FIXME");
		ImageIcon icon= new ImageIcon();
		icon.setImage(image);
		encodedImageLabel.setIcon(icon);
	}
	
	protected void printPage() {
		if(includeDirectDownloadLink)
		{
			QRHelper.createDirectDownloadCode();
		}
		if(includeMarketDownloadLink)
		{
			QRHelper.createMarketDownloadCode();
		}
		// TODO print the code
	
	}



	protected void center() {
		  Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		  Dimension frameSize = getSize();
		  int x = (screenSize.width - frameSize.width) / 2;
		  int y = (screenSize.height - frameSize.height) / 2;
		  setLocation(x, y);
	  }
	 
	  public void showTitle() {
		  setTitle("OpenIntents.org WifiQR Desktop");
	  }

	  
	@SuppressWarnings("serial")
	public class FileQuitAction extends AbstractAction {
		  public FileQuitAction() {
			  super("Quit");
		  }
		  
		  public void actionPerformed(ActionEvent ae) {
			  System.exit(0);
		  }
	  }

}
