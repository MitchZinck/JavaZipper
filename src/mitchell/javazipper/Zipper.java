package mitchell.javazipper;

import java.awt.FlowLayout;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * 
 * @author Mitchell Zicnk <mitchellzinck@yahoo.com>
 *
 */
public class Zipper extends JFrame {
	
	/**
	 * The JFrame for this program.
	 */
	final JFrame frame = new JFrame();
	
	/**
	 * Zipper constructor.
	 */
	public Zipper() {
		super("Cache maker.");
		setLayout(new FlowLayout());
	}
	
	/**
	 * The main function.
	 * @param args
	 */
	public static void main(String[] args) {		
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
		zipper.setSize(500, 700);
		zipper.setVisible(true);
		zipper.setResizable(false);
		

		
		
		System.out.println("Do you want to compress or decompress? (C/D)");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			String str = br.readLine();
			if(str.equalsIgnoreCase(("C"))) {
				System.out.println("Please type in the directory that contains your files... \n(EX: C:/Users/Mitchell/Desktop/raw/");
				String directoryRead = br.readLine();
				System.out.println("Please type in the directory that you want to output the idx file to... \n(EX: C:/Users/Mitchell/Desktop/cache/");
				String directoryWrite = br.readLine();
				System.out.println("Please type in the .idx filename for this cache file 'INCLUDE THE EXTENSION'... \n(EX: main_cache_file.idx6");
				String filename = br.readLine();
				write(directoryRead, directoryWrite, filename);
			} else if(str.equalsIgnoreCase("D")) {
				System.out.println("Please type in the path to the .idx file you want to decompress... 'INCLUDE EXTENSION'\n(EX: C:/Users/Mitchell/Desktop/cache/urmom.idx6");
				String filePath = br.readLine();
				System.out.println("Please type in the directory that you want to output the raw files to... \n(EX: C:/Users/Mitchell/Desktop/raw/");
				String directoryWrite = br.readLine();
				System.out.println("Please type in the extension you want all the raw files to be... \n(EX: .png");
				String extension = br.readLine();
				read(filePath, directoryWrite, extension);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	private static void read(String filePath, String directoryWrite, String extension) {
		filePath.replace("\\", "/");
		directoryWrite.replace("\\", "/");

		if(!directoryWrite.substring(directoryWrite.length() - 1).equals("/")) {
			directoryWrite += "/";
		}
		
		byte[] buffer = new byte[1024];		
		
		try {
			File folder = new File(directoryWrite);
			
			if (!folder.exists()) {
				folder.mkdir();
			}

			ZipInputStream zis = new ZipInputStream(new FileInputStream(filePath));
			ZipEntry ze = zis.getNextEntry();
			
			while (ze != null) {
				System.out.println("Successful decompress of " + ze.getName());
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
				if(child.getName().contains(filename) || directoryWrite.contains(child.getName())) {
					continue;
				}
				System.out.println("Successful compress of " + child.getName());
				ZipEntry ze = new ZipEntry(child.getName().substring(0, child.getName().length() - 4) + ".dat");
				ze.setMethod(ZipEntry.DEFLATED);
				zos.putNextEntry(ze);
				bis = new BufferedInputStream(new FileInputStream(child));
				while ((bytesRead = bis.read(buffer)) != -1) {
					zos.write(buffer, 0, bytesRead);
				}
			}
			
			zos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
