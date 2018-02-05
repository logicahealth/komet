package sh.isaac.api.statement;

public interface Interval {
    /**
     *
     * @return the lower bound for this measurement
     */
    float getLowerBound();

    /**
     *
     * @return the upper bound for this measurement
     */
    float getUpperBound();

    /**
     *
     * @return true if the lower bound is part of the interval.
     */
    boolean includeLowerBound();

    /**
     *
     * @return true if the upper bound is part of the interval.
     */
    boolean includeUpperBound();
}
