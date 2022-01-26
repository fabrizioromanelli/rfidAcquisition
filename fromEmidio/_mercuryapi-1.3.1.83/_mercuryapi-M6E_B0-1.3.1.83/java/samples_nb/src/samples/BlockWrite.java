/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samples;
import com.thingmagic.*;
/**
 *
 * @author rsoni
 */
public class BlockWrite
{


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
      byte data1[]= {1,2,3,4};
      Gen2.BlockWrite tagop=new Gen2.BlockWrite(Gen2.Bank.USER,0,(byte)2,data1);
      Gen2.Select select=new Gen2.Select(false,Gen2.Bank.EPC,0,0,null);
      sr.executeTagOp(tagop,select);
      r.destroy();
    }
    catch (ReaderException re)
    {
      System.out.println("Error: " + re.getMessage());
    }
  }
}
