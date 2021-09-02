package Utility;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

public class BufferTools {



	public static void putInBuffer(int color, ByteBuffer pixelBuffer) {
		pixelBuffer.put((byte) ((color >> 16) & 0xFF));     // Red component
		pixelBuffer.put((byte) ((color >> 8) & 0xFF));      // Green component
		pixelBuffer.put((byte) (color & 0xFF));               // Blue component
		pixelBuffer.put((byte) ((color >> 24) & 0xFF));    // Alpha component. Only for RGBA
	}
	
	public  static void changePixel(int index,Color c,byte[] pixel){
//		Color c=color.makeColorAlpha();
		byte red=(byte) c.getRed();
		byte green=(byte) c.getGreen();
		byte blue= (byte) c.getBlue();
		byte alpha= (byte) ((byte) c.getAlpha()*0.5f);
		pixel[index]   = red;
		pixel[index+1]   = green;
		pixel[index+2]   = blue;
		pixel[index+3]   = alpha;
//		
//	    int r = pixel[index] & 0xFF;
//	    int g = pixel[index + 1] & 0xFF;
//	    int b = pixel[index + 2] & 0xFF;
//	    int a = pixel[index + 3] & 0xFF;

	}
	
	public static ByteBuffer getByteBuffer(BufferedImage bi){

	    ByteBuffer byteBuffer = null;
	    DataBuffer dataBuffer = bi.getRaster().getDataBuffer();

	    if (dataBuffer instanceof DataBufferByte) {
	    	byte[] pixelData = ((DataBufferByte) dataBuffer).getData();
	    	byteBuffer= ByteBuffer.wrap(pixelData);
	    }
	    return byteBuffer;
	}


	public static byte[] getByteData(BufferedImage theImage) {
	    WritableRaster raster = theImage.getRaster();
	    DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
//	    DataBuffer buffer =  raster.getDataBuffer();
//	    pixelBuffer= (ByteBuffer) buffer;
	    
//	        int[] pixelData = ((DataBufferInt) buffer).getData();
//	        buffer = ByteBuffer.allocate(pixels.length * 4);
//	        buffer.asIntBuffer().put(IntBuffer.wrap(pixelData));

	    
	    return buffer.getData();
	}



}
