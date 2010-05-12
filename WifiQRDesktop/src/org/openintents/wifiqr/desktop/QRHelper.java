package org.openintents.wifiqr.desktop;

import java.awt.image.BufferedImage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.ByteMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRHelper {

	
	public static BufferedImage createTextCode(String text){
		QRCodeWriter qrCodeWriter= new QRCodeWriter();
		
		int qrWidth=128;
		int qrHeight=128;
		ByteMatrix byteMatrix=null;
		try {
			byteMatrix=qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, qrWidth, qrHeight);
		} catch (WriterException e) {
			e.printStackTrace();
		}
		return toBufferedImage(byteMatrix);
	}
	
	public static BufferedImage createURLCode(String url){
		QRCodeWriter qrCodeWriter= new QRCodeWriter();
		
		int qrWidth=128;
		int qrHeight=128;
		ByteMatrix byteMatrix = null;
		try {
			byteMatrix=qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, qrWidth, qrHeight);
		} catch (WriterException e) {
			e.printStackTrace();
		}

		return toBufferedImage(byteMatrix);
	}
	
	public static BufferedImage createDirectDownloadCode(){
		return createURLCode("");
	}
	
	public static BufferedImage createMarketDownloadCode(){
		return createURLCode("");
	}
	
	
	public static BufferedImage toBufferedImage(ByteMatrix matrix) {
	    int width = matrix.getWidth();
	    int height = matrix.getHeight();
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    for (int x = 0; x < width; x++) {
	      for (int y = 0; y < height; y++) {

	        image.setRGB(x, y, matrix.get(x, y) == 0 ? BLACK : WHITE);
	      }
	    }
	    return image;
	}
	private static final int BLACK = 0xFF000000;
	private static final int WHITE = 0xFFFFFFFF;

}
