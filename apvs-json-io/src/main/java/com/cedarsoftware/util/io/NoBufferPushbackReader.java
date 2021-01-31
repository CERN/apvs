package com.cedarsoftware.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.cedarsoftware.util.io.JsonReader.FastPushbackReader;

/** 
 * Same as FastPushbackReader but based on simple InputStream without buffer
 * does NOT support UTF-8
 * 
 * @author duns
 */
public class NoBufferPushbackReader extends FastPushbackReader {
         
        private final InputStream in;

    	public NoBufferPushbackReader(InputStream in) {
    		super(new Reader() {

				@Override
				public void close() throws IOException {
					// ignored
				}

				@Override
				public int read(char[] arg0, int arg1, int arg2)
						throws IOException {
					return 0;
				}
    			
    		});	// Fake object...
    		this.in = in;
    	}
    	
        public int read() throws IOException
        {
            _pos++;
            if (_idx < _buf.length)
            {
                return _buf[_idx++];
            }
            return in.read();
        }

        public void close() throws IOException
        {
            in.close();
            _pos = 0;
        }

}
