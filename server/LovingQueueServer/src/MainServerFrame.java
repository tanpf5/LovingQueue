import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.filechooser.FileFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import BarCode.BarCode;


public class MainServerFrame extends JFrame implements ActionListener{
	private Vector<MainServer>servers;
	private MainServer currentServer;
	private ReadWriteLock rwlock;  //lock for currentServer
	private ReadWriteLock rwlockForServers;
	private BarCode barCode;
	private JLabel currentNum;
	private JLabel currentPassword;
	private JLabel currentLength;
	private JComponent serverPane;
	private JFileChooser fileChooser;
	private JList<String> list;
	private DefaultListModel<String> listModel;
	private int currentIndex=-1;
	private String content=new String("");
	//I added
	private JLabel serverName;
	private JLabel serverIP;
	private JLabel serverPort;
	private JButton nextButton;
	private JButton newButton;
	private JButton startButton;
	private JButton generateButton;  //generate BarCode
	private JButton saveButton;
	private JButton iconButton;
	private JTextField nameField;
	private JTextField ipField;
	private JTextField portField;
	
	//for generate BarCode Demonstration
	private ImageIcon icon;
	
	private Timer updateTimer;
	private Timer updateFileTimer;
	private Thread serverThread;
	public MainServerFrame() {
		super();
		this.setSize(new Dimension(850,600));
		this.setLayout(null);
		this.setTitle("Loving Queue Server");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		rwlock= new ReentrantReadWriteLock();
		rwlockForServers= new ReentrantReadWriteLock();
		servers=new Vector<MainServer>();
		//启动server线程
		currentServer=new MainServer();
		
		serverName=new JLabel("Name: ");
		serverName.setBounds(250,100,400,50);
		serverIP=new JLabel("Server IP: ");
		serverIP.setBounds(250,150,400,50);
		serverPort=new JLabel("Server Port: ");
		serverPort.setBounds(250,200,400,50);
		nameField = new JTextField();
		nameField.setBounds(350, 115, 150, 20);
		ipField = new JTextField();
		ipField.setBounds(350, 165, 150, 20);
		portField = new JTextField();
		portField.setBounds(350, 215, 150, 20);
		
		currentNum=new JLabel("CurrentNumber: ");
		currentNum.setBounds(250, 250, 400, 50);
		currentPassword=new JLabel("CurrentPassword: ");
		currentPassword.setBounds(250,300,400,50);
		currentLength=new JLabel("QueueLength: ");
		currentLength.setBounds(250,350,400,50);
		nextButton=new JButton("Next");
		nextButton.setBounds(250,440,80,30);
		nextButton.setActionCommand("next");
		nextButton.addActionListener(this);
		newButton=new JButton("New");
		newButton.setBounds(380,440,80,30);
		newButton.setActionCommand("new");
		newButton.addActionListener(this);
		startButton=new JButton("Start");
		startButton.setBounds(510,440,80,30);
		startButton.setActionCommand("start");
		startButton.addActionListener(this);
		saveButton=new JButton("Save");
		saveButton.setBounds(270,520,120,30);
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		generateButton=new JButton("Generate");
		generateButton.setBounds(430,520,120,30);
		generateButton.setActionCommand("generate");
		generateButton.addActionListener(this);
		iconButton=new JButton();
		iconButton.setBounds(570,115,230,230);
		
		serverPane=new ListServer();
		serverPane.setBounds(30,110,130,300);
		serverPane.setOpaque(true);
		add(serverPane);
		
		add(serverName);
		add(nameField);
		add(serverIP);
		add(ipField);
		add(serverPort);
		add(portField);
		add(currentNum);
		add(currentPassword);
		add(currentLength);
		add(nextButton);
		add(newButton);
		add(startButton);
		add(saveButton);
		add(generateButton);
		add(iconButton);
		
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		//更新当前头信息的线程
        updateTimer=new Timer(true);
        updateTimer.scheduleAtFixedRate(new UpdateTask(this), new Date(), 1000);
		updateFileTimer=new Timer(true);
		updateFileTimer.scheduleAtFixedRate(new UpdateFileTask(this), new Date(), 30000);
	}
	
	
	class UpdateTask extends TimerTask {
		MainServerFrame msf;
		public UpdateTask(MainServerFrame t) {
			msf=t;
		}
		@Override
		public void run() {
			msf.updateInformation();
		}
	}
	
	private void updateInformation() {
		if(currentServer.status==true){
			startButton.setText("Close");
			startButton.setActionCommand("close");
		} else {
			startButton.setText("Start");
			startButton.setActionCommand("start");
		}
		int t;
		currentServer.rwlock.readLock().lock();
		QueueElement head=currentServer.queue.peek();
		t=currentServer.queue.size();
		currentServer.rwlock.readLock().unlock();
		if (head!=null) {
			currentNum.setText("CurrentNumber:   "+head.queueNumber);
			currentPassword.setText("CurrentPassword:   "+head.password);
		} else {
			currentNum.setText("CurrentNumber:   None");
			currentPassword.setText("CurrentPassword:   None");
		}
		currentLength.setText("QueueLength:   "+t);
	}

	class UpdateFileTask extends TimerTask{
		MainServerFrame msf;
		public UpdateFileTask(MainServerFrame t){
			msf=t;
		}
		@Override
		public void run() {
			msf.updateFileInformation();
		}
	}
	
	private void updateFileInformation() {
		rwlockForServers.readLock().lock();
		for(int i=0;i<servers.size();i++){
			MainServer ms=servers.get(i);
			content=content+ms.name+"|"+ms.ip+"|"+Integer.toString(ms.port)+"|";
			int t;
			Queue<QueueElement> queue = new LinkedList<QueueElement>();
			ms.rwlock.readLock().lock();
			queue=ms.queue;
			t=queue.size();
			ArrayList<QueueElement> arrayList= new ArrayList<QueueElement>(queue);
			ms.rwlock.readLock().unlock();
			content+=ms.currentNumber+"|";
			content+=t+"|";
			for(int j=0;j<t;j++){
				content+=arrayList.get(j).queueNumber+"|"+arrayList.get(j).password+"|";
			}
			content=content+"\n";
		}
		rwlockForServers.readLock().unlock();
		File file=new File("ServerInformation.txt");
		FileOutputStream out;
		try {
			//System.out.println("Size is "+servers.size());
			//System.out.println("content is "+content);
			out = new FileOutputStream(file);
			out.write(content.getBytes());
			out.close();
			content=new String("");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("next")) {
			currentServer.rwlock.writeLock().lock();
			currentServer.queue.poll();
			currentServer.rwlock.writeLock().unlock();
		}
		if (e.getActionCommand().equals("new")) {
			QueueElement qe=currentServer.genNewElement();
			JOptionPane.showMessageDialog(this, String.format("Please take a note:\nnum=%d\npassword=%s",qe.queueNumber,qe.password), "new",JOptionPane.INFORMATION_MESSAGE);
		}
		if(e.getActionCommand().equals("start")){
			currentServer.serverThread=new Thread(currentServer);
			currentServer.serverThread.start();
		}
		if(e.getActionCommand().equals("close")){
			currentServer.status=false;
			currentServer.serverThread=null;
//			if(currentServer.status==false){
//				startButton.setText("Start");
//				startButton.setActionCommand("start");
//			}
		}
		if(e.getActionCommand().equals("save")){
			/*
			int index=list.getSelectedIndex();
			if(index==0){
				JOptionPane.showMessageDialog(null, "Default Server can't be modified!","Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			*/
			if(currentIndex!=-1){
				String nameCopy=nameField.getText();
				String ipCopy=ipField.getText();
				String portCopy=portField.getText();
				servers.get(currentIndex).name=nameCopy;
				servers.get(currentIndex).ip=ipCopy;
				servers.get(currentIndex).port=Integer.parseInt(portCopy);
				rwlock.writeLock().lock();
				currentServer=servers.get(currentIndex);
				System.out.println("port "+currentServer.port);
				rwlock.writeLock().unlock();
				listModel.setElementAt(nameCopy, currentIndex);
				JOptionPane.showMessageDialog(this, "Saved Successfully!");
			}
			icon=new ImageIcon("");
			iconButton.setIcon(icon);
		}
		if(e.getActionCommand().equals("generate")){
		    if(fileChooser == null){
			    fileChooser = new JFileChooser();
			    fileChooser.setFileFilter(new FileFilter(){
			    	public boolean accept(File f) {
			    		if(f.isDirectory() || f.getName().endsWith(".png")){
			    			return true;
			    		}
			    		return false;
			    	}
			    	public String getDescription(){
			    	   return "图片文件";
			    	}
			});
		          fileChooser.setCurrentDirectory(new File("."));					
			}
		    if (barCode==null)
		    	barCode=new BarCode();
		    int result = fileChooser.showSaveDialog(MainServerFrame.this);
			if(result == JFileChooser.APPROVE_OPTION){
				String name=fileChooser.getSelectedFile().getAbsolutePath();
				if(fileChooser.getSelectedFile().exists()) {
					int i=JOptionPane.showConfirmDialog(null,"File already existed,do you want to overwrite it?", "File save",JOptionPane.OK_CANCEL_OPTION);
					if (i!=0)
						return;
					if (!fileChooser.getSelectedFile().delete()) {
						System.out.println("delete error!");
						return;
					}
				}	
				rwlock.readLock().lock();
				String content=currentServer.ip+":"+Integer.toString(currentServer.port)+"|"+currentServer.name;
				rwlock.readLock().unlock();
				System.out.println("content="+content);
				System.out.println("name="+name);
				barCode.encode(content,300, 300, name);
				icon=new ImageIcon(name);
				icon.getImage().flush();
				iconButton.setIcon(icon);
		    }
		}
		
	}
	
	public class ListServer extends JPanel implements ListSelectionListener{

		private JButton removeButton; 
		public ListServer(){
			super(new BorderLayout());
			listModel = new DefaultListModel<String>();
			
			int judge=Initialize();
			if(judge==0){
				listModel.insertElementAt("Server-default",0);
				MainServer addServer=new MainServer();
				addServer.name="Server-default";
				addServer.ip="0.0.0.0";
				addServer.port=-1;
				servers.add(addServer);
			}
			
			//listModel.remove(0);
	        list = new JList<String>(listModel);
	        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        //list.setSelectedIndex(0);
	        list.addListSelectionListener(this);
	        list.setVisibleRowCount(10);
	        list.setFixedCellHeight(20);
	        list.setFixedCellWidth(200);
	        JScrollPane listScrollPane = new JScrollPane(list);
	        listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			listScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	        listScrollPane.setPreferredSize(new Dimension(200,200));
	        list.setSelectedIndex(0);
	     
			JButton addButton = new JButton("+");
	        AddListener addListener = new AddListener(addButton);
	        addButton.setActionCommand("add");
	        addButton.addActionListener(addListener);
	 
	        removeButton = new JButton("-");
	        removeButton.setActionCommand("remove");
	        removeButton.addActionListener(new RemoveListener());
	        if (servers.size()==0)
	        	removeButton.setEnabled(false);
	        
	        JPanel buttonPane=new JPanel();
	        buttonPane.setLayout(new BorderLayout());
	        buttonPane.add(addButton,BorderLayout.WEST);
	        buttonPane.add(Box.createHorizontalStrut(5));
//	        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
	        buttonPane.add(Box.createHorizontalStrut(5));
	        buttonPane.add(removeButton,BorderLayout.EAST);
	        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	 
	        add(listScrollPane, BorderLayout.CENTER);
	        add(buttonPane, BorderLayout.SOUTH);
			
		}
		
		public int Initialize(){
			//System.out.println("Initial");
			File file=new File("ServerInformation.txt");
			if(file.exists()==true){
				try {
					FileInputStream in=new FileInputStream(file);
					byte[] buf=new byte[10240];
					int length=in.read(buf);
					if(length==-1){
						return 0;
					}
					String content=new String(buf,0,length);
					String []serverEntity=content.split("\n");
					//System.out.println(serverEntity.length);
					int index=listModel.getSize();
					for(int i=0;i<serverEntity.length;i++){
						//System.out.println(serverEntity[i]);/*
						if(currentServer.status==true){
							startButton.setText("Close");
							startButton.setActionCommand("Close");
						}
						String []server=serverEntity[i].split("\\|");
						//System.out.println(server.length);
						MainServer tempServer=new MainServer();
						tempServer.name=server[0];
						tempServer.ip=server[1];
						tempServer.port=Integer.parseInt(server[2]);
						tempServer.currentNumber=Integer.parseInt(server[3]);
						int t=Integer.parseInt(server[4]);
						for(int j=0;j<t;j++){
							QueueElement element=new QueueElement(Integer.parseInt(server[5+j*2]),server[j*2+6]);
							tempServer.queue.add(element);
						}
						servers.add(tempServer);
						listModel.insertElementAt(server[0],index);
						index++;
					}
					//System.out.println(serverEntity[1]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else
				return 0;
			return -1;
		}
		public class AddListener implements ActionListener{
			private JButton button;
			public AddListener(JButton addButton) {
				this.button=addButton;
			}
           
			public void actionPerformed(ActionEvent e){
				int index=listModel.getSize();
				removeButton.setEnabled(true);
				listModel.insertElementAt("Server"+(index+1),index);
				//System.out.println("2add");
				MainServer addServer=new MainServer();
				nameField.setText("Server"+(index+1));
				addServer.name=nameField.getText();
				addServer.ip="0.0.0.0";
				addServer.port=-1;
				servers.add(addServer);
				list.setModel(listModel);
				
				list.setSelectedIndex(index);
				list.ensureIndexIsVisible(index);
				
			}
		}
		
		
		public class RemoveListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				int index=list.getSelectedIndex();
				listModel.remove(index);
				servers.remove(index);
				//System.out.println("2remove");
				int size=listModel.getSize();
				if(index==listModel.getSize()){
						index--;
				}
				if(index!=-1)
					list.setSelectedIndex(index);
				else
					currentIndex=-1;
				rwlock.readLock().lock();
				//System.out.println("List model size"+listModel.getSize());
				if(listModel.getSize()!=0){
					nameField.setText(currentServer.name);
					ipField.setText(currentServer.ip);
					portField.setText(Integer.toString(currentServer.port));
				}
				else {
					removeButton.setEnabled(false);
					nameField.setText("");
					ipField.setText("");
					portField.setText("");
					nameField.setEditable(false);
					ipField.setEditable(false);
					portField.setEditable(false);
				}
				rwlock.readLock().unlock();
				list.ensureIndexIsVisible(index);
			}
		}
		@Override
		public void valueChanged(ListSelectionEvent e) {
			// TODO Auto-generated method stub
			if(e.getValueIsAdjusting()==false){
				//System.out.println("2222");
				if(list.getSelectedIndex()==-1){}
				else{
					icon=new ImageIcon("");
					iconButton.setIcon(icon);
					int index=list.getSelectedIndex();
					//System.out.println(index);
					if(index != -1 && servers.size()>index){
						rwlock.writeLock().lock();
						currentServer=servers.get(index);
						rwlock.writeLock().unlock();
						currentIndex=index;
						//System.out.println(currentServer.name);
						nameField.setText(currentServer.name);
						ipField.setText(currentServer.ip);
						portField.setText(Integer.toString(currentServer.port));
						nameField.setEditable(true);
						ipField.setEditable(true);
						portField.setEditable(true);
						if(currentServer.status==false){
							startButton.setText("Start");
							startButton.setActionCommand("start");
						}else{
							startButton.setText("Close");
							startButton.setActionCommand("close");
						}
					}
				}
			}
			
		}
		
	}
	
}
