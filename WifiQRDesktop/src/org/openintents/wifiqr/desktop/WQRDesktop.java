package org.openintents.wifiqr.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.html.ImageView;

import org.openintents.wifiqr.util.WifiConfigHelper;

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
	
	private String[] secTypes={"WPA","WPA2","WEP"};

	JTextField 	tfSSID		= 	new JTextField(10);
	JTextField 	tfPasswd	= 	new JTextField(10);

	JComboBox	cbSecType	=	new JComboBox(secTypes);

	JLabel 		encodedImageLabel= new JLabel();
	JLabel 		passlabel=new JLabel();
	String 		secType		=	new String();
	
	JTextField  tfWepkey1= new JTextField(10);
	JTextField  tfWepkey2= new JTextField(10);
	JTextField  tfWepkey3= new JTextField(10);
	JTextField  tfWepkey4= new JTextField(10);
	JTextField[] tfWepkeys={tfWepkey1,tfWepkey2,tfWepkey3,tfWepkey4};
	JLabel[]	labelWepkey= new JLabel[4];
	protected int mSelectedSecType=0;

	protected boolean includeDirectDownloadLink=true;
	protected boolean includeMarketDownloadLink=false;
	
	JCheckBox	ckIncludeDirectDownload= new JCheckBox();
	JCheckBox	ckIncludeMarketDownload= new JCheckBox();
	JCheckBox	ckIncludeAndAppstoreDownload= new JCheckBox();
	
	
	BufferedImage encodedWifiConfig=null;
	

	
	Graphics graphics2d=getGraphics();

	JLabel encodedDirectLabel=new JLabel();

	JLabel encodedMarketLabel= new JLabel();

	
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
	    passlabel=new JLabel("Password",JLabel.LEFT);
	    panel.add(passlabel,gbc);
	    gbc.gridx=2;
	    panel.add(tfPasswd,gbc);

	    for (int i=0;i<4;i++)
	    {
		    gbc.gridx=0;
		    gbc.gridy++;
		    labelWepkey[i]=new JLabel("Passkey "+(i+1),JLabel.LEFT);
		    labelWepkey[i].setVisible(false);
		    panel.add(labelWepkey[i],gbc);
		    gbc.gridx=2;
		    panel.add(tfWepkeys[i],gbc);
		    tfWepkeys[i].setVisible(false);
	    	
	    }
	    
	    
	    gbc.gridy++;
	    label=new JLabel("Encryption Type",JLabel.LEFT);
	    gbc.gridx=0;
	    panel.add(label,gbc);
	    gbc.gridx=1;
	    panel.add(cbSecType,gbc);
	    cbSecType.addItemListener(new ItemListener(){

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange()==ItemEvent.SELECTED)
				{
					secType=(String)cbSecType.getSelectedItem();
					swichtPassFields();
				}
				
			}
	    	
	    });


	    gbc.gridx=0;
	    gbc.gridy++;
	    label=new JLabel("Include direct download",JLabel.LEFT);
	    panel.add(label,gbc);
	    gbc.gridx=2;
	    panel.add(ckIncludeDirectDownload,gbc);	    

	    gbc.gridx=0;
	    gbc.gridy++;
	    label=new JLabel("Include Market download",JLabel.LEFT);
	    panel.add(label,gbc);
	    gbc.gridx=2;
	    panel.add(ckIncludeMarketDownload,gbc);
	    
	    gbc.gridx=0;
	    gbc.gridy++;
	    label=new JLabel("Include AndAppstore download",JLabel.LEFT);
	    panel.add(label,gbc);
	    gbc.gridx=2;
	    panel.add(ckIncludeAndAppstoreDownload,gbc);
	    
	    
	    
	    gbc.gridx=0;
	    gbc.gridy++;
	    panel.add(encodedImageLabel,gbc);
	    gbc.gridx=2;
	    gbc.gridy++;
	    panel.add(encodedMarketLabel,gbc);    
	    gbc.gridx=4;
	    gbc.gridy++;
	    panel.add(encodedDirectLabel,gbc);	   
	    
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
	

	protected void swichtPassFields() {
		if(secType.equals("WEP"))
		{
			for (int i=0;i<4;i++)
			{
				labelWepkey[i].setVisible(true);
				tfWepkeys[i].setVisible(true);
			}
			tfPasswd.setVisible(false);
			passlabel.setVisible(false);
		}else{
			for (int i=0;i<4;i++)
			{
				labelWepkey[i].setVisible(false);
				tfWepkeys[i].setVisible(false);
			}			
			tfPasswd.setVisible(true);
			passlabel.setVisible(true);
		}
		pack();
	}



	protected void createCode(){
		int secType=cbSecType.getSelectedIndex();

		WifiConfiguration wificonfig= new WifiConfiguration();
		wificonfig.password=tfPasswd.getText();
		wificonfig.SSID=tfSSID.getText();
		
		Vector<String>wepKeys=new Vector<String>();
		int wepKeysTotal=0;
		for (int i=0;i<4;i++)
		{
			final String value=tfWepkeys[i].getText();
			if(value!=null && ! value.equals(""))
			{					
				wepKeys.add("\""+value+"\"");
				wepKeysTotal++;
			}
		}
		
		String[] strWepKeys=new String[wepKeysTotal];
		wepKeys.toArray(strWepKeys);						
		wificonfig.wepKeys=strWepKeys;
		String codeContent=WifiConfigHelper.writeToString(wificonfig,this.secType);
		BufferedImage image=encodedWifiConfig=QRHelper.createTextCode(codeContent);
		ImageIcon icon= new ImageIcon();
		icon.setImage(image);
		encodedImageLabel.setIcon(icon);
		
		if(ckIncludeDirectDownload.isSelected())
		{
			BufferedImage encodedMarketLink=QRHelper.createDirectDownloadCode();
			ImageIcon icon1= new ImageIcon();
			icon.setImage(encodedMarketLink);
			encodedDirectLabel.setIcon(icon1);		
		}
		if(ckIncludeMarketDownload.isSelected())
		{
			BufferedImage encodedMarketLink=QRHelper.createMarketDownloadCode();
			ImageIcon icon1= new ImageIcon();
			icon.setImage(encodedMarketLink);
			encodedMarketLabel.setIcon(icon1);
		}
		if(ckIncludeAndAppstoreDownload.isSelected())
		{
			
		}		
		pack();
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
