package gov.vha.isaac.ochre.api.util;


/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
import java.util.Comparator;

/**
 * The Alphanum Algorithm is an improved sorting algorithm for strings containing numbers.
 *
 * Instead of sorting numbers in ASCII order like a standard sort, this algorithm sorts numbers in numeric order.
 *
 * The inspiration for this implementation came from http://www.DaveKoelle.com However, his implementation did not handle leading 0's properly, nor
 * did it handle nulls or case sensitivity.
 *
 * I fixed all of those issues, and also added convenience methods.
 *
 * @author <A HREF="mailto:daniel.armbrust@gmail.com">Dan Armbrust</A>
 *
 * See http://armbrust.dyndns.org/programs/index.php?page=3
 */
public class AlphanumComparator implements Comparator<String>
{
    private boolean ignoreCase_;
    private static AlphanumComparator caseSensitiveInstance_;
    private static AlphanumComparator caseInsensitiveInstance_;

    /**
     * Create a new instance of an AlphanumComparator.
     *
     * @param caseSensitive
     */
    public AlphanumComparator(boolean ignoreCase)
    {
        this.ignoreCase_ = ignoreCase;
    }

    /**
     * Get a reference to a cached, shared instance. Good for reuse, but would have multithreading issues if many threads are trying to sort at the
     * same time.
     *
     * @param caseSensitive
     */
    public static synchronized AlphanumComparator getCachedInstance(boolean ignoreCase)
    {
        if (ignoreCase)
        {
            if (caseSensitiveInstance_ == null)
            {
                caseSensitiveInstance_ = new AlphanumComparator(true);
            }
            return caseSensitiveInstance_;
        }
        else
        {
            if (caseInsensitiveInstance_ == null)
            {
                caseInsensitiveInstance_ = new AlphanumComparator(false);
            }
            return caseInsensitiveInstance_;
        }
    }

    private boolean isDigit(char ch)
    {
        return ch >= 48 && ch <= 57;
    }

    /**
     * Length of string is passed in for improved efficiency (only need to calculate it once) *
     */
    private String getChunk(String s, int slength, int marker)
    {
        StringBuilder chunk = new StringBuilder();
        char c = s.charAt(marker);
        chunk.append(c);
        marker++;
        if (isDigit(c))
        {
            while (marker < slength)
            {
                c = s.charAt(marker);
                if (!isDigit(c))
                {
                    break;
                }
                chunk.append(c);
                marker++;
            }
        }
        else
        {
            while (marker < slength)
            {
                c = s.charAt(marker);
                if (isDigit(c))
                {
                    break;
                }
                chunk.append(c);
                marker++;
            }
        }
        return chunk.toString();
    }

    /*
     * Take in string (which we assume will pass Integer.ParseInt) and return an array of integers.
     * An array is returned so we don't exceed the limits of int.
     * 
     * For example, 45600000000524566874861567 would be returned as : [456000000,005245668,74861567]
     */
    private int[] subChunkNumeric(String numericChunk)
    {
        int[] result = new int[(int) Math.ceil(numericChunk.length() / 9.0)];
        int s = 0;
        int e = (9 > numericChunk.length() ? numericChunk.length() : 9);
        for (int i = 0; i < result.length; i++)
        {
            result[i] = Integer.parseInt(numericChunk.substring(s, e));
            s = e;
            e = (e + 9 > numericChunk.length() ? numericChunk.length() : e + 9);
        }
        return result;
    }

    @Override
    public int compare(String s1, String s2)
    {
        if (s1 == null)
        {
            return -1;
        }
        if (s2 == null)
        {
            return 1;
        }

        int thisMarker = 0;
        int thatMarker = 0;
        int s1Length = s1.length();
        int s2Length = s2.length();

        while (thisMarker < s1Length && thatMarker < s2Length)
        {
            String thisChunk = getChunk(s1, s1Length, thisMarker);
            thisMarker += thisChunk.length();

            String thatChunk = getChunk(s2, s2Length, thatMarker);
            thatMarker += thatChunk.length();

            // If both chunks contain numeric characters, sort them numerically
            int result = 0;
            if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0)))
            {
                int[] thisChunkInt = subChunkNumeric(thisChunk);
                int[] thatChunkInt = subChunkNumeric(thatChunk);

                //0 pad the shorter array, so that they have the same length.
                if (thisChunkInt.length > thatChunkInt.length)
                {
                    int[] temp = new int[thisChunkInt.length];
                    int insertOffset = thisChunkInt.length - thatChunkInt.length;
                    for (int i = 0; i < thatChunkInt.length; i++)
                    {
                        temp[insertOffset++] = thatChunkInt[i];
                    }
                    thatChunkInt = temp;
                }
                else
                {
                    if (thisChunkInt.length < thatChunkInt.length)
                    {
                        int[] temp = new int[thatChunkInt.length];
                        int insertOffset = thatChunkInt.length - thisChunkInt.length;
                        for (int i = 0; i < thisChunkInt.length; i++)
                        {
                            temp[insertOffset++] = thisChunkInt[i];
                        }
                        thisChunkInt = temp;
                    }
                }

                for (int i = 0; i < thisChunkInt.length; i++)
                {
                    if (thisChunkInt[i] > thatChunkInt[i])
                    {
                        result = 1;
                        break;
                    }
                    else
                    {
                        if (thisChunkInt[i] < thatChunkInt[i])
                        {
                            result = -1;
                            break;
                        }
                    }
                }
            }
            else
            {
                if (ignoreCase_)
                {
                    result = thisChunk.compareToIgnoreCase(thatChunk);
                }
                else
                {
                    result = thisChunk.compareTo(thatChunk);
                }
            }

            if (result != 0)
            {
                return result;
            }
        }

        return s1Length - s2Length;
    }

    public static int compare(String left, String right, boolean ignoreCase)
    {
        return getCachedInstance(ignoreCase).compare(left, right);
    }
}
