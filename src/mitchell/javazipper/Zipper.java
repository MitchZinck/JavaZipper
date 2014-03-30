package mitchell.javazipper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * 
 * @author Mitchell Zinck <mitchellzinck@yahoo.com>
 *
 */
public class Zipper extends JFrame {

	/**
	 * The JFrame for this program.
	 */
	final JFrame frame = new JFrame();
	private static ArrayList<String> listOfItems = new ArrayList<String>();
	private JButton compress, decompress, finish, addAll;
	private File folder_or_file;
	private DefaultListModel<String> itemListModel, finalListModel;
	private static String recentOutput;
	private ButtonListener bl;
	private JLabel itemListLabel, finalListLabel;
	private JList itemList, finalList;
	private JScrollPane itemListPane, finalListPane;
	private ListSelectionModel listSelectionModel;
	private boolean alreadyDone = false, compressing;
	
	/**
	 * Zipper constructor.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Zipper() {
		super("Cache maker");
		
		GroupLayout layout = new GroupLayout(getContentPane());
		this.getContentPane().setLayout(layout);
		layout.setAutoCreateGaps(true);
	    layout.setAutoCreateContainerGaps(true);
		
		compress = new JButton("Compress");
		decompress = new JButton("Decompress");
		addAll = new JButton("Add all");
		finish = new JButton("Finish");
		
		itemListLabel = new JLabel("Raw folder/Cache Files");
		finalListLabel = new JLabel("Files to compress/decompress");
		
		bl = new ButtonListener();
		compress.addActionListener(bl);
		decompress.addActionListener(bl);
		finish.addActionListener(bl);
		addAll.addActionListener(bl);
		
		itemListModel = new DefaultListModel();
		finalListModel = new DefaultListModel();
		
		itemList = new JList();
		itemList.setModel(itemListModel);
		itemListPane = new JScrollPane(itemList);
		itemListPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		finalList = new JList();
		finalList.setModel(finalListModel);
		finalListPane = new JScrollPane(finalList);
		finalListPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//listPanel.setVisible(false);
		
		listSelectionModel = itemList.getSelectionModel();
        listSelectionModel.addListSelectionListener(new ItemListHandler());
        listSelectionModel = finalList.getSelectionModel();
        listSelectionModel.addListSelectionListener(new FinalListHandler());
        
        layout.setHorizontalGroup(layout.createSequentialGroup()
        		.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        			.addComponent(compress)
        			.addComponent(decompress))
        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	    	.addComponent(itemListLabel)
        	        .addComponent(itemListPane))
    	        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
    	        	.addComponent(finalListLabel)
    	        	.addComponent(finalListPane))
        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	        .addComponent(finish)
        	        .addComponent(addAll))
        	);
        
        layout.linkSize(SwingConstants.HORIZONTAL, compress, decompress);
        
        layout.setVerticalGroup(layout.createSequentialGroup()
        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        	        .addComponent(compress)
        	        .addComponent(itemListLabel)
        	        .addComponent(finalListLabel)
        	        .addComponent(finish))
        	    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        	    	.addComponent(decompress)
        	        .addComponent(itemListPane)
        	        .addComponent(finalListPane)
        	        .addComponent(addAll))
        	);
		
		this.pack();
	}
	
	/**
	 * The main function.
	 * @param args
	 */
	public static void main(String[] args) {		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					Properties prop = new Properties();
					OutputStream output = new FileOutputStream(System.getProperty("user.home") 
							+ System.getProperty("file.separator") + "zipperconfig.properties");
					
					prop.setProperty("recentOutput", recentOutput);			 
					prop.store(output, null);					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
		});
		
		InputStream input = null;
		try {
			Properties prop = new Properties();
			input = new FileInputStream(System.getProperty("user.home") 
					+ System.getProperty("file.separator") + "zipperconfig.properties");			
			
			prop.load(input);			
			recentOutput = prop.getProperty("recentOutput");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Zipper zipper = new Zipper();
		zipper.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		zipper.setSize(500, 500);
		zipper.setVisible(true);
		zipper.setResizable(false);
	}
	
	/**
	 * Lists all files in a folder
	 * @param folder
	 * 		Folder to list files from
	 */
	public void listFilesForFolder(final File folder) {
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile()) {
		        itemListModel.addElement(file.getName());
		    }
		}
	}
	
	/**
	 * The button Listener class
	 * 
	 * @author Mitchell
	 *
	 */
	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent action) {
			if(action.getActionCommand().equals("Decompress")) {
				JFileChooser fileChooser = new JFileChooser();				
				int returnValue = fileChooser.showOpenDialog(Zipper.this);
				
				if(returnValue == JFileChooser.APPROVE_OPTION) {
					folder_or_file = fileChooser.getSelectedFile();
					ArrayList<String> names = read(folder_or_file.getPath(), null, null, false);
					
					finalListModel.removeAllElements();
					itemListModel.removeAllElements();
					for(String s : names) {
						itemListModel.addElement(s);
					}
				}
				compressing = false;
			} else if(action.getActionCommand().equals("Finish")) {
				if(finalListModel.getSize() == 0) {
					JOptionPane.showConfirmDialog(null, "Please add stuff to the files to compress/decompress list.", 
							"Error",  JOptionPane.OK_OPTION);
					return;
				}
				
				JTextField fileName_orExtension = new JTextField(5);
			    JTextField writePath = new JTextField(recentOutput, 50);
	
			    JPanel optionPanel = new JPanel();
			    optionPanel.add(new JLabel("Extension:"));
			    optionPanel.add(fileName_orExtension);
			    optionPanel.add(Box.createHorizontalStrut(15)); // a spacer
			    optionPanel.add(new JLabel("Write Path: "));
			    optionPanel.add(writePath);
			    
			    int result;
			    if(compressing == false) {
			    	result = JOptionPane.showConfirmDialog(null, optionPanel, "Please Enter Raw File Extensions (Ex: .png, .idx) "
							+ "and Write Path (Ex: C:/Desktop/rawfiles)", JOptionPane.OK_CANCEL_OPTION);
					if(result == JOptionPane.OK_OPTION) {
						recentOutput = writePath.getText();
						for(Object s : finalListModel.toArray()) {
							String z = (String) s;
							listOfItems.add(z);
						}
						read(folder_or_file.getPath(), writePath.getText(), fileName_orExtension.getText(), true);
					}
			    } else {
			    	result = JOptionPane.showConfirmDialog(null, optionPanel, "Please Enter Compressed File Name (Ex: main_file_cache.idx6) "
							+ "and Write Path (Ex: C:/Desktop/cache)", JOptionPane.OK_CANCEL_OPTION);
					if(result == JOptionPane.OK_OPTION) {
						recentOutput = writePath.getText();
						for(Object z : finalListModel.toArray()) {
							String s = (String) z;
							listOfItems.add(s);
						}
						write(folder_or_file.toString(), writePath.getText(), fileName_orExtension.getText());
					}
			    }
							
				listOfItems.clear();
			} else if(action.getActionCommand().equals("Compress")) {
				JFileChooser fileChooser = new JFileChooser();				
				fileChooser.setDialogTitle("File Chooser");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnValue = fileChooser.showOpenDialog(Zipper.this);
				
				if(returnValue == JFileChooser.APPROVE_OPTION) {
					folder_or_file = fileChooser.getSelectedFile();
					listFilesForFolder(folder_or_file);
				}
				compressing = true;
			} else if(action.getActionCommand().equals("Add all")) {
				for(Object s : itemListModel.toArray()) {
					String z = (String) s;
					finalListModel.addElement(z);
					itemListModel.removeElement(s);
				}
			}
		}		
	}
	
	/**
	 * Read a .idx file from the filePath and write the raw contents to the directoryWrite.
	 * 
	 * @param filePath
	 * 			The .idx path.
	 * @param directoryWrite
	 * 			The directory to write the uncompressed raw files.
	 * @param extension
	 * 			The raw files extensions.
	 */
	private static ArrayList<String> read(String filePath, String directoryWrite, String extension, boolean write) {
		filePath.replace("\\", "/");
		
		if(write == true) {
			directoryWrite.replace("\\", "/");
	
			if(!directoryWrite.substring(directoryWrite.length() - 1).equals("/")) {
				directoryWrite += "/";
			}
		}
		
		byte[] buffer = new byte[1024];		
		ArrayList<String> fileNames = new ArrayList<String>();
		
		try {
			File folder = null;
			if(write == true) {
				folder = new File(directoryWrite);				
				
				if (!folder.exists()) {
					folder.mkdir();
				}
			}
			
			Path path = Paths.get(filePath);
			byte[] zipfile = Files.readAllBytes(path);

			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(decode(zipfile)));
			ZipEntry ze = zis.getNextEntry();
			
			while (ze != null) {
				if(write == false) {
					fileNames.add(ze.getName());
					ze = zis.getNextEntry();
					continue;
				}
				
				boolean cont = false;
				for(String s : listOfItems) {
					if(s.equals(ze.getName())) {
						cont = true;
						listOfItems.remove(s);
						break;
					}
				}
				
				if(cont == false) {
					ze = zis.getNextEntry();
					continue;
				}
				
				String fileName = ze.getName().substring(0,ze.getName().length() - 4) + extension;
				File newFile = new File(folder + File.separator + fileName);
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		if(write == false) {
			return fileNames;
		} else {
			return null;
		}
	}
	
	/**
	 * Writes all of the raw files contained in the directory to read from, and writes a zipfile with
	 * the extension of there choice to the specified directory (I use .idx(number here)).
	 * 
	 * @param directoryRead
	 * 			The directory to read the raw files from.
	 * @param directoryWrite
	 * 			The directory to write the zipfile to.
	 * @param filename
	 * 			The filename with and extension that will be the zipfile. Ex; hiddenfile.idx8.
	 */
	public static void write(String directoryRead, String directoryWrite, String filename) {
		directoryRead.replace("\\", "/");
		directoryWrite.replace("\\", "/");
		
		if(!directoryRead.substring(directoryRead.length() - 1).equals("/")) {
			directoryRead += "/";
		}
		if(!directoryWrite.substring(directoryWrite.length() - 1).equals("/")) {
			directoryWrite += "/";
		}
		
		File dir = new File(directoryRead);
		ZipOutputStream zos;
		BufferedInputStream bis;
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		
		try {
			zos = new ZipOutputStream(new FileOutputStream(directoryWrite + filename));
			
			for(File child : dir.listFiles()) {
				if(child.getName().contains(filename) || directoryWrite.contains(child.getName()) || !listOfItems.contains(child.getName())) {
					continue;
				}
				ZipEntry ze = new ZipEntry(child.getName().substring(0, child.getName().length() - 4) + ".dat");
				ze.setMethod(ZipEntry.DEFLATED);
				zos.putNextEntry(ze);
				bis = new BufferedInputStream(new FileInputStream(child));
				while ((bytesRead = bis.read(buffer)) != -1) {
					zos.write(buffer, 0, bytesRead);
				}
			}
			
			zos.close();
			
			Path path = Paths.get(directoryWrite + filename);
			byte[] zipfile = Files.readAllBytes(path);
			
			FileOutputStream fos = new FileOutputStream(directoryWrite + filename);
			fos.write(encode(zipfile));
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/**
	 * First list handler....
	 * @author Mitchell
	 *
	 */
	public class ItemListHandler implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
	        ListSelectionModel lsm = (ListSelectionModel)e.getSource();

	        if (lsm.isSelectionEmpty()) {
	            return;
	        } else if(alreadyDone == false) {
	            int minIndex = lsm.getMinSelectionIndex();
	            int maxIndex = lsm.getMaxSelectionIndex();
	            for (int i = minIndex; i <= maxIndex; i++) {
	                if (lsm.isSelectedIndex(i)) {
	                    finalListModel.addElement(itemListModel.get(i));
	                    itemListModel.remove(i);
	                    alreadyDone = true;
	                }
	            }
	        } else {
	        	alreadyDone = false;
	        }
	    }
	}
	
	/**
	 * Second list handler....
	 * @author Mitchell
	 *
	 */
	public class FinalListHandler implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
	        ListSelectionModel lsm = (ListSelectionModel)e.getSource();

	        if (lsm.isSelectionEmpty()) {
	            return;
	        } else if(alreadyDone == false) {
	            int minIndex = lsm.getMinSelectionIndex();
	            int maxIndex = lsm.getMaxSelectionIndex();
	            for (int i = minIndex; i <= maxIndex; i++) {
	                if (lsm.isSelectedIndex(i)) {
	                    itemListModel.addElement(finalListModel.get(i));
	                    finalListModel.remove(i);
	                    alreadyDone = true;
	                }
	            }
	        } else {
	        	alreadyDone = false;
	        }
	    }
	}
	

	/**
	 * Method to decode the zip file.
	 * @param byteToEncode
	 * 			The encoded zipfile to a byte array.
	 * @return
	 * 			Decoded zipfile byte array.
	 */
	public static byte[] decode(byte[] decode) {
		String passphrase = "A8ch2!d7vhaw";
		byte[] salt = "Ahsgf!&%gaDgs47$SDhisa".getBytes();
		int iterations = 10000;
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			SecretKey tmp = factory.generateSecret(new PBEKeySpec(passphrase.toCharArray(), salt, iterations, 128));
			SecretKeySpec key = new SecretKeySpec(tmp.getEncoded(), "AES");			
			
			Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");

			aes.init(Cipher.DECRYPT_MODE, key);
			return aes.doFinal(decode);
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Method to encode the zip file.
	 * @param byteToEncode
	 * 			The zipfile to a byte array.
	 * @return
	 * 			Encoded zipfile byte array.
	 */
	public static byte[] encode(byte[] byteToEncode) {
		String passphrase = "A8ch2!d7vhaw";
		byte[] salt = "Ahsgf!&%gaDgs47$SDhisa".getBytes();
		int iterations = 10000;
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			SecretKey tmp = factory.generateSecret(new PBEKeySpec(passphrase.toCharArray(), salt, iterations, 128));
			SecretKeySpec key = new SecretKeySpec(tmp.getEncoded(), "AES");			
			
			Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
			aes.init(Cipher.ENCRYPT_MODE, key);
			byte[] cipherText = aes.doFinal(byteToEncode);
			
			return cipherText;
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
	}
}
