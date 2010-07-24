package jgbe;
/*
 *  IMPORTANT: THIS FILE IS AUTOGENERATED
 *
 *  Any modifications to this file will be lost when regenerating it.
 *  Modify the corresponding .jpp file instead, and regenerate this file.
 */







import java.io.*;
import java.lang.Integer;

class LZOutputStream extends OutputStream {
 public static final int FILE_MAGIX_UNKNOWN = 0xfe;

 public static final int min_copy_len = 4;
 public static final int max_copy_len = min_copy_len + 256 - 1;
 public static final int max_lit_len = 127;







 private int hash_bits;
 private int hash_size;
 private int hash_mask;
 private int hash_shift;

 private int wind_bits;
 private int wind_size;
 private int wind_mask;

 private int[] hashhead;
 private int[] hashprev;

 private byte[] inbuf;

 private int input_size;
 private int input_mask;
 private int ifpos;
 private int inlen;

 private int lookupind;
 private int nmlen;
 private int mlen;
 private int mind;

 private OutputStream outstr;

 private void advance_hash(int value) {
  lookupind = (((lookupind<<hash_shift)^(value))&hash_mask);
 };

 private void write_output() throws IOException {
  if (lookupind == -1 && inlen >= min_copy_len) {

   lookupind = 0;
   for (int i = 0; i < min_copy_len; ++i)
    advance_hash(inbuf[i]);
  }

  mlen = 0;
  mind = -1;


  if (inlen >= min_copy_len) {

   int bound = inlen;
   if (bound > max_copy_len) bound = max_copy_len;


   for (int lind = hashhead[lookupind]; ifpos < lind + wind_size; lind = hashprev[lind & wind_mask]) {
    int clen = 0;
    while ((clen < bound) && (inbuf[(ifpos+clen)&input_mask] == inbuf[(lind+clen)&input_mask]))
     clen++;
    if (clen >= min_copy_len && clen > mlen) {
     mlen = clen;
     mind = lind;
     if (mlen == max_copy_len) break;
    }
   }
  }


  if (mlen==0) {
   if (inlen >= min_copy_len) {

    hashprev[ifpos & wind_mask] = hashhead[lookupind];
    hashhead[lookupind] = ifpos;
    advance_hash(inbuf[(ifpos+min_copy_len)&input_mask]&0xff);
   }
   ++nmlen;
   ++ifpos;
   --inlen;
  };


  if ((mlen!=0 && nmlen!=0) || nmlen == 127 || (inlen == 0 && nmlen != 0)) {
   outstr.write(nmlen | (1<<7));
   for (int i = ifpos-nmlen; i < ifpos; ++i)
    outstr.write(inbuf[i&input_mask]);
   nmlen = 0;
  }


  if (mlen!=0) {
   mind = ifpos - mind;
   outstr.write(mind >> 8);
   outstr.write(mind & 0xFF);
   outstr.write(mlen-min_copy_len);
   while (inlen > 0 && (mlen > 0)) {
    if (inlen >= min_copy_len) {

     hashprev[ifpos & wind_mask] = hashhead[lookupind];
     hashhead[lookupind] = ifpos;
     advance_hash(inbuf[(ifpos+min_copy_len)&input_mask]&0xff);
    }
    ++ifpos;
    --inlen;
    --mlen;
   }
  }
 };

 private void initialize(OutputStream ostream, int windowbits, int numhashbits) throws IOException
 {
  if (windowbits <= 0) throw new IOException("Invalid number of window bits");
  if (windowbits > 15) throw new IOException("Invalid number of window bits");
  if (numhashbits < 4) throw new IOException("Invalid number of hash bits");
  if (numhashbits > 30) throw new IOException("Invalid number of hash bits");

  wind_bits = windowbits;
  wind_size = 1 << wind_bits;
  wind_mask = wind_size - 1;

  hash_bits = numhashbits;
  hash_size = 1 << hash_bits;
  hash_mask = hash_size - 1;
  hash_shift = (hash_bits+3) >> 2;

  outstr = ostream;

  hashhead = new int[hash_size];
  hashprev = new int[wind_size];

  for (int i = 0; i < hash_size; ++i)
   hashhead[i] = -wind_size;

  input_size = wind_size * 2;
  input_mask = input_size - 1;

  inbuf = new byte[input_size];
  ifpos = 0;
  inlen = 0;

  lookupind = -1;
  nmlen = 0;
  mlen = 0;

  outstr.write(FILE_MAGIX_UNKNOWN);
  outstr.write(wind_bits);
 }

 public LZOutputStream(OutputStream ostream, int windowbits, int numhashbits) throws IOException
 {
  initialize(ostream, windowbits, numhashbits);
 }

 public LZOutputStream(OutputStream ostream) throws IOException
 {
  initialize(ostream, 15, 16);
 }

 public void write(int b) throws IOException
 {
  try {
   inbuf[(ifpos + inlen) & input_mask] = (byte)b;
   ++inlen;
   while (inlen >= max_copy_len+max_lit_len)
    write_output();
  } catch (Throwable e) {
   throw new IOException(e.toString());
  }
 }

 public void flush() throws IOException
 {
  try {
   while (inlen > 0)
    write_output();

   for (int i = 0; i < hash_size; ++i)
    hashhead[i] = -wind_size;

   lookupind = -1;

   outstr.flush();
  } catch (Throwable e) {
   throw new IOException(e.toString());
  }
 }

 public void close() throws IOException
 {
  if (inbuf != null) {
   flush();
   inbuf = null;
   hashhead = null;
   hashprev = null;
  }
  outstr.close();
 }

 protected void finalize() throws Throwable
 {
  close();

 }



 public static void main(String[] args) {
  try {
   String ifname = args[0];
   String ofname = args[1];
   int windbits = Integer.parseInt(args[2]);
   int hashbits = Integer.parseInt(args[3]);

   System.out.println("Input file : " + ifname);
   System.out.println("Output file: " + ofname);
   System.out.println("Window bits: " + windbits);
   System.out.println("Hash bits  : " + hashbits);

   InputStream istr = new FileInputStream(ifname);
   OutputStream ostr = new LZOutputStream(new FileOutputStream(ofname), windbits, hashbits);

   int x = 0;
   int bread;
   while (-1 != (bread = istr.read())) {
    ostr.write(bread);
    if (++x == 1024*1024) {
     System.out.print(".");
     x = 0;
    }
   }

   System.out.println("Done");
   istr.close();
   ostr.close();
  } catch (Throwable e) {
   System.out.println("Something went wrong!");
   e.printStackTrace();
  }
 }

}