<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ext="http://xslt.solor.io">
    <xsl:output indent="yes"/>

    <xsl:template match="/">
        <root>
            <xsl:value-of select="ext:test()" />
        </root>
    </xsl:template>
</xsl:stylesheet>