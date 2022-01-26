/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samples;
import com.thingmagic.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.TreeMap;
import java.util.EnumSet;
import java.util.Vector;
import java.util.List;
/**
 *
 * @author rsoni
 */
public class SavedConfig {


  static SerialPrinter serialPrinter;
  static StringPrinter stringPrinter;
  static TransportListener currentListener;

  static void usage()
  {
    System.out.printf("Usage: demo reader-uri <command> [args]\n" +
                      "  (URI: 'tmr:///COM1' or 'tmr://astra-2100d3/' " +
                      "or 'tmr:///dev/ttyS0')\n\n" +
                      "Available commands:\n");
    System.exit(1);
  }
  
   public static void setTrace(Reader r, String args[])
  {
    boolean b;
    SerialReader sr;

    if (args[0].toLowerCase().equals("on"))
    {
      if (currentListener == null)
      {
        if (r instanceof SerialReader)
        {
          if (serialPrinter == null)
            serialPrinter = new SerialPrinter();
          currentListener = serialPrinter;
        }
        else if (r instanceof RqlReader)
        {
          if (stringPrinter == null)
            stringPrinter = new StringPrinter();
          currentListener = stringPrinter;
        }
        else
        {
          System.out.println("Reader does not support message tracing");
          return;
        }
        r.addTransportListener(currentListener);
      }
    }
    else if (currentListener != null)
    {
      r.removeTransportListener(currentListener);
      currentListener = null;
    }
  }
   
   static class SerialPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      System.out.print(tx ? "Sending: " : "Received:");
      for (int i = 0; i < data.length; i++)
      {
        if (i > 0 && (i & 15) == 0)
          System.out.printf("\n         ");
        System.out.printf(" %02x", data[i]);
      }
      System.out.printf("\n");
    }
  }

  static class StringPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      System.out.println((tx ? "Sending:\n" : "Receiving:\n") +
                         new String(data));
    }
  }


  public static void main(String argv[]) throws ReaderException
  {
    // Program setup
    TagFilter target;

    Reader r;
    int nextarg;
    boolean trace;

    r = null;
    target = null;
    trace = false;

    nextarg = 0;

    if (argv.length < 1)
      usage();

    if (argv[nextarg].equals("-v"))
    {
      trace = true;
      nextarg++;
    }
      r = Reader.create(argv[nextarg]);
      if (trace)
      {
        setTrace(r, new String[] {"on"});
      }
      r.connect();
      r.paramSet("/reader/region/id", Reader.Region.NA);
    try
    {
      SerialReader sr=(SerialReader)r;
      sr.cmdSetProtocol(TagProtocol.GEN2);
      sr.cmdSetUserProfile(SerialReader.SetUserProfileOption.SAVE,SerialReader.ConfigKey.ALL,SerialReader.ConfigValue.CUSTOM_CONFIGURATION);
      
	  byte data[]={0x06};
      byte response[];
      response=sr.cmdGetUserProfile(data);
      String str=sr.byteArrayToHexString(response);
      System.out.println(str);
      
      r.destroy();
    }
    catch (ReaderException re)
    {
      System.out.println("Error: " + re.getMessage());
    }
  }
}
