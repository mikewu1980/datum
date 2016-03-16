package org.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class ImageUtils {
	/**
	 * 图像平移
	 * 
	 * @param src
	 *            源图像
	 * @param tx
	 *            X轴平移量
	 * @param ty
	 *            Y轴平移量
	 * @param formatName
	 *            图片格式名称（可以为空）
	 * @return
	 */
	public static BufferedImage translation(BufferedImage src, double tx, double ty, String formatName) {
		if (null == src) {
			throw new IllegalArgumentException();
		}

		int width = src.getWidth();
		int height = src.getHeight();
		if (tx >= width) {
			throw new IllegalArgumentException();
		}
		if (ty >= height) {
			throw new IllegalArgumentException();
		}

		AffineTransform transform = AffineTransform.getTranslateInstance(tx, ty);
		AffineTransformOp op = null;

		// 检查格式
		ImageFormat format = ImageFormat.fromName(formatName);
		switch (format) {
			case GIF: {
				op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
				break;
			}
			case PNG: {
				op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
				break;
			}
			default: {
				op = new AffineTransformOp(transform, null);
				break;
			}
		}

		BufferedImage dst = op.filter(src, null);

		return dst;
	}

	/**
	 * 旋转图像
	 * 
	 * @param src
	 *            源图像
	 * @param angle
	 *            旋转角度
	 * @param formatName
	 * @return
	 */
	public static BufferedImage rotate(BufferedImage src, double angle, String formatName) {
		return rotate(src, angle, 0D, 0D, formatName);
	}

	/**
	 * 旋转图像
	 * 
	 * @param src
	 *            源图像
	 * @param angle
	 *            旋转角度
	 * @param x
	 *            旋转锚点的X轴坐标
	 * @param y
	 *            旋转锚点的Y轴坐标
	 * @param formatName
	 * @return
	 */
	public static BufferedImage rotate(BufferedImage src, double angle, double x, double y, String formatName) {
		if (null == src) {
			throw new IllegalArgumentException();
		}

		AffineTransform transform = null;
		// 检查角度是否是90°的倍数，如果是则调用象限数旋转方法
		final double quadrant = 90D;
		double modulus = angle % quadrant;
		if (modulus == 0D) {
			int quotient = (int) ((angle / quadrant) % 4D);
			transform = AffineTransform.getQuadrantRotateInstance(quotient, x, y);
		} else {
			double radian = Math.toRadians(angle);
			transform = AffineTransform.getRotateInstance(radian, x, y);
		}

		AffineTransformOp op = null;
		BufferedImage dst = null;

		// 检查格式
		ImageFormat format = ImageFormat.fromName(formatName);
		switch (format) {
			case GIF: {
				op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
				dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
				break;
			}
			case PNG: {
				op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
				dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
				break;
			}
			default: {
				op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
				dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
				break;
			}
		}

		// BufferedImage dst = op.filter(src, null);

		Graphics2D g2d = dst.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		// g2d.drawImage(temp, x, y, null);

		// 透明度
		// g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1F));
		g2d.drawImage(src, op, 0, 0);

		// g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

		g2d.dispose();

		return dst;
	}

	/**
	 * 图像缩放
	 * 
	 * @param src
	 *            源图像
	 * @param sx
	 *            X轴缩放比例
	 * @param sy
	 *            Y轴缩放比例
	 * @param formatName
	 *            图片格式名称（可以为空）
	 * @return
	 */
	public static BufferedImage zoom(BufferedImage src, double sx, double sy, String formatName) {
		if (null == src) {
			throw new IllegalArgumentException();
		}
		if (sx <= 0) {
			throw new IllegalArgumentException();
		}
		if (sy <= 0) {
			throw new IllegalArgumentException();
		}

		AffineTransform transform = AffineTransform.getScaleInstance(sx, sy);
		AffineTransformOp op = null;

		// 检查格式
		ImageFormat format = ImageFormat.fromName(formatName);
		switch (format) {
			case GIF: {
				op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
				break;
			}
			case PNG: {
				op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
				break;
			}
			default: {
				op = new AffineTransformOp(transform, null);
				break;
			}
		}
		BufferedImage dst = op.filter(src, null);

		return dst;
	}

	public static BufferedImage zoom(BufferedImage src, double x, double y) {
		if (null == src) {
			throw new IllegalArgumentException();
		}
		if (x <= 0) {
			throw new IllegalArgumentException();
		}
		if (y <= 0) {
			throw new IllegalArgumentException();
		}

		AffineTransform transform = AffineTransform.getScaleInstance(x, y);
		AffineTransformOp op = null;

		// // 检查格式
		// ImageFormat format = ImageFormat.fromName(formatName);
		// switch (format) {
		// case GIF: {
		// op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		// break;
		// }
		// case PNG: {
		// op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		// break;
		// }
		// default: {
		op = new AffineTransformOp(transform, null);
		// break;
		// }
		// }
		BufferedImage dst = op.filter(src, null);

		return dst;
	}

	/**
	 * 从源图像中截取一个矩形图像
	 * 
	 * @param src
	 *            源图像
	 * @param x
	 *            截取图像左上角的X轴坐标
	 * @param y
	 *            截取图像左上角的Y轴坐标
	 * @param width
	 *            截取图像的宽
	 * @param height
	 *            截取图像的高
	 * @return
	 */
	public static BufferedImage cutImageByRectangle(final byte[] src, int x, int y, int width, int height) {
		if (null == src) {
			throw new IllegalArgumentException();
		}
		if (width <= 0) {
			throw new IllegalArgumentException();
		}
		if (height <= 0) {
			throw new IllegalArgumentException();
		}

		ImageInputStream iis = null;
		ImageReader reader = null;
		ByteArrayInputStream input = new ByteArrayInputStream(src);

		try {
			iis = ImageIO.createImageInputStream(input);

			Iterator<ImageReader> imageReader = ImageIO.getImageReaders(iis);
			if (imageReader.hasNext()) {
				reader = imageReader.next();
				// String formatName = reader.getFormatName();
				reader.setInput(iis, true);

				ImageReadParam param = reader.getDefaultReadParam();
				Rectangle rect = new Rectangle(x, y, width, height);
				param.setSourceRegion(rect);
				BufferedImage image = reader.read(0, param);
				return image;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.dispose();
			}
			if (iis != null) {
				try {
					iis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 向一个图像附加另一个图像
	 * 
	 * @param background
	 *            背景（被附加的图像）
	 * @param foreground
	 *            前景（附加的图像）
	 * @param x
	 *            前景位于背景的X轴坐标
	 * @param y
	 *            前景位于背景的X轴坐标
	 * @param width
	 *            前景宽度
	 * @param height
	 *            前景高度
	 * @param alpha
	 *            透明度（值为0到1之间的浮点数，且包括0和1，0-完全透明，1-完全不透明）
	 * @return
	 */
	public static BufferedImage attachImage(Image background, Image foreground, int x, int y, int width, int height,
			float alpha) {
		if (null == background) {
			throw new IllegalArgumentException();
		}
		if (null == foreground) {
			throw new IllegalArgumentException();
		}
		if (alpha < 0 || alpha > 1) {
			throw new IllegalArgumentException();
		}

		try {
			int w = background.getWidth(null);
			int h = background.getHeight(null);
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

			Graphics2D g = image.createGraphics();
			// 设置对线段的锯齿状边缘处理
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			// Image temp = background.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			g.drawImage(background, 0, 0, null);

			// 透明度
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));

			g.drawImage(foreground, x, y, width, height, null);

			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

			g.dispose();

			return image;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 向一个图像附加另一个图像
	 * 
	 * @param background
	 *            背景（被附加的图像）
	 * @param foreground
	 *            前景（附加的图像）
	 * @param x
	 *            前景位于背景的X轴坐标
	 * @param y
	 *            前景位于背景的X轴坐标
	 * @param alpha
	 *            透明度（值为0到1之间的浮点数，且包括0和1，0-完全透明，1-完全不透明）
	 * @return
	 */
	public static BufferedImage attachImage(Image background, Image foreground, int x, int y, float alpha) {
		if (null == background) {
			throw new IllegalArgumentException();
		}
		if (null == foreground) {
			throw new IllegalArgumentException();
		}
		if (alpha < 0 || alpha > 1) {
			throw new IllegalArgumentException();
		}

		int width = foreground.getWidth(null);
		int height = foreground.getHeight(null);

		BufferedImage image = ImageUtils.attachImage(background, foreground, x, y, width, height, alpha);
		return image;
	}

	/**
	 * 向一个图像附加另一个图像
	 * 
	 * @param background
	 *            背景（被附加的图像）
	 * @param foreground
	 *            前景（附加的图像）
	 * @param x
	 *            前景位于背景的X轴坐标
	 * @param y
	 *            前景位于背景的X轴坐标
	 * @return
	 */
	public static BufferedImage attachImage(Image background, Image foreground, int x, int y) {
		if (null == background) {
			throw new IllegalArgumentException();
		}
		if (null == foreground) {
			throw new IllegalArgumentException();
		}

		int width = foreground.getWidth(null);
		int height = foreground.getHeight(null);

		BufferedImage image = ImageUtils.attachImage(background, foreground, x, y, width, height, 1F);
		return image;
	}

	/**
	 * 字节数组转输出流
	 * 
	 * @param input
	 * @return
	 */
	public static ByteArrayOutputStream convert2OutputStream(final byte[] input) {
		if (null == input || input.length == 0) {
			throw new IllegalArgumentException();
		}

		ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
		return ImageUtils.convert2OutputStream(inputStream);
	}

	/**
	 * 输入流转输出流
	 * 
	 * @param input
	 * @return
	 */
	public static ByteArrayOutputStream convert2OutputStream(InputStream input) {
		if (null == input) {
			throw new IllegalArgumentException();
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		try {
			while ((len = input.read(buffer)) > 0) {
				output.write(buffer, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return output;
	}

	/**
	 * 图像转输出流
	 * 
	 * @param input
	 * @param formatName
	 *            图像格式名称，如：png、jpg、bmp、gif
	 * @return
	 */
	public static ByteArrayOutputStream convert2OutputStream(BufferedImage input, String formatName) {
		if (null == input) {
			throw new IllegalArgumentException();
		}
		if (StringUtils.isBlank(formatName)) {
			throw new IllegalArgumentException();
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageIO.write(input, formatName, output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return output;
	}

	/**
	 * 文件转字节数组
	 * 
	 * @param file
	 * @return
	 */
	public static byte[] convert2ByteArray(File file) {
		if (isIllegal(file)) {
			throw new IllegalArgumentException();
		}

		FileChannel channel = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			channel = fis.getChannel();
			ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
			while ((channel.read(byteBuffer)) > 0) {
				// do nothing
			}
			byte[] bytes = byteBuffer.array();

			return bytes;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 输出流转文件
	 * 
	 * @param stream
	 * @param path
	 *            文件所在目录路径
	 * @param fileName
	 *            文件名
	 * @return
	 */
	public static File convert2File(ByteArrayOutputStream stream, String path, String fileName) {
		if (null == stream) {
			throw new IllegalArgumentException();
		}
		if (StringUtils.isBlank(path)) {
			throw new IllegalArgumentException();
		}
		if (StringUtils.isBlank(fileName)) {
			throw new IllegalArgumentException();
		}

		try {
			// 检查目录
			File dir = new File(path);
			// if (dir.canWrite() == false) {
			// throw new IllegalAccessError(path);
			// }
			if (dir.exists() == false) {
				dir.mkdirs();
			}

			// 文件路径
			String filePath = dir.getAbsolutePath() + File.separator + fileName;
			File file = new File(filePath);

			FileOutputStream fos = new FileOutputStream(file);
			stream.writeTo(fos);
			stream.flush();

			return file;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 字节数组转文件
	 * 
	 * @param bytes
	 * @param path
	 *            文件所在目录路径
	 * @param fileName
	 *            文件名
	 * @return
	 */
	public static File convert2File(byte[] bytes, String path, String fileName) {
		if (null == bytes || bytes.length == 0) {
			throw new IllegalArgumentException();
		}
		if (StringUtils.isBlank(path)) {
			throw new IllegalArgumentException();
		}
		if (StringUtils.isBlank(fileName)) {
			throw new IllegalArgumentException();
		}

		ByteArrayOutputStream stream = ImageUtils.convert2OutputStream(bytes);
		File file = ImageUtils.convert2File(stream, path, fileName);

		return file;
	}

	/**
	 * 图像转文件
	 * 
	 * @param input
	 * @param formatName
	 *            图像格式名称，如：png、jpg、bmp、gif
	 * @param path
	 *            文件所在目录路径
	 * @param fileName
	 *            文件名
	 * @return
	 */
	public static File convert2File(BufferedImage input, String formatName, String path, String fileName) {
		if (null == input) {
			throw new IllegalArgumentException();
		}
		if (StringUtils.isBlank(formatName)) {
			throw new IllegalArgumentException();
		}
		if (StringUtils.isBlank(path)) {
			throw new IllegalArgumentException();
		}
		if (StringUtils.isBlank(fileName)) {
			throw new IllegalArgumentException();
		}

		ByteArrayOutputStream stream = ImageUtils.convert2OutputStream(input, formatName);
		File file = ImageUtils.convert2File(stream, path, fileName);
		return file;
	}

	/**
	 * 字符串转二进制矩阵
	 * 
	 * @param content
	 * @param width
	 *            矩阵宽度
	 * @param height
	 *            矩阵高度
	 * @return
	 */
	public static BitMatrix convert2BitMatrix(String content, int width, int height) {
		try {
			HashMap<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			MultiFormatWriter writer = new MultiFormatWriter();
			BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
			//matrix = deleteWhite(matrix);
			return matrix;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * 二维码去白边
	 * @param matrix
	 * @return
	 */
	public static BitMatrix deleteWhite(BitMatrix matrix){  
	    int[] rec = matrix.getEnclosingRectangle();  
	    int resWidth = rec[2] + 1;  
	    int resHeight = rec[3] + 1;  
	  
	    BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);  
	    resMatrix.clear();  
	    for (int i = 0; i < resWidth; i++) {  
	        for (int j = 0; j < resHeight; j++) {  
	            if (matrix.get(i + rec[0], j + rec[1]))  
	                resMatrix.set(i, j);  
	        }  
	    }  
	    return resMatrix;  
	}  
	/**
	 * 二进制矩阵转二维码图像
	 * 
	 * @param matrix
	 * @return
	 */
	public static BufferedImage convert2BufferedImage(BitMatrix matrix) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int c = matrix.get(x, y) == true ? Color.BLACK.getRGB() : Color.WHITE.getRGB();
				image.setRGB(x, y, c);
			}
		}

		return image;
	}

	/**
	 * 是否为非法文件
	 * 
	 * @param file
	 * @return
	 */
	private static boolean isIllegal(File file) {
		if (null == file) {
			return true;
		}
		if (file.exists() == false) {
			return true;
		}
		if (file.isFile() == false) {
			return true;
		}
		if (file.canRead() == false) {
			return true;
		}

		return false;
	}

	/**
	 * 获取图片格式名称
	 * 
	 * @param file
	 * @return
	 */
	public static String getFormatName(File input) {
		if (isIllegal(input)) {
			throw new IllegalArgumentException();
		}

		String formatName = null;

		try {
			ImageInputStream iis = ImageIO.createImageInputStream(input);
			formatName = ImageUtils.getFormatName(iis);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return formatName;
	}

	/**
	 * 获取图片格式名称
	 *
	 * @param input
	 * @return
	 */
	public static String getFormatName(final byte[] input) {
		if (null == input || input.length == 0) {
			throw new IllegalArgumentException();
		}

		String formatName = null;

		try {
			ByteArrayInputStream in = new ByteArrayInputStream(input);
			ImageInputStream iis = ImageIO.createImageInputStream(in);

			formatName = ImageUtils.getFormatName(iis);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return formatName;
	}

	/**
	 * 获取图片格式名称
	 *
	 * @param input
	 * @return
	 */
	private static String getFormatName(ImageInputStream input) {
		if (null == input) {
			throw new IllegalArgumentException();
		}

		String formatName = null;

		try {
			Iterator<ImageReader> imageReader = ImageIO.getImageReaders(input);
			if (imageReader.hasNext()) {
				ImageReader reader = imageReader.next();
				formatName = reader.getFormatName();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (StringUtils.isNotBlank(formatName)) {
			formatName = formatName.toLowerCase();
		}

		return formatName;
	}

	/**
	 * 根据高度等比例压缩图片
	 * 
	 * @param source
	 * @param height
	 * @return
	 */
	public static ByteArrayOutputStream compressWithHeight(File source, int height) {
		if (isIllegal(source)) {
			throw new IllegalArgumentException();
		}
		if (height <= 0) {
			throw new IllegalArgumentException();
		}

		byte[] bytes = ImageUtils.convert2ByteArray(source);

		return ImageUtils.compressWithHeight(bytes, height);
	}

	/**
	 * 根据宽度等比例压缩图片
	 * 
	 * @param source
	 * @param width
	 * @return
	 */
	public static ByteArrayOutputStream compressWithWidth(File source, int width) {
		if (isIllegal(source)) {
			throw new IllegalArgumentException();
		}
		if (width <= 0) {
			throw new IllegalArgumentException();
		}

		byte[] bytes = ImageUtils.convert2ByteArray(source);

		return ImageUtils.compressWithWidth(bytes, width);
	}

	/**
	 * 根据高度等比例压缩图片
	 * 
	 * @param source
	 * @param height
	 * @return
	 */
	public static ByteArrayOutputStream compressWithHeight(final byte[] source, int height) {
		if (null == source || source.length == 0) {
			throw new IllegalArgumentException();
		}
		if (height <= 0) {
			throw new IllegalArgumentException();
		}

		try {
			String formatName = ImageUtils.getFormatName(source);

			ByteArrayInputStream input = new ByteArrayInputStream(source);
			BufferedImage src = ImageIO.read(input);
			BufferedImage dst = ImageUtils.compressWithHeight(src, height, formatName);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(dst, formatName, output);
			return output;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 根据宽度等比例压缩图片
	 * 
	 * @param source
	 * @param width
	 * @return
	 */
	public static ByteArrayOutputStream compressWithWidth(final byte[] source, int width) {
		if (null == source || source.length == 0) {
			throw new IllegalArgumentException();
		}
		if (width <= 0) {
			throw new IllegalArgumentException();
		}

		try {
			String formatName = ImageUtils.getFormatName(source);

			ByteArrayInputStream input = new ByteArrayInputStream(source);
			BufferedImage src = ImageIO.read(input);
			BufferedImage dst = ImageUtils.compressWithWidth(src, width, formatName);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(dst, formatName, output);
			return output;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 根据高度等比例压缩图片
	 * 
	 * @param src
	 * @param height
	 * @param formatName
	 * @return
	 */
	public static BufferedImage compressWithHeight(BufferedImage src, int height, String formatName) {
		if (null == src) {
			throw new IllegalArgumentException();
		}
		if (height <= 0) {
			throw new IllegalArgumentException();
		}

		int h = src.getHeight();
		double scale = (double) height / (double) h;
		// 只压缩不拉伸
		if (scale >= 1) {
			return src;
		}

		BufferedImage dst = ImageUtils.zoom(src, scale, scale, formatName);
		return dst;
	}

	/**
	 * 根据宽度等比例压缩图片
	 * 
	 * @param src
	 * @param width
	 * @param formatName
	 * @return
	 */
	public static BufferedImage compressWithWidth(BufferedImage src, int width, String formatName) {
		if (null == src) {
			throw new IllegalArgumentException();
		}
		if (width <= 0) {
			throw new IllegalArgumentException();
		}

		int w = src.getWidth();
		double scale = (double) width / (double) w;
		// 只压缩不拉伸
		if (scale >= 1) {
			return src;
		}

		BufferedImage dst = ImageUtils.zoom(src, scale, scale, formatName);
		return dst;
	}

	/**
	 * 从原始图像中居中截取最大的正方形图像
	 * 
	 * @param source
	 * @return
	 */
	public static ByteArrayOutputStream centerSquare(File source) {
		if (isIllegal(source)) {
			throw new IllegalArgumentException();
		}

		byte[] bytes = ImageUtils.convert2ByteArray(source);

		return ImageUtils.centerSquare(bytes);
	}

	/**
	 * 从原始图像中居中截取最大的正方形图像
	 * 
	 * @param source
	 * @return
	 */
	public static ByteArrayOutputStream centerSquare(final byte[] source) {
		if (null == source || source.length == 0) {
			throw new IllegalArgumentException();
		}

		try {
			ByteArrayInputStream input = new ByteArrayInputStream(source);
			BufferedImage src = ImageIO.read(input);

			int height = src.getHeight();
			int width = src.getWidth();

			// 计算截取正方形的参数
			boolean isWidth = false;
			int length = 0;
			int difference = 0;
			if (height == width) {
				return ImageUtils.convert2OutputStream(source);
			} else if (height < width) {
				length = height;
				difference = width - height;
				isWidth = true;
			} else {
				length = width;
				difference = height - width;
				isWidth = false;
			}

			// 计算坐标
			int temp = difference / 2;
			int x = 0;
			int y = 0;
			if (isWidth) {
				x = temp;
			} else {
				y = temp;
			}

			// 截取图片
			src = ImageUtils.cutImageByRectangle(source, x, y, length, length);
			if (null != src) {
				String formatName = ImageUtils.getFormatName(source);
				ByteArrayOutputStream dest = new ByteArrayOutputStream();
				ImageIO.write(src, formatName, dest);
				return dest;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 图像平移
	 * 
	 * @param source
	 *            源图像
	 * @param x
	 *            X轴平移量
	 * @param y
	 *            Y轴平移量
	 * @return
	 */
	public static ByteArrayOutputStream translation(File source, double x, double y) {
		if (isIllegal(source)) {
			throw new IllegalArgumentException();
		}

		byte[] bytes = ImageUtils.convert2ByteArray(source);

		return ImageUtils.translation(bytes, x, y);
	}

	/**
	 * 图像平移
	 * 
	 * @param source
	 *            源图像
	 * @param x
	 *            X轴平移量
	 * @param y
	 *            Y轴平移量
	 * @return
	 */
	public static ByteArrayOutputStream translation(final byte[] source, double x, double y) {
		if (null == source || source.length == 0) {
			throw new IllegalArgumentException();
		}

		try {
			String formatName = ImageUtils.getFormatName(source);

			ByteArrayInputStream input = new ByteArrayInputStream(source);
			BufferedImage src = ImageIO.read(input);

			src = ImageUtils.translation(src, x, y, formatName);
			if (null != src) {
				ByteArrayOutputStream dest = new ByteArrayOutputStream();
				ImageIO.write(src, formatName, dest);
				return dest;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static ByteArrayOutputStream rotate(File source, double angle, double x, double y) {
		if (isIllegal(source)) {
			throw new IllegalArgumentException();
		}

		byte[] bytes = ImageUtils.convert2ByteArray(source);

		return ImageUtils.rotate(bytes, angle, x, y);
	}

	public static ByteArrayOutputStream rotate(final byte[] source, double angle, double x, double y) {
		if (null == source || source.length == 0) {
			throw new IllegalArgumentException();
		}

		try {
			String formatName = ImageUtils.getFormatName(source);

			ByteArrayInputStream input = new ByteArrayInputStream(source);
			BufferedImage src = ImageIO.read(input);

			src = ImageUtils.rotate(src, angle, x, y, formatName);
			if (null != src) {
				ByteArrayOutputStream dest = new ByteArrayOutputStream();
				ImageIO.write(src, formatName, dest);
				return dest;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 向一个图像附加另一个图像
	 * 
	 * @param background
	 *            背景（被附加的图像）
	 * @param foreground
	 *            前景（附加的图像）
	 * @param x
	 *            前景位于背景的X轴坐标
	 * @param y
	 *            前景位于背景的X轴坐标
	 * @param width
	 *            前景宽度
	 * @param height
	 *            前景高度
	 * @param alpha
	 *            透明度（值为0到1之间的浮点数，且包括0和1，0-完全透明，1-完全不透明）
	 * @return
	 */
	public static BufferedImage attachImage(final byte[] background, final byte[] foreground, int x, int y, int width,
			int height, float alpha) {
		if (null == background || background.length == 0) {
			throw new IllegalArgumentException();
		}
		if (null == foreground || foreground.length == 0) {
			throw new IllegalArgumentException();
		}

		try {
			Image imageBack = ImageIO.read(new ByteArrayInputStream(background));
			Image imageFore = ImageIO.read(new ByteArrayInputStream(foreground));
			BufferedImage image = ImageUtils.attachImage(imageBack, imageFore, x, y, width, height, alpha);
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 向一个图像附加另一个图像
	 * <p>
	 * 注意：设置透明度必须相应的图片格式配合，如png、gif
	 * </p>
	 * 
	 * @param background
	 *            背景（被附加的图像）
	 * @param foreground
	 *            前景（附加的图像）
	 * @param x
	 *            前景位于背景的X轴坐标
	 * @param y
	 *            前景位于背景的X轴坐标
	 * @param alpha
	 *            透明度（值为0到1之间的浮点数，且包括0和1，0-完全透明，1-完全不透明）
	 * @return
	 */
	public static BufferedImage attachImage(final byte[] background, final byte[] foreground, int x, int y, float alpha) {
		if (null == background || background.length == 0) {
			throw new IllegalArgumentException();
		}
		if (null == foreground || foreground.length == 0) {
			throw new IllegalArgumentException();
		}

		try {
			Image imageBack = ImageIO.read(new ByteArrayInputStream(background));
			Image imageFore = ImageIO.read(new ByteArrayInputStream(foreground));
			BufferedImage image = ImageUtils.attachImage(imageBack, imageFore, x, y, alpha);
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 向一个图像附加另一个图像
	 * <p>
	 * 注意：设置透明度必须相应的图片格式配合，如png、gif
	 * </p>
	 * 
	 * @param background
	 *            背景（被附加的图像）
	 * @param foreground
	 *            前景（附加的图像）
	 * @param x
	 *            前景位于背景的X轴坐标
	 * @param y
	 *            前景位于背景的X轴坐标
	 * @return
	 */
	public static BufferedImage attachImage(final byte[] background, final byte[] foreground, int x, int y) {
		if (null == background || background.length == 0) {
			throw new IllegalArgumentException();
		}
		if (null == foreground || foreground.length == 0) {
			throw new IllegalArgumentException();
		}

		try {
			Image imageBack = ImageIO.read(new ByteArrayInputStream(background));
			Image imageFore = ImageIO.read(new ByteArrayInputStream(foreground));
			BufferedImage image = ImageUtils.attachImage(imageBack, imageFore, x, y);
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 向一个图像附加另一个图像
	 * <p>
	 * 注意：设置透明度必须相应的图片格式配合，如png、gif
	 * </p>
	 * 
	 * @param background
	 *            背景（被附加的图像）
	 * @param foreground
	 *            前景（附加的图像）
	 * @param x
	 *            前景位于背景的X轴坐标
	 * @param y
	 *            前景位于背景的X轴坐标
	 * @param alpha
	 *            透明度（值为0到1之间的浮点数，且包括0和1，0-完全透明，1-完全不透明）
	 * @return
	 */
	public static BufferedImage attachImage(File background, File foreground, int x, int y, float alpha) {
		if (isIllegal(background)) {
			throw new IllegalArgumentException();
		}
		if (isIllegal(foreground)) {
			throw new IllegalArgumentException();
		}

		try {
			Image imageBack = ImageIO.read(background);
			Image imageFore = ImageIO.read(foreground);
			BufferedImage image = ImageUtils.attachImage(imageBack, imageFore, x, y, alpha);
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 向一个图像附加另一个图像
	 * 
	 * @param background
	 *            背景（被附加的图像）
	 * @param foreground
	 *            前景（附加的图像）
	 * @param x
	 *            前景位于背景的X轴坐标
	 * @param y
	 *            前景位于背景的X轴坐标
	 * @return
	 */
	public static BufferedImage attachImage(File background, File foreground, int x, int y) {
		if (isIllegal(background)) {
			throw new IllegalArgumentException();
		}
		if (isIllegal(foreground)) {
			throw new IllegalArgumentException();
		}

		try {
			Image imageBack = ImageIO.read(background);
			Image imageFore = ImageIO.read(foreground);
			BufferedImage image = ImageUtils.attachImage(imageBack, imageFore, x, y);
			return image;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 根据字符串生成指定大小的PNG格式的二维码图像
	 * 
	 * @param content
	 * @param width
	 *            二维码图像宽度
	 * @param height
	 *            二维码图像高度
	 * @return
	 */
	public static ByteArrayOutputStream createQRCodeImage(String content, int width, int height) {
		BitMatrix matrix = ImageUtils.convert2BitMatrix(content, width, height);
		BufferedImage image = ImageUtils.convert2BufferedImage(matrix);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, ImageFormat.JPEG.getName(), output);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;
	}

	@Deprecated
	public static BufferedImage[] readImage(File file) {
		if (isIllegal(file)) {
			throw new IllegalArgumentException();
		}

		BufferedImage[] images = null;
		ImageInputStream iis = null;
		ImageReader reader = null;

		try {
			BufferedImage source = ImageIO.read(file);
			iis = ImageIO.createImageInputStream(file);
			Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
			if (imageReaders.hasNext()) {
				reader = imageReaders.next();
				reader.setInput(iis);
				int number = reader.getNumImages(true);
				images = new BufferedImage[number];
				for (int i = 0; i < number; i++) {
					BufferedImage image = reader.read(i);
					if (source.getWidth() > image.getWidth() || source.getHeight() > image.getHeight()) {
						image = zoom(image, source.getWidth(), source.getHeight(), reader.getFormatName());
					}
					images[i] = image;
				}
				reader.dispose();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.dispose();
			}
			if (iis != null) {
				try {
					iis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return images;
	}

	@Deprecated
	public static BufferedImage[] processImage(final BufferedImage[] images, int x, int y, int width, int height)
			throws Exception {
		if (null == images) {
			return images;
		}

		BufferedImage[] newImages = new BufferedImage[images.length];
		for (int i = 0; i < images.length; i++) {
			BufferedImage newImage = images[i];
			newImages[i] = newImage.getSubimage(x, y, width, height);
		}
		return newImages;
	}

	@Deprecated
	public static void writerImage(BufferedImage[] images, String formatName, File file) throws Exception {
		Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName(formatName);
		if (imageWriters.hasNext()) {
			ImageWriter writer = imageWriters.next();
			String fileName = file.getName();
			int index = fileName.lastIndexOf(".");
			if (index > 0) {
				fileName = fileName.substring(0, index + 1) + formatName;
			}
			String pathPrefix = "";
			File outFile = new File(pathPrefix + fileName);
			ImageOutputStream ios = ImageIO.createImageOutputStream(outFile);
			writer.setOutput(ios);

			if (writer.canWriteSequence()) {
				writer.prepareWriteSequence(null);
				for (int i = 0; i < images.length; i++) {
					BufferedImage childImage = images[i];
					IIOImage image = new IIOImage(childImage, null, null);
					writer.writeToSequence(image, null);
				}
				writer.endWriteSequence();
			} else {
				for (int i = 0; i < images.length; i++) {
					writer.write(images[i]);
				}
			}

			writer.dispose();
			ios.close();
		}
	}

	@Deprecated
	public static void cutImage(File source, File dest, int x, int y, int width, int height) throws Exception {
		// 读取图片信息
		BufferedImage[] images = readImage(source);
		// 处理图片
		images = processImage(images, x, y, width, height);
		// 获取文件后缀
		String formatName = getFormatName(source);
		// dest = new File(getPathWithoutSuffix(dest.getPath()) + formatName);

		// 写入处理后的图片到文件
		writerImage(images, formatName, dest);
	}

	/**
	 * 获取系统支持的图片格式
	 */
	public static void getOSSupportsStandardImageFormat() {
		String[] readerFormatName = ImageIO.getReaderFormatNames();
		String[] readerSuffixName = ImageIO.getReaderFileSuffixes();
		String[] readerMIMEType = ImageIO.getReaderMIMETypes();

		System.out.println("========================= OS supports reader ========================");
		System.out.println("OS supports reader format name :  " + Arrays.asList(readerFormatName));
		System.out.println("OS supports reader suffix name :  " + Arrays.asList(readerSuffixName));
		System.out.println("OS supports reader MIME type :  " + Arrays.asList(readerMIMEType));

		String[] writerFormatName = ImageIO.getWriterFormatNames();
		String[] writerSuffixName = ImageIO.getWriterFileSuffixes();
		String[] writerMIMEType = ImageIO.getWriterMIMETypes();

		System.out.println("========================= OS supports writer ========================");
		System.out.println("OS supports writer format name :  " + Arrays.asList(writerFormatName));
		System.out.println("OS supports writer suffix name :  " + Arrays.asList(writerSuffixName));
		System.out.println("OS supports writer MIME type :  " + Arrays.asList(writerMIMEType));
	}

	public static void main(String[] args) {
		// ImageUtils.getOSSupportsStandardImageFormat();
		// File file = new File("C:\\Users\\Michael\\Pictures\\logo.jpg");
		// File file = new File("C:\\Users\\Michael\\Pictures\\20151103111025.png");
		// File file = new File("C:\\Users\\Michael\\Pictures\\20150901163853.jpg");
		// File file = new File("C:\\Users\\Michael\\Pictures\\children.gif");
		// File file = new File("C:\\Users\\Michael\\Pictures\\special.jpg");
		// File file = new File("C:\\Users\\Michael\\Pictures\\qrCode.png");
		try {
			// BufferedImage src = ImageIO.read(file);
			// src.createGraphics().drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)
			// ByteArrayOutputStream output = ImageUtils.compressWithWidth(file, 150);
			// ByteArrayOutputStream output = ImageUtils.centerSquare(file);
			// ByteArrayOutputStream output = ImageUtils.translation(file, 0, -264.5);
			// ByteArrayOutputStream output = ImageUtils.rotate(file, 45D, 300D, 300D);
			// ByteArrayOutputStream output = ImageUtils.createQRCode("http://www.baidu.com", 300, 300);
			// BufferedImage image = ImageUtils.attachImage(file, file2, 250, 660, 0.5f);
			// ImageUtils.convert2File(output, "C:\\Users\\Michael\\Pictures\\", "result.jpg");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 图像文件格式
	 * 
	 * @author Michael
	 */
	public enum ImageFormat {
		/**  */
		BMP("bmp"),
		/**  */
		WBMP("wbmp"),
		/**  */
		JPG("jpg"),
		/**  */
		JPEG("jpeg"),
		/**  */
		PNG("png"),
		/**  */
		GIF("gif");

		private ImageFormat(String name) {
			this.name = name;
		}

		private final String name;

		public String getName() {
			return name;
		}

		public static ImageFormat fromName(String name) {
			for (ImageFormat item : ImageFormat.values()) {
				if (item.getName().equals(name)) {
					return item;
				}
			}

			return null;
		}

	}

}
