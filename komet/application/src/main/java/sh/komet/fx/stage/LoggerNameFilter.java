/*
 * Copyright 2020 Mind Computing Inc, Sagebits LLC
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
package sh.komet.fx.stage;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;

/**
 * Custom Log4j Filter to quiet down logging per logger.
 * Use like:
 * <LoggerNameFilter name="net.sagebits.HK2Utilities.ClassInfo" onMatch="DENY" onMismatch="NEUTRAL"/>
 * This does a "startsWith" match, so you can match all loggers in a package, or a specific package
 * @author <a href="mailto:daniel.armbrust.list@sagebits.net">Dan Armbrust</a>
 */
@Plugin(name = "LoggerNameFilter", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
@PerformanceSensitive("allocation")
public final class LoggerNameFilter extends AbstractFilter {

	public static final String ATTR_MATCH = "name";
    private final String value;
    
    private LoggerNameFilter(final String name, final Result onMatch, final Result onMismatch) {
        super(onMatch, onMismatch);
        this.value = name;
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object... params) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg,
                         final Throwable t) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg,
                         final Throwable t) {
        return filter(logger);
    }

    @Override
    public Result filter(final LogEvent event) {
        return event.getLoggerName().startsWith(this.value) ? onMatch : onMismatch;
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4, final Object p5) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4, final Object p5, final Object p6) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4, final Object p5, final Object p6,
                         final Object p7) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4, final Object p5, final Object p6,
                         final Object p7, final Object p8) {
        return filter(logger);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object p0, final Object p1, final Object p2, final Object p3,
                         final Object p4, final Object p5, final Object p6,
                         final Object p7, final Object p8, final Object p9) {
        return filter(logger);
    }

    private Result filter(final Logger logger) {
        return logger.getName().startsWith(this.value) ? onMatch : onMismatch;
    }


    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder extends AbstractFilterBuilder<Builder> implements org.apache.logging.log4j.core.util.Builder<LoggerNameFilter> {

        @PluginBuilderAttribute
        private String name = "";

        /**
         * @param name the name of the logger to match on
         * @return this
         */
        public LoggerNameFilter.Builder setMatchString(final String name) {
            this.name = name;
            return this;
        }
        @Override
        public LoggerNameFilter build() {
            return new LoggerNameFilter(this.name, this.getOnMatch(), this.getOnMismatch());
        }
    }
}

