package BarCode;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import javax.imageio.ImageIO;

public class BarCode {

    /**
     * @param args the command line arguments
     */
    public void encode(String contents, int width, int height, String imgPath) { 
        try { 
            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents,BarcodeFormat.QR_CODE, width, height); 
            MatrixToImageWriter 
                    .writeToFile(bitMatrix, "png", new File(imgPath)); 
   
        } catch (WriterException | IOException e) { 
            e.printStackTrace(); 
        } 
    } 
   
    public String decode(String imgPath) {  
        try {  
            Reader reader = new MultiFormatReader();
            File file = new File(imgPath);  
            BufferedImage image;  
            try {  
                image = ImageIO.read(file);
                if (image == null) {  
                    System.out.println("Could not decode image");  
                }  
                LuminanceSource source = new BufferedImageLuminanceSource(image);  
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(  
                        source));  
                Result result;  
                Hashtable hints = new Hashtable();  
                hints.put(DecodeHintType.CHARACTER_SET, "GBK");  
                result = new MultiFormatReader().decode(bitmap, hints); 
                String resultStr = result.getText();  
                return resultStr;  
  
            } catch (IOException ioe) {  
                System.out.println(ioe.toString());  
            } catch (ReaderException re) {  
                System.out.println(re.toString());  
            }  
  
        } catch (Exception ex) {  
  
        }  
        return null;
    }  
    
    /*
    public static void main(String[] args) { 
        String imgPath = "/home/lvyuanhao/test.png"; 
        String contents = "I love summer!" ;
        int width = 300, height = 300; 
        BarCode handler = new BarCode(); 
        handler.encode(contents, width, height, imgPath); 
        System.out.println(handler.decode(imgPath)); 
    }
    */
}