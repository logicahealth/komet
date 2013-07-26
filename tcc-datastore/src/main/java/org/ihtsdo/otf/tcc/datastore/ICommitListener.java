/**
 * 14 Sep 2010 $Name$ $Revision$
 */
package org.ihtsdo.otf.tcc.datastore;

/**
 * <p>
 * Represent a commit event listener: will receive events from the commit
 * manager
 * </p>
 * @author mafe2
 */
public interface ICommitListener {

    /**
     * <p>
     * fire the commit event
     * </p>
     * @param event
     * @throws Exception if something goes wrong
     */
    public abstract void afterCommit(final CommitEvent event) throws Exception;

    /**
     * <p>
     * Invoked on commit manager shutdown
     * </p>
     * @throws Exception
     */
    public abstract void shutdown() throws Exception;
}
