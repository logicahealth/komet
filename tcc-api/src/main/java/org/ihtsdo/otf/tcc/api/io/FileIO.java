/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.api.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * From the book "Java Cookbook, 2nd Edition. Some simple file IO primitives
 * reimplemented in Java. All methods are static since there is no state.
 */
public class FileIO {

    /*
     * Comment by Tore
     * I changed the copy File method because on windows platforms the nio
     * transferTo method
     * cannot handle > 64MB files.
     * please see: http://www.rgagnon.com/javadetails/java-0064.html
     */

    /**
     * 
     * @param in
     * @param out
     * @throws IOException
     */
    public static void copyFile(File in, File out) throws IOException {
        FileChannel destinationChannel;
        try (FileChannel sourceChannel = new FileInputStream(in).getChannel()) {
            destinationChannel = new FileOutputStream(out).getChannel();
            int maxCount = (64 * 1024 * 1024) - (32 * 1024);
            long size = sourceChannel.size();
            long position = 0;
            while (position < size) {
                position += sourceChannel.transferTo(position, maxCount, destinationChannel);
            }
        }
        destinationChannel.close();
    }

    /**
     * 
     * @param from
     * @param to
     * @param copyInvisibles
     * @throws IOException
     */
    public static void recursiveCopy(File from, File to, boolean copyInvisibles) throws IOException {
        if (from.isDirectory()) {
            to.mkdirs();
            for (File f : from.listFiles()) {
                if (f.isHidden() == false || ((copyInvisibles == true) && (f.getName().endsWith(".DS_Store") == false))) {
                    File childTo = new File(to, f.getName());
                    recursiveCopy(f, childTo, copyInvisibles);
                }
            }
        } else {
            copyFile(from, to);
        }
    }

    /**
     * 
     * @param from
     * @throws IOException
     */
    public static void recursiveDelete(File from) throws IOException {
        if (from.isDirectory()) {
            for (File f : from.listFiles()) {
                recursiveDelete(f);
            }
        }
        from.delete();
    }

    /**
     * 
     */
    public static class FileAndObject {
        private Object obj;

        private File file;

        /**
         * 
         * @param obj
         * @param file
         */
        public FileAndObject(Object obj, File file) {
            super();
            this.obj = obj;
            this.file = file;
        }

        /**
         * 
         * @return
         */
        public File getFile() {
            return file;
        }

        /**
         * 
         * @return
         */
        public Object getObj() {
            return obj;
        }

    }

    /** Copy a file from one filename to another
     * @param inName
     * @param outName 
     * @throws FileNotFoundException 
     * @throws IOException  
     */
    public static void copyFile(String inName, String outName) throws FileNotFoundException, IOException {
        BufferedInputStream is = new BufferedInputStream(new FileInputStream(inName));
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(outName));
        copyFile(is, os, true);
    }
    
    /**
     * 
     * @param is
     * @param outFile
     * @throws IOException
     */
    public static void copyFile(InputStream is, File outFile) throws IOException {
        outFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(outFile);
        copyFile(is, fos, true);
    }

    /** Copy a file from an opened InputStream to an opened OutputStream
     * @param is 
     * @param os 
     * @param close 
     * @throws IOException 
     */
    public static void copyFile(InputStream is, OutputStream os, boolean close) throws IOException {
        byte[] buffer = new byte[4096];
        int length;
        int bytesWritten = 0;
        while (is.available() > 0) {
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
                bytesWritten = bytesWritten + length;
            }
        }
        is.close();
        if (close) {
            os.close();
        }
    }

    /** Copy a file from an opened Reader to an opened Writer
     * @param is
     * @param os
     * @param close 
     * @throws IOException  
     */
    public static void copyFile(Reader is, Writer os, boolean close) throws IOException {
        int b; // the byte read from the file
        while ((b = is.read()) != -1) {
            os.write(b);
        }
        is.close();
        if (close) {
            os.close();
        }
    }

    /** Copy a file from a filename to a PrintWriter.
     * @param inName 
     * @param pw
     * @param close 
     * @throws FileNotFoundException
     * @throws IOException  
     */
    public static void copyFile(String inName, PrintWriter pw, boolean close) throws FileNotFoundException, IOException {
        BufferedReader ir = new BufferedReader(new FileReader(inName));
        copyFile(ir, pw, close);
    }

    /** Open a file and read the first line from it.
     * @param inName 
     * @return 
     * @throws FileNotFoundException 
     * @throws IOException 
     */
    public static String readLine(String inName) throws FileNotFoundException, IOException {
        String line;
        try (BufferedReader is = new BufferedReader(new FileReader(inName))) {
            line = is.readLine();
        }
        return line;
    }

    /** The size of blocking to use */
    protected static final int BLKSIZ = 8192;

    /**
     * Copy a data file from one filename to another, alternate method. As the
     * name suggests, use my own buffer instead of letting the BufferedReader
     * allocate and use the buffer.
     * @param inName 
     * @param outName 
     * @throws FileNotFoundException 
     * @throws IOException 
     */
    public void copyFileBuffered(String inName, String outName) throws FileNotFoundException, IOException {
        OutputStream os;
        try (InputStream is = new FileInputStream(inName)) {
            os = new FileOutputStream(outName);
            int count;
            byte[] b = new byte[BLKSIZ];
            while ((count = is.read(b)) != -1) {
                os.write(b, 0, count);
            }
        }
        os.close();
    }

    /** Read the entire content of a Reader into a String
     * @param is 
     * @return
     * @throws IOException  
     */
    public static String readerToString(Reader is) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] b = new char[BLKSIZ];
        int n;

        // Read a block. If it gets any chars, append them.
        while ((n = is.read(b)) > 0) {
            sb.append(b, 0, n);
        }

        // Only construct the String object once, here.
        return sb.toString();
    }

    /** Read the content of a Stream into a String
     * @param is 
     * @return 
     * @throws IOException 
     */
    public static String inputStreamToString(InputStream is) throws IOException {
        return readerToString(new InputStreamReader(is));
    }

    /**
     * 
     */
    public static class FileAndObjectResult {
        private FileAndObject returnValue;
        private Exception ex;

        /**
         * 
         * @return
         */
        public FileAndObject getReturnValue() {
            return returnValue;
        }

        /**
         * 
         * @param returnValue
         */
        public void setReturnValue(FileAndObject returnValue) {
            this.returnValue = returnValue;
        }

        /**
         * 
         * @return
         */
        public Exception getEx() {
            return ex;
        }

        /**
         * 
         * @param ex
         */
        public void setEx(Exception ex) {
            this.ex = ex;
        }
    }


    /**
     * 
     */
    public static class FileResult {
        private File returnValue;
        private Exception ex;

        /**
         * 
         * @return
         */
        public File getReturnValue() {
            return returnValue;
        }

        /**
         * 
         * @param returnValue
         */
        public void setReturnValue(File returnValue) {
            this.returnValue = returnValue;
        }

        /**
         * 
         * @return
         */
        public Exception getEx() {
            return ex;
        }

        /**
         * 
         * @param ex
         */
        public void setEx(Exception ex) {
            this.ex = ex;
        }
    }

    /**
     * Accepts a string with regular expressions and possibly /../ portions.
     * Removes the /../ sections, by substituting the higher named directory,
     * and
     * then returns the first file in the file system that matches the
     * optionally included regular expression.
     * 
     * @param s
     * @return
     */
    public static File normalizeFileStr(String s) {
        // System.out.println("s" + s);
        int slashDotIndex = s.indexOf("/../");
        while (slashDotIndex >= 0) {
            String part1 = s.substring(0, slashDotIndex);
            // System.out.println("part1a " + part1);
            part1 = part1.substring(0, part1.lastIndexOf(File.separator));
            // System.out.println("part1b " + part1);
            String part2 = s.substring(slashDotIndex + 3);
            // System.out.println("part2 " + part2);
            s = part1 + part2;
            // System.out.println("s " + s);
            slashDotIndex = s.indexOf("/../");
        }
        s = s.replace('/', File.separatorChar);
        File inputFile = new File(s);
        if (inputFile.exists()) {
            return inputFile;
        }
        // Find an ancestor that exists
        File p = inputFile.getParentFile();
        while (p.exists() == false) {
            p = p.getParentFile();
        }
        // Try regular expression matching...

        // System.out.println("Regex: " + normalizeFileString(inputFile));
        Pattern pattern = Pattern.compile(normalizeFileString(inputFile));

        File f = matchPattern(p, pattern);
        return f;
    }

    private static String normalizeFileString(File f) {
        // return f.getAbsolutePath().replace("/", " ").replace("-", " ");
        String path = f.getAbsolutePath();
        path = path.replace("[\\", "[@");
        path = path.replace('\\', '/');
        path = path.replace("[@", "[\\");
        return path;
    }

    private static File matchPattern(File p, Pattern pattern) {
        for (File f : p.listFiles()) {
            // System.out.println("Current file: " + f.toString());
            // System.out.println("Testing: " + f.getAbsolutePath());
            // System.out.println("Testing normal: " + normalizeFileString(f));
            Matcher m = pattern.matcher(normalizeFileString(f));
            if (m.matches()) {
                return f;
            }
            if (f.isDirectory()) {
                File result = matchPattern(f, pattern);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Returns a String of the relative directory of a file, relative to the
     * {@code user.dir} System property.
     * 
     * @param f the file to get the relative directory of.
     * @return a String of the relative directory.
     */
    public static String getRelativePath(File f) {
        File startupDir = new File(System.getProperty("user.dir"));
        String startupDirString = startupDir.getAbsolutePath();
        String fileAbsolutePath = f.getAbsolutePath();
        if (fileAbsolutePath.contains(startupDirString)) {
            return fileAbsolutePath.substring(startupDirString.length() + 1);
        }

        int depth = 1;
        File parent = startupDir.getParentFile();
        while (fileAbsolutePath.contains(parent.getAbsolutePath()) == false) {
            depth++;
            parent = parent.getParentFile();
        }
        StringBuilder relativePath = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            relativePath.append("..").append(File.separator);
        }
        relativePath.append(fileAbsolutePath.substring(parent.getAbsolutePath().length() + 1));
        return relativePath.toString();
    }

    /**
     * 
     * @param f
     * @return
     */
    public static String getNormalizedRelativePath(File f) {
        return getRelativePath(f).replace('\\', '/');
    }

    /**
     * 
     * @param rootFile
     * @param prefix
     * @param suffix
     * @param excludeHidden
     * @return
     */
    public static List<File> recursiveGetFiles(File rootFile, String prefix, String suffix, boolean excludeHidden) {
        List<File> fileList = new ArrayList<>();
        recursiveGetFiles(rootFile, fileList, prefix, suffix, excludeHidden);
        return fileList;
    }

    private static void recursiveGetFiles(File rootFile, List<File> fileList, final String prefix, final String suffix,
            final boolean excludeHidden) {
        File[] children = rootFile.listFiles(new FileFilter() {

            @Override
            public boolean accept(File child) {
                if (excludeHidden) {
                    if (child.isHidden() || child.getName().startsWith(".")) {
                        return false;
                    }
                }
                if (child.isDirectory()) {
                    return true;
                }
                if (prefix != null && prefix.length() > 1) {
                    return child.getName().endsWith(suffix) && child.getName().startsWith(prefix);
                } else {
                    return child.getName().endsWith(suffix);
                }
            }
        });
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    recursiveGetFiles(child, fileList, prefix, suffix, excludeHidden);
                } else {
                    fileList.add(child);
                }
            }
        }
    }

}
