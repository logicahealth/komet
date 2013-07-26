package org.ihtsdo.otf.tcc.api.conflict;

//~--- non-JDK imports --------------------------------------------------------

import java.util.ArrayList;
import java.util.List;
import org.ihtsdo.otf.tcc.api.chronicle.ComponentVersionBI;

public class EditPathWinsStrategy extends ContradictionManagementStrategy {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- methods -------------------------------------------------------------


   @Override
   public <T extends ComponentVersionBI> List<T> resolveVersions(List<T> versions) {
      List<T> returnValues = new ArrayList<>(2);

      for (T v : versions) {
         if (ec.getEditPaths().contains(v.getPathNid())) {
            returnValues.add(v);
         }
      }

      if (returnValues.isEmpty()) {
         for (T part : versions) {
            if (part.isBaselineGeneration()) {
               returnValues.add(part);
            }
         }
      }

      return returnValues;
   }

   @Override
   public <T extends ComponentVersionBI> List<T> resolveVersions(T part1, T part2) {
       assert part1 != null;
       assert part2 != null;
       assert ec != null;
       
      List<T> returnValues = new ArrayList<>(2);

      if (ec.getEditPaths().contains(part1.getPathNid())) {
         returnValues.add(part1);
      }

      if (ec.getEditPaths().contains(part2.getPathNid())) {
         returnValues.add(part2);
      }

      if (returnValues.isEmpty()) {
         if (part1.isBaselineGeneration()) {
            returnValues.add(part1);
         }

         if (part2.isBaselineGeneration()) {
            returnValues.add(part2);
         }
      }

      return returnValues;
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public String getDescription() {
      return "<html>This resolution strategy implements resolution that"
             + "<li>suppresses the members that are NOT on the edit path(s) from </li>"
             + "<li>participating in the potential contradiction.</ul>" + "</html>";
   }

   @Override
   public String getDisplayName() {
      return "Suppress versions NOT on a edit path from contradictions";
   }
}
