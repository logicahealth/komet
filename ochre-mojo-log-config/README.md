A project that simply carries logging configuration - include this on the classpath when you want 
this logging configuration.

Useful for mojo execution.

More importantly, do NOT include this on the classpath when you are controlling the logging configuration from elsewhere - as 
you should only have one logging implementation on the final classpath - and only one logging configuration file (log4j2.xml) 
on the classpath at runtime.