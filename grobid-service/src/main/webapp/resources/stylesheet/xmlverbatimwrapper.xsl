<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
    XML to HTML Verbatim Formatter with Syntax Highlighting
    Version 1.1

    Copyright 2002 Oliver Becker
    ob@obqo.de

    Licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software distributed
    under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
    CONDITIONS OF ANY KIND, either express or implied. See the License for the
    specific language governing permissions and limitations under the License.

    Alternatively, this software may be used under the terms of the 
    GNU Lesser General Public License (LGPL).
-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:import href="xmlverbatim.xsl" />

   <xsl:output method="html"
               doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN" />

   <!-- select the name of an element that should be formatted
        (print only these elements and their contents) -->
   <xsl:param name="select" />

   <!-- CSS Stylesheet -->
   <xsl:param name="css-stylesheet" select="'xmlverbatim.css'" />

   <!-- root -->
   <xsl:template match="/">
      <xsl:apply-templates select="/" mode="xmlverbwrapper" />
   </xsl:template>

   <xsl:template match="/" mode="xmlverbwrapper">
      <html>
         <head>
            <title>XML source view</title>
            <link rel="stylesheet" type="text/css" 
	          href="{$css-stylesheet}" />
         </head>
         <body class="xmlverb-default">
            <tt>
               <xsl:choose>
                  <!-- "select" parameter present? -->
                  <xsl:when test="$select">
                     <xsl:apply-templates mode="xmlverbwrapper" />
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:apply-templates select="." mode="xmlverb" />
                  </xsl:otherwise>
               </xsl:choose>
            </tt>
         </body>
      </html>
      <xsl:text>&#xA;</xsl:text>
   </xsl:template>

   <xsl:template match="*" mode="xmlverbwrapper">
      <xsl:choose>
         <xsl:when test="name()=$select">
            <!-- switch to render mode -->
            <!-- print indent -->
            <span class="xmlverb-text">
               <xsl:call-template name="preformatted-output">
                  <xsl:with-param name="text">
                     <xsl:call-template name="find-last-line">
                        <xsl:with-param name="text"
                              select="preceding-sibling::node()[1][self::text()]" />
                     </xsl:call-template>
                  </xsl:with-param>
               </xsl:call-template>
            </span>
            <!-- print element -->
            <xsl:apply-templates select="." mode="xmlverb" />
            <br /><br />
         </xsl:when>
         <xsl:otherwise>
            <!-- look for the selected element among the children -->
            <xsl:apply-templates select="*" mode="xmlverbwrapper" />
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!-- return the last line (after newline) in parameter $text -->
   <xsl:template name="find-last-line">
      <xsl:param name="text" />
      <xsl:choose>
         <xsl:when test="contains($text,'&#xA;')">
            <xsl:call-template name="find-last-line">
               <xsl:with-param name="text"
                    select="substring-after($text,'&#xA;')" />
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$text" />
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
