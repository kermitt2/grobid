<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:m="http://www.mendeley.com/ns/"
    exclude-result-prefixes="xs m"
    version="2.0">

    <xsl:strip-space elements="page textbox textline"/>
    
    <xsl:output indent="no" />
    
    <!-- Returns a list of coords: x, y, width, height -->
    <xsl:function name="m:getBoundingBox" as="xs:double*">
        <xsl:param name="pagewidth" as="xs:double" />
        <xsl:param name="pageheight" as="xs:double" />
        <xsl:param name="coords" as="xs:string" />
        <xsl:analyze-string select="$coords" regex="^([^,]+),([^,]+),([^,]+),([^,]+)$">
            <xsl:matching-substring>
                <!--  height -->
                <xsl:variable name="height" select="number(regex-group(4)) - number(regex-group(2))" />
                <!-- x coord -->
                <xsl:value-of select="format-number(number(regex-group(1)), '#.00')" />
                <!-- y coords seem to be inverted with pdfminer -->
                <xsl:value-of select="format-number($pageheight - number(regex-group(4)) - $height, '#.00')" />
                <!--  width -->
                <xsl:value-of select="format-number(number(regex-group(3)) - number(regex-group(1)), '#.00')" />
                <!--  height -->
                <xsl:value-of select="format-number($height, '#.00')" />
            </xsl:matching-substring>
        </xsl:analyze-string>
    </xsl:function>
    
    
    <xsl:template match="* |@*" />
    
    <xsl:template match="/">
        <DOCUMENT>
            <xsl:apply-templates />
        </DOCUMENT>
    </xsl:template>
    
    <xsl:template match="pages">
        <xsl:apply-templates />
    </xsl:template>
    
    
    <xsl:template match="page">
        <xsl:variable name="bbox" select="m:getBoundingBox(0, 0, @bbox)" />
        <xsl:variable name="width" select="$bbox[3]" />
        <xsl:variable name="height" select="$bbox[4]" />
        <PAGE number="{@id}" id="p{@id}" width="{$width}" height="{$height}">
            <xsl:apply-templates>
                <xsl:with-param name="pid" select="@id" tunnel="yes" />
                <xsl:with-param name="page-width" select="$width" tunnel="yes" />
                <xsl:with-param name="page-height" select="$height" tunnel="yes" />
<!--                <xsl:with-param name="text-counter" select="count(preceding-sibling::page/textbox/textline/text[not(normalize-space())])" tunnel="yes" />-->
            </xsl:apply-templates>
        </PAGE>
    </xsl:template>
    
    <xsl:template match="textbox[normalize-space(string-join(textline/text, ''))]">
        <xsl:param name="pid" tunnel="yes"/>
        <BLOCK id="p{$pid}_b{@id}">
            <xsl:apply-templates />
        </BLOCK>
    </xsl:template>
    
    <!-- For the purposes of getting the header and body text, just get text that occurs in a texbox. Orphaned textlines (e.g. in a figure) not in a textbox are excluded -->
    <xsl:template match="textbox/textline[normalize-space(string-join(text, ''))]">
        <xsl:param name="pid" tunnel="yes"/>
        <xsl:param name="page-width" tunnel="yes"/>
        <xsl:param name="page-height" tunnel="yes"/>
        <xsl:param name="text-counter" tunnel="yes"/>
        <xsl:variable name="bbox" select="m:getBoundingBox($page-width, $page-height, @bbox)" />
        <TEXT x="{$bbox[1]}" y="{$bbox[2]}" width="{$bbox[3]}" height="{$bbox[4]}" id="p{$pid}_t{count(preceding-sibling::textline) + count(../preceding-sibling::*/textline) + 1}">
            <xsl:for-each-group select="text" group-adjacent="normalize-space() != ''" >
                <xsl:if test="normalize-space()">
                    <xsl:variable name="bbox" select="m:getBoundingBox($page-width, $page-height, @bbox)" />
                    <xsl:variable name="total-width" select="format-number(sum(for $i in current-group()/@bbox return m:getBoundingBox($page-width, $page-height, $i)[3]), '#.00')" />
                    <xsl:variable name="x" select="$bbox[1]" />
                    <xsl:variable name="y" select="$bbox[2]" />
                    <xsl:variable name="height" select="$bbox[4]" />
                    <xsl:variable name="base" select="format-number($y + $height, '#.00')" />
                    <!--<xsl:variable name="wid" select="count(preceding-sibling::text[not(normalize-space())]) + count(../preceding-sibling::*/text[not(normalize-space())]) + count(../../preceding-sibling::*/*/text[not(normalize-space())]) + 1" />
                    <xsl:variable name="sid" select="$wid + $text-counter" />-->
                    <xsl:variable name="wid" select="generate-id()" />
                    <xsl:variable name="sid" select="generate-id()" />
                    <TOKEN x="{$x}" y="{$y}" width="{$total-width}" height="{$height}" base="{$base}" sid="p{$pid}_s{$sid}" id="p{$pid}_w{$wid}">
                        <xsl:apply-templates select="current-group()/@*" />
                        <xsl:apply-templates select="current-group()" />
                    </TOKEN>
                </xsl:if>
            </xsl:for-each-group>
        </TEXT>
    </xsl:template>
    
    <xsl:template match="text/@size">
        <xsl:attribute name="font-size" select="round(number(.))" />
    </xsl:template>
    
    <xsl:template match="text/@font[lower-case(.)='unknown']" priority="1" />
    
    <xsl:template match="text/@font">
        <xsl:attribute name="font-name" select="." />
        <xsl:attribute name="bold" select="if (matches(., '[,;\-]Bold', 'i')) then 'yes' else 'no' " />
        <xsl:attribute name="italic" select="if (matches(., '[,;\-d]Italic', 'i')) then 'yes' else 'no' " />
    </xsl:template>
    
    
    <xsl:template match="textline/text[normalize-space()]">
        <xsl:value-of select="." />
        <xsl:value-of select="following-sibling::*[1][self::text][not(normalize-space())]" />
    </xsl:template>
    
    
</xsl:stylesheet>