package sh.isaac.api.component.semantic;

import java.util.List;

import org.jvnet.hk2.annotations.Contract;

import sh.isaac.api.chronicle.Chronology;
import sh.isaac.api.commit.ChangeCheckerMode;
import sh.isaac.api.component.semantic.version.SemanticVersion;
import sh.isaac.api.coordinate.EditCoordinate;

/**
 * {@link SemanticBuildListenerI}
 *
 * An interface that allows implementers to easily mark their listener as something that will register 
 * itself as a class that does things on build of a semantic.
 * 
 * The only intent of this interface is to provide a global handle to all modules which are registered to do 
 * things - so that then can be enabled or disabled individually, or across the board prior to performing 
 * programmatic operations where you don't want the listeners firing.
 * 
 * =====================================
 * Implementers of this class MUST use a @Named annotation:
 * <code>@Named (value="the name")</code>
 * =====================================
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a> 
 */

@Contract
public interface SemanticBuildListenerI
{
   /**
    * Returns the HK2 service name of this listener.  This should return the same string as the 
    * <code>@Named (value="the name")</code>	
    * annotation on the implementation of the class.
     * @return 
    */
   public String getListenerName();
   
   /**
    * Tell the listener that it MAY enable itself (it does not have to).  A listener may be primarily controlled
    * by a user preference, for example.  While {@link #disable()} overrides the user preferences, and orders the 
    * listener disabled - this call only informs the user that it is no longer being overridden - and may return to 
    * the enabled state if that was the users preference.
    */
   public void enable();
   
   /**
    * Tell the listener to disable itself - either by unregistering upstream, or ignoring all commit related events 
    * until {@link #enable()} is called.
    */
   public void disable();

   
   /**
    * Query whether or not the listener is enabled
    * @return boolean indicating whether or not the listener is enabled
    */
   public boolean isEnabled();

   /**
    * The caller is responsible to write the component to the proper store when 
    * all updates to the component are complete. 
    * @param stampSequence
    * @param builtObjects a list objects build as a result of call build. 
    * Includes top-level object being built. 
    * The caller is also responsible to write all build objects to the proper store. 
    */
   default void applyBefore(int stampSequence, List<Chronology> builtObjects) {}


   /**
    * The caller is responsible to write the component to the proper store when 
    * all updates to the component are complete. 
    * @param stampSequence
    * @param builtSemantic semantic built as a result of building this object
    * @param builtObjects a list objects build as a result of call build. 
    * Includes top-level object being built. 
    * The caller is also responsible to write all build objects to the proper store. 
    */
   default void applyAfter(int stampSequence, SemanticVersion builtSemantic, List<Chronology> builtObjects) {}

   /**
    * A listener method that applies to a SemanticBuilder before building a component with a state of ACTIVE. 
    * @param editCoordinate the edit coordinate that determines the author, module and path for the change
    * @param changeCheckerMode determines if added to the commit manager with or without checks.
     * @param builtObjects
    */
   default public void applyBefore(
         EditCoordinate editCoordinate, 
         ChangeCheckerMode changeCheckerMode,
         List<Chronology> builtObjects) {}

   /**
    * A listener method that applies to a SemanticBuilder after building a component with a state of ACTIVE. 
    * @param editCoordinate the edit coordinate that determines the author, module and path for the change
    * @param changeCheckerMode determines if added to the commit manager with or without checks. 
     * @param builtSemanticVersion 
     * @param builtObjects 
    */
   default public void applyAfter(
         EditCoordinate editCoordinate, 
         ChangeCheckerMode changeCheckerMode,
         SemanticVersion builtSemanticVersion,
         List<Chronology> builtObjects) {}
}
