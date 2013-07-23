package org.ihtsdo.ttk.lookup;

//~--- non-JDK imports --------------------------------------------------------

import java.util.Collection;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collections;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        String                  testObject  = "Test Object";
        String                  testObject2 = "Test Object 2";
        InstanceWrapper<String> instance    = new InstanceWrapper(testObject, "An ID", "A name",
                                                  Collections.singletonList("a property"));

        Looker.addPair(instance);
        String result = Looker.lookup(String.class);
        System.out.println("Found: " + result);

        instance = new InstanceWrapper(testObject2, "An ID 2", "A name 2", 
                Collections.singletonList("a property"));
        Looker.addPair(instance);

        result = Looker.lookup(String.class);
        
        System.out.println("Found: " + result);
        
        Collection<String> results = (Collection<String>) Looker.lookupAll(String.class);
        
        System.out.println("Found: " + results);
        
        Result<String> resultResult = Looker.lookupResult(String.class);
        
        System.out.println("Found result: " + resultResult);
        System.out.println("Found result: " + resultResult.allItems());
        
        Lookup.Template lt = new Lookup.Template<>(String.class, "An ID 2", null);
        
        Lookup.Item resultItem = Looker.lookupItem(lt);

        System.out.println("Found resultItem: " + resultItem);
        
        
       lt = new Lookup.Template<>(String.class, null, null);
       Lookup.Result lookupResult = Looker.lookup(lt);
       
       System.out.println("Found lookupResult: " + lookupResult.allItems());

        
     }
}
