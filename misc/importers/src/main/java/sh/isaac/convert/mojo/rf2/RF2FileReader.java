/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributions from 2013-2017 where performed either by US government 
 * employees, or under US Veterans Health Administration contracts. 
 *
 * US Veterans Health Administration contributions by government employees
 * are work of the U.S. Government and are not subject to copyright
 * protection in the United States. Portions contributed by government 
 * employees are USGovWork (17USC §105). Not subject to copyright. 
 * 
 * Contribution by contractors to the US Veterans Health Administration
 * during this period are contractually contributed under the
 * Apache License, Version 2.0.
 *
 * See: https://www.usa.gov/government-works
 * 
 * Contributions prior to 2013:
 *
 * Copyright (C) International Health Terminology Standards Development Organisation.
 * Licensed under the Apache License, Version 2.0.
 *
 */
package sh.isaac.convert.mojo.rf2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;

import sh.isaac.converters.sharedUtils.sql.TerminologyFileReader;

/**
 * 
 * {@link RF2FileReader}
 *
 * Reads the CSV formatted release files.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RF2FileReader implements TerminologyFileReader
{
   private String[] header;
   private CSVReader reader;
   private int fieldCount_ = 0;
   private String[] next_;
   
   
   public RF2FileReader(InputStream in) throws IOException
   {
      reader = new CSVReader(new BufferedReader(new InputStreamReader(in,  "UTF-8")), '\t', CSVParser.NULL_CHARACTER);
      header = readLine();
      if (header != null)
      {
         next_ = readLine();
      }
   }
   
   public String[] getHeader()
   {
      return header;
   }

   private String[] readLine() throws IOException
   {
      String[] temp = reader.readNext();
      if (temp != null)
      {
         if (fieldCount_ == 0)
         {
            fieldCount_ = temp.length;
         }
         else if (temp.length < fieldCount_)
         {
            temp = Arrays.copyOf(temp, fieldCount_);
         }
         else if (temp.length > fieldCount_)
         {
            throw new RuntimeException("Data error - to many fields found on line: " + Arrays.toString(temp));
         }
      }
      return temp;
   }

   @Override
   public void close() throws IOException
   {
      reader.close();
   }

   @Override
   public boolean hasNextRow() throws IOException
   {
      return next_ != null;
   }
   
   public String[] peekNextRow() throws IOException
   {
      return next_;
   }

   @Override
   public List<String> getNextRow() throws IOException
   {
      if (next_ != null)
      {
         List<String> temp = Arrays.asList(next_);
         next_ = readLine();
         return temp;
      }
      else
      {
         throw new IOException("No more rows");
      }
   }
}
