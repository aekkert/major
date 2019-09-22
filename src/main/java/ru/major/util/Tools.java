package ru.major.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author alex
 */
public class Tools {
      /**
   * Проверает на null и закрывает перехватывая исключения
   * @param c
   */
  public static void close(Closeable c) {
    if(c != null) {
      try {
        c.close();
      } catch(Throwable th) {
      }
    }
  }

  /**
   * Проверает на null и закрывает перехватывая исключения
   * @param c
   */
  public static void close(Connection c) {
    if(c != null) {
      try {
        c.close();
      } catch(Throwable th) {
      }
    }
  }

  /**
   * Проверает на null и закрывает перехватывая исключения
   * @param c
   */
  public static void close(Statement c) {
    if(c != null) {
      try {
        c.close();
      } catch(Throwable th) {
      }
    }
  }

  /**
   * Проверает на null и закрывает перехватывая исключения
   * @param c
   */
  public static void close(ResultSet c) {
    if(c != null) {
      try {
        c.close();
      } catch(Throwable th) {
      }
    }
  }
  
  public static String getConteType(String ext) {
      String res = "application/octet-stream";
      final String EXT_TYPES[][] = {{"jpg", "image/jpeg"},
                                    {"jpe", "image/jpeg"},  
                                    {"jpeg", "image/jpeg"},  
                                    {"bmp", "image/bmp"},  
                                    {"tif", "image/tiff"},  
                                    {"doc", "application/msword"},  
                                    {"docx", "application/msword"},  
                                    {"txt", "text/plain"},  
                                    {"zip", "application/zip"},  
                                    {"wrl", "x-world/x-vrml"},  
                                    {"wml", "text/vnd.wap.wml"},  
                                    {"wbmp", "image/vnd.wap.wbmp"},
                                    {"vsd", "application/x-visio"},
                                    {"wav", "audio/x-wav"},
                                    {"xsl", "application/xml"},
                                    {"xml", "application/xml"},
                                    {"tar", "application/x-tar"},
                                    {"swf", "application/x-shockwave-flash"},
                                    {"svg", "image/svg+xml"},
                                    {"psd", "image/x-photoshop"},
                                    {"rtf", "application/rtf"},
                                    {"rtx", "text/richtext"},
                                    {"qt", "video/quicktime"},
                                    {"png", "image/png"},
                                    {"pict", "image/pict"},
                                    {"pic", "image/pict"},
                                    {"pct", "image/pict"},
                                    {"pdf", "application/pdf"},
                                    {"mpg", "video/mpeg"},
                                    {"mpega", "audio/x-mpeg"},
                                    {"mpeg", "audio/x-mpeg"},
                                    {"mp3", "audio/x-mpeg"},
                                    {"mov", "video/quicktime"},
                                    {"midi", "audio/x-midi"},
                                    {"html", "text/html"},
                                    {"htm", "text/html"},
                                    {"avi", "video/x-msvideo"},
                                    {"ogg", "audio/ogg;codec=opus"},
                                    {"wav", "audio/wav"}
                                };
      for ( String[] a :  EXT_TYPES)
          if ( ext.equalsIgnoreCase(a[0])) {
              res = a[1];
              break;
          }
      return res;
    }

  public static final String Transliterate(final String src) {
        final int C_BADS[] = {(int)'№',(int)'~',(int)'@',(int)'$',(int)'%',(int)'^',(int)'&',(int)'*',(int)'(',(int)')',(int)'?'};
        
        if ( src == null )
            return null;
        String res = "";
        int c;
        StringReader r = new StringReader(src.toLowerCase());
        try {
            while ( (c = r.read()) != -1 ) {
                switch ( c ) {
                    case (int)'а':
                        res += 'a';
                        break;
                    case (int)'б': 
                        res += 'b';
                        break;
                    case (int)'в':
                        res += 'v';
                        break;
                    case (int)'г':
                        res += 'g';
                        break;
                    case (int)'д':
                        res += 'd';
                        break;
                    case (int)'е':
                        res += 'e';
                        break;
                    case (int)'ё':
                        res += "yo";
                        break;
                    case (int)'ж':
                        res += "zh";
                        break;
                    case (int)'з':
                        res += 'z';
                        break;
                    case (int)'и':
                        res += 'i';
                        break;
                    case (int)'й':
                        res += 'j';
                        break;
                    case (int)'к':
                        res += 'k';
                        break;
                    case (int)'л':
                        res += 'l';
                        break;
                    case (int)'м':
                        res += 'm';
                        break;
                    case (int)'н':
                        res += 'n';
                        break;
                    case (int)'о':
                        res += 'o';
                        break;
                    case (int)'п':
                        res += 'p';
                        break;
                    case (int)'р':
                        res += 'r';
                        break;
                    case (int)'с':
                        res += 's';
                        break;
                    case (int)'т':
                        res += 't';
                        break;
                    case (int)'у':
                        res += 'u';
                        break;
                    case (int)'ф':
                        res += 'f';
                        break;
                    case (int)'х':
                        res += 'h';
                        break;
                    case (int)'ц':
                        res += 'c';
                        break;
                    case (int)'ч':
                        res += "ch";
                        break;
                    case (int)'ш':
                        res += "sh";
                        break;
                    case (int)'щ':
                        res += 'w';
                        break;
                    case (int)'ы':
                        res += 'y';
                        break;
                    case (int)'ъ':
                        res += '#';
                        break;
                    case (int)'ь':
                        res += '\'';
                        break;
                    case (int)'э':
                        res += "je";
                        break;
                    case (int)'ю':
                        res += "yu";
                        break;
                    case (int)'я':
                        res += "ya";
                        break;
                    default:
                        boolean fIncl = true;
                        for (int b : C_BADS) if ( b == c ) {fIncl = false;break;}
                        if ( fIncl )
                            res += (char)c;
                }
            }       //while
        } catch(java.lang.Throwable tw) {
            tw.getMessage();
        }
        return res;
    }
    
    public static final long guidToLn(String guid){
        UUID uuid = java.util.UUID.fromString(guid);
        return uuid.getMostSignificantBits() & Long.MAX_VALUE;
    }
    
    public static final String makeImage(BufferedImage image, String img) throws IOException {
        File f = File.createTempFile("img", ".jpg");
        FileUtils.copyURLToFile(new URL(img), f);
        BufferedImage overlay = ImageIO.read(f);
        BufferedImage resized = resize(overlay, 735, 735);
        // create the new image, canvas size is the max. of both image sizes
        int w = Math.max(image.getWidth(), image.getWidth());
        int h = Math.max(image.getHeight(), image.getHeight());
        BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // paint both images, preserving the alpha channels
        Graphics g = combined.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.drawImage(resized, 34, 34, null);
        f.delete();
        return encodeToString(combined);
    }

    private static BufferedImage resize(BufferedImage img, int height, int width) {
        Image tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return resized;
    }
    
    public static String encodeToString(BufferedImage image) throws IOException {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        imageString = new String(java.util.Base64.getEncoder().encode(bos.toByteArray()));
        return imageString;
    }
}
