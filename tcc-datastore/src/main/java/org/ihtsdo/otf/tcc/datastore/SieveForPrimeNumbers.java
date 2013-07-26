/* Copyright (c) 2011 the authors listed at the following URL, and/or
the authors of referenced articles or incorporated external code:
http://en.literateprograms.org/Sieve_of_Eratosthenes_(Java)?action=history&offset=20080113061739

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Retrieved from: http://en.literateprograms.org/Sieve_of_Eratosthenes_(Java)?oldid=12079

 * 
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.otf.tcc.datastore;

import java.util.BitSet;
import java.util.List;
import java.util.ArrayList;

public class SieveForPrimeNumbers {

    private BitSet sieve;

    public SieveForPrimeNumbers(int size) {
        sieve = new BitSet((size + 1) / 2);
    }

    public boolean is_composite(int k) {
        assert k >= 3 && (k % 2) == 1;
        return sieve.get((k - 3) / 2);
    }

    public void set_composite(int k) {
        assert k >= 3 && (k % 2) == 1;
        sieve.set((k - 3) / 2);
    }

    public static int largestPrime(int max)  {
        List<Integer>  primeList = sieve_of_eratosthenes(max);
        return primeList.get(primeList.size() - 1);
        
    }
    public static List<Integer> sieve_of_eratosthenes(int max) {
        SieveForPrimeNumbers sieve = new SieveForPrimeNumbers(max + 1); // +1 to include max itself
        for (int i = 3; i * i <= max; i += 2) {
            if (sieve.is_composite(i)) {
                continue;
            }

            // We increment by 2*i to skip even multiples of i
            for (int multiple_i = i * i; multiple_i <= max; multiple_i += 2 * i) {
                sieve.set_composite(multiple_i);
            }
        }


        List<Integer> primes = new ArrayList<>();
        primes.add(2);
        for (int i = 3; i <= max; i += 2) {
            if (!sieve.is_composite(i)) {
                primes.add(i);
            }
        }
        return primes;
    }

    public static void main(String[] args) {
        int max = 1;
        int num_times = 1;

        if (args.length > 0) {
            max = Integer.parseInt(args[0]);
        }

        if (args.length > 1) {
            num_times = Integer.parseInt(args[1]);
        }

        List<Integer> result = null;
        long start_time = System.currentTimeMillis();
        for (int i = 0; i < num_times; i++) {
            result = sieve_of_eratosthenes(max);
        }

        double time_in_ms = (double) (System.currentTimeMillis() - start_time) / num_times;
        double time_per_integer_ns = time_in_ms / max * 1000000;

        System.out.println("Sieved over integers 1 to " + max + " in "
                + time_in_ms + " ms (" + time_per_integer_ns + " ns per integer)");

        for (Integer i : result) {
            System.out.println(i);
        }
    }
}
