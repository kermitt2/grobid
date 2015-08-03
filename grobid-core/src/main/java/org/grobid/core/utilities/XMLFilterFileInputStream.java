package org.grobid.core.utilities;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.xerces.util.XMLChar;

/**
 *  Custom FileInputStream skipping invalid xml version 1.0 characters.
 *  The list of invalid xml characters rely on Xerces implementation. 
 */
public class XMLFilterFileInputStream extends FileInputStream {

    public XMLFilterFileInputStream(String filepath) throws FileNotFoundException {
        super(filepath);
    }

    public XMLFilterFileInputStream(File file) throws FileNotFoundException {
        super(file);
    }

    public XMLFilterFileInputStream(FileDescriptor filedescriptor) {
        super(filedescriptor);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = 0, c;
        do {
            c = this.read();
            if(c != -1) {
                b[off + n] = (byte) c;
                n++;
                len--;  
            } else {
                return c;
            }
        } while(c != -1 && len > 0);
        return n;
    }

    @Override
    public int read() throws IOException {
        int c;
        do {
            c = super.read();
        } while((c != -1) && XMLChar.isInvalid(c));
        return c;
    }
	
    /*@Override
    public int read() throws IOException {
        int c;
        do {
            c = super.read();
        }
		while((c != -1) && !( (c == 0x9) ||	
			(c == 0xA) ||
			(c == 0xD) ||
			((c >= 0x20) && (c <= 0xD7FF)) ||
			((c >= 0xE000) && (c <= 0xFFFD)) ||
			((c >= 0x10000) && (c <= 0x10FFFF)) ));
        return c;
    }*/

}