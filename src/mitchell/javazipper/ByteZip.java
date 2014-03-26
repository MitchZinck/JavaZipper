package mitchell.javazipper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

/**
 * Class dedicated to special methods.
 * @author Mitchell
 *
 */
public class ByteZip {
	
	private static HashMap<String, BufferedImage> imageArray = new HashMap<String, BufferedImage>();
	private static HashMap<String, byte[]> itemDefArray = new HashMap<String, byte[]>();
	
	private static void zipFileManager(String filePath, String purpose) {
		filePath.replace("\\", "/");
		
		byte[] buffer = new byte[1024];		
		int len = 0;
		
		try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(filePath));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			new ByteArrayInputStream(buffer);
			ZipEntry ze = zis.getNextEntry();
			
			while (ze != null) {
				while ((len = zis.read(buffer)) > 0) {
					bos.write(buffer, 0, len);
				}
				
				byte[] b = bos.toByteArray();
				
				if(purpose.equals("IMAGE")) {
					try {
						BufferedImage img = ImageIO.read(new ByteArrayInputStream(b));
						imageArray.put(ze.getName().substring(0,ze.getName().length() - 4) + ".png", img);
					}
					catch (IndexOutOfBoundsException e) {
						System.out.println("ERROR: SOMETHING WENT WRONG WITH READING " + ze.getName());
						bos.flush();
						bos.reset();
						ze = zis.getNextEntry();
						continue;
					}
				} else if(purpose.equals("ITEMDEF")) {
					try {
						itemDefArray.put("ze.getName().substring(0,ze.getName().length() - 4) + .dat", b);
					}
					catch (IndexOutOfBoundsException e) {
						System.out.println("ERROR: SOMETHING WENT WRONG WITH READING " + ze.getName());
						bos.flush();
						bos.reset();
						ze = zis.getNextEntry();
						continue;
					}
				}
				
				bos.flush();
				bos.reset();
				ze = zis.getNextEntry();
			}
			
			bos.close();
			zis.closeEntry();
			zis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
//		int count = 0;
//		for(Image b : imageArray) {
//	        try {
//		        ImageIO.write((RenderedImage) b, "png",new File("C:\\Users\\Mitchell\\Desktop\\cache\\" + count + "picture.png"));
//	        } catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//	        count++;
//		}
	}
}
