/**
 * 14 Sep 2010 $Name$ $Revision$
 */
package org.ihtsdo.otf.tcc.datastore;

/**
 * <p>
 * </p>
 * @author mafe2
 */
public class CommitEvent {

    /**
     * <p>
     * event data
     * </p>
     */
    private final Object data;

    /**
     * <p>
     * Create a new instance with the event data (may be null)
     * </p>
     * @param data
     */
    public CommitEvent(final Object data) {
        super();
        this.data = data;
    }

    /**
     * <p>
     * data.
     * </p>
     * @return data.
     */
    public Object getData() {
        return this.data;
    }

}
