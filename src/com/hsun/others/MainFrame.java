package com.hsun.others;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	RestUtility restUtility;
	
	boolean isUpdate;
	
	int curPrice;
	JLabel diffPriceLabel;
	JScrollPane priceListPane;
	JTextArea priceTextArea;
	int confirmPrice = -1;
	JLabel errorMsgLabel;
	
	TrayIcon trayIcon = null;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {//Schedule a job for the event-dispatching thread: adding TrayIcon.
            @Override
            public void run() {
            	MainFrame mainFrame = new MainFrame();
            }
        });
		
	}
	
	public MainFrame(){
		
		if(!SystemTray.isSupported()){
		    System.out.println("System tray is not supported !!! ");
		    return;
		}
		
		restUtility = RestUtility.getInstance();
		//get the systemTray of the system
		SystemTray systemTray = SystemTray.getSystemTray();
		
		Image image = Toolkit.getDefaultToolkit().getImage("src/images/1.gif");
		
		//popupmenu
		PopupMenu trayPopupMenu = new PopupMenu();
		
		//1t menuitem for popupmenu
		MenuItem action = new MenuItem("Show");
		action.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	setVisible(true);
		    	isUpdate = true;
		    }
		});  
		trayPopupMenu.add(action);
		
		//2nd menuitem of popupmenu
		MenuItem close = new MenuItem("Exit");
		close.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        System.exit(0);
		    }
		});
		trayPopupMenu.add(close);
		
		//setting tray icon
		trayIcon = new TrayIcon(image,"", trayPopupMenu);
		
		trayIcon.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	setVisible(true);
		    	isUpdate = true;
		    }
		});
		
		//adjust to default size as per system recommendation 
		trayIcon.setImageAutoSize(true);
		
		try{
		    systemTray.add(trayIcon);
		}catch(AWTException awtException){
		    awtException.printStackTrace();
		}
		
		this.errorMsgLabel = new JLabel();
		this.diffPriceLabel = new JLabel();
		this.priceTextArea = new JTextArea();
		priceTextArea.setEditable(false);
		this.errorMsgLabel.setForeground(Color.red);
		
		this.setTitle("test");
		this.setSize(150,220);
		this.setResizable(false);
		this.setVisible(true);
		
		this.isUpdate = true;
		
		
		Container container = this.getContentPane();
		container.setLayout(new FlowLayout(FlowLayout.CENTER));
		container.setBackground(Color.WHITE);
		
		getContent(container);
		
		this.setLocation(0,0);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		RefreshPrice process = new RefreshPrice();
		Thread refreshThread = new Thread(process);
        try {
        	refreshThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void getContent(Container container){
		this.priceListPane = new JScrollPane(this.priceTextArea);
		
		final JTextField confirmPriceText = new JTextField();
		Dimension orderPriceD = confirmPriceText.getPreferredSize();
		orderPriceD.width = 100;
		JButton confirmButton = new JButton();
		
		confirmPriceText.setPreferredSize(orderPriceD);
		
		confirmButton.setText("confirm");
		Dimension confirmButtonD = confirmButton.getPreferredSize();
		confirmButtonD.width = 100;
		
		confirmButton.setPreferredSize(confirmButtonD);
		confirmButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(confirmPriceText.getText().trim().length()==0){
					confirmPrice = -1;
					errorMsgLabel.setText("");
				}else{
					if(!confirmPriceText.getText().matches("[0-9]+")){
						errorMsgLabel.setText("輸入正整數啦幹");
						confirmPriceText.setText("");
						confirmPrice = -1;
					}else{
						errorMsgLabel.setText("");
						confirmPrice = Integer.parseInt(confirmPriceText.getText());
					}
				}
			}
		});
		
		
		Dimension pricePaneD = priceListPane.getPreferredSize();
		pricePaneD.width = 120;
		pricePaneD.height = 80;
		
		priceListPane.setPreferredSize(pricePaneD);

		diffPriceLabel.setText(" ");
		Dimension diffPriceLabelD = diffPriceLabel.getPreferredSize();
		diffPriceLabelD.width = 100;
		diffPriceLabel.setPreferredSize(diffPriceLabelD);
		
		container.add(errorMsgLabel);
		container.add(confirmPriceText);
		container.add(diffPriceLabel);
		container.add(confirmButton);
		container.add(priceListPane);
	}
	
	public void refreshPrice(){
		Date curTime = new Date();
		String diffPriceStr = "";
		int diffPrice;
		this.priceTextArea.setText(this.priceTextArea.getText()+String.valueOf(curPrice)+"　"+curTime.getHours()+":"+curTime.getMinutes()+":"+curTime.getSeconds()+"\n");
		if(this.confirmPrice > -1){
			diffPrice = curPrice - confirmPrice;
			diffPriceStr = String.valueOf(diffPrice);
			diffPriceLabel.setText(diffPriceStr);
			if(diffPrice==0){
				diffPriceLabel.setForeground(Color.BLACK);
			}else if(diffPrice>0){
				diffPriceLabel.setForeground(Color.RED);
			}else{
				diffPriceLabel.setForeground(Color.GREEN);
			}
		}else{
			diffPriceLabel.setText(" ");
		}
		priceListPane.getVerticalScrollBar().setValue(priceListPane.getVerticalScrollBar().getMaximum());
	}

  	class RefreshPrice extends SwingWorker<Object, Object>{

		@Override
		protected Object doInBackground() throws Exception {
	  		try{
		  		while(true){
			  		if(isUpdate){
			  			curPrice = restUtility.getTxPrice();
			  			trayIcon.setToolTip(String.valueOf(curPrice));
				  		refreshPrice();
					}
			  		Thread.sleep(10000);//10sec refresh
		  		}
	  		}catch(Exception e){
	  			e.printStackTrace();
	  		}
			return null;
		}
  	}
}
