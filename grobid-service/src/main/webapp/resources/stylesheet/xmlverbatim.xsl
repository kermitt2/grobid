<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
    XML to HTML Verbatim Formatter with Syntax Highlighting
    Version 1.1
    Contributors: Doug Dicks, added auto-indent (parameter indent-elements)
                  for pretty-print

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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:verb="http://informatik.hu-berlin.de/xmlverbatim"
                exclude-result-prefixes="verb">

   <xsl:output method="html" omit-xml-declaration="yes" indent="no"/>

   <xsl:param name="indent-elements" select="false()" />

   <xsl:template match="/">
      <xsl:apply-templates select="." mode="xmlverb" />
   </xsl:template>

   <!-- root -->
   <xsl:template match="/" mode="xmlverb">
      <xsl:text>&#xA;</xsl:text>
      <xsl:comment>
         <xsl:text> converted by xmlverbatim.xsl 1.1, (c) O. Becker </xsl:text>
      </xsl:comment>
      <xsl:text>&#xA;</xsl:text>
      <div class="xmlverb-default">
         <xsl:apply-templates mode="xmlverb">
            <xsl:with-param name="indent-elements" select="$indent-elements" />
         </xsl:apply-templates>
      </div>
      <xsl:text>&#xA;</xsl:text>
   </xsl:template>

   <!-- wrapper -->
   <xsl:template match="verb:wrapper">
      <xsl:apply-templates mode="xmlverb">
         <xsl:with-param name="indent-elements" select="$indent-elements" />
      </xsl:apply-templates>
   </xsl:template>

   <xsl:template match="verb:wrapper" mode="xmlverb">
      <xsl:apply-templates mode="xmlverb">
         <xsl:with-param name="indent-elements" select="$indent-elements" />
      </xsl:apply-templates>
   </xsl:template>

   <!-- element nodes -->
   <xsl:template match="*" mode="xmlverb">
      <xsl:param name="indent-elements" select="false()" />
      <xsl:param name="indent" select="''" />
      <xsl:param name="indent-increment" select="'&#xA0;&#xA0;&#xA0;'" />
      <xsl:if test="$indent-elements">
         <br/>
         <xsl:value-of select="$indent" />
      </xsl:if>
      <xsl:text>&lt;</xsl:text>
      <xsl:variable name="ns-prefix"
                    select="substring-before(name(),':')" />
      <xsl:if test="$ns-prefix != ''">
         <span class="xmlverb-element-nsprefix">
            <xsl:value-of select="$ns-prefix"/>
         </span>
         <xsl:text>:</xsl:text>
      </xsl:if>
      <span class="xmlverb-element-name">
         <xsl:value-of select="local-name()"/>
      </span>
      <xsl:variable name="pns" select="../namespace::*"/>
      <xsl:if test="$pns[name()=''] and not(namespace::*[name()=''])">
         <span class="xmlverb-ns-name">
            <xsl:text> xmlns</xsl:text>
         </span>
         <xsl:text>=&quot;&quot;</xsl:text>
      </xsl:if>
      <xsl:for-each select="namespace::*">
         <xsl:if test="not($pns[name()=name(current()) and 
                           .=current()])">
            <xsl:call-template name="xmlverb-ns" />
         </xsl:if>
      </xsl:for-each>
      <xsl:for-each select="@*">
         <xsl:call-template name="xmlverb-attrs" />
      </xsl:for-each>
      <xsl:choose>
         <xsl:when test="node()">
            <xsl:text>&gt;</xsl:text>
            <xsl:apply-templates mode="xmlverb">
              <xsl:with-param name="indent-elements"
                              select="$indent-elements"/>
              <xsl:with-param name="indent"
                              select="concat($indent, $indent-increment)"/>
              <xsl:with-param name="indent-increment"
                              select="$indent-increment"/>
            </xsl:apply-templates>
            <xsl:if test="* and $indent-elements">
               <br/>
               <xsl:value-of select="$indent" />
            </xsl:if>
            <xsl:text>&lt;/</xsl:text>
            <xsl:if test="$ns-prefix != ''">
               <span class="xmlverb-element-nsprefix">
                  <xsl:value-of select="$ns-prefix"/>
               </span>
               <xsl:text>:</xsl:text>
            </xsl:if>
            <span class="xmlverb-element-name">
               <xsl:value-of select="local-name()"/>
            </span>
            <xsl:text>&gt;</xsl:text>
         </xsl:when>
         <xsl:otherwise>
            <xsl:text> /&gt;</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="not(parent::*)"><br /><xsl:text>&#xA;</xsl:text></xsl:if>
   </xsl:template>

   <!-- attribute nodes -->
   <xsl:template name="xmlverb-attrs">
      <xsl:text> </xsl:text>
      <span class="xmlverb-attr-name">
         <xsl:value-of select="name()"/>
      </span>
      <xsl:text>=&quot;</xsl:text>
      <span class="xmlverb-attr-content">
         <xsl:call-template name="html-replace-entities">
            <xsl:with-param name="text" select="normalize-space(.)" />
            <xsl:with-param name="attrs" select="true()" />
         </xsl:call-template>
      </span>
      <xsl:text>&quot;</xsl:text>
   </xsl:template>

   <!-- namespace nodes -->
   <xsl:template name="xmlverb-ns">
      <xsl:if test="name()!='xml'">
         <span class="xmlverb-ns-name">
            <xsl:text> xmlns</xsl:text>
            <xsl:if test="name()!=''">
               <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:value-of select="name()"/>
         </span>
         <xsl:text>=&quot;</xsl:text>
         <span class="xmlverb-ns-uri">
            <xsl:value-of select="."/>
         </span>
         <xsl:text>&quot;</xsl:text>
      </xsl:if>
   </xsl:template>

   <!-- text nodes -->
   <xsl:template match="text()" mode="xmlverb">
      <span class="xmlverb-text">
         <xsl:call-template name="preformatted-output">
            <xsl:with-param name="text">
               <xsl:call-template name="html-replace-entities">
                  <xsl:with-param name="text" select="." />
               </xsl:call-template>
            </xsl:with-param>
         </xsl:call-template>
      </span>
   </xsl:template>

   <!-- comments -->
   <xsl:template match="comment()" mode="xmlverb">
      <xsl:text>&lt;!--</xsl:text>
      <span class="xmlverb-comment">
         <xsl:call-template name="preformatted-output">
            <xsl:with-param name="text" select="." />
         </xsl:call-template>
      </span>
      <xsl:text>--&gt;</xsl:text>
      <xsl:if test="not(parent::*)"><br /><xsl:text>&#xA;</xsl:text></xsl:if>
   </xsl:template>

   <!-- processing instructions -->
   <xsl:template match="processing-instruction()" mode="xmlverb">
      <xsl:text>&lt;?</xsl:text>
      <span class="xmlverb-pi-name">
         <xsl:value-of select="name()"/>
      </span>
      <xsl:if test=".!=''">
         <xsl:text> </xsl:text>
         <span class="xmlverb-pi-content">
            <xsl:value-of select="."/>
         </span>
      </xsl:if>
      <xsl:text>?&gt;</xsl:text>
      <xsl:if test="not(parent::*)"><br /><xsl:text>&#xA;</xsl:text></xsl:if>
   </xsl:template>


   <!-- =========================================================== -->
   <!--                    Procedures / Functions                   -->
   <!-- =========================================================== -->

   <!-- generate entities by replacing &, ", < and > in $text -->
   <xsl:template name="html-replace-entities">
      <xsl:param name="text" />
      <xsl:param name="attrs" />
      <xsl:variable name="tmp">
         <xsl:call-template name="replace-substring">
            <xsl:with-param name="from" select="'&gt;'" />
            <xsl:with-param name="to" select="'&amp;gt;'" />
            <xsl:with-param name="value">
               <xsl:call-template name="replace-substring">
                  <xsl:with-param name="from" select="'&lt;'" />
                  <xsl:with-param name="to" select="'&amp;lt;'" />
                  <xsl:with-param name="value">
                     <xsl:call-template name="replace-substring">
                        <xsl:with-param name="from" 
                                        select="'&amp;'" />
                        <xsl:with-param name="to" 
                                        select="'&amp;amp;'" />
                        <xsl:with-param name="value" 
                                        select="$text" />
                     </xsl:call-template>
                  </xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
         </xsl:call-template>
      </xsl:variable>
      <xsl:choose>
         <!-- $text is an attribute value -->
         <xsl:when test="$attrs">
            <xsl:call-template name="replace-substring">
               <xsl:with-param name="from" select="'&#xA;'" />
               <xsl:with-param name="to" select="'&amp;#xA;'" />
               <xsl:with-param name="value">
                  <xsl:call-template name="replace-substring">
                     <xsl:with-param name="from" 
                                     select="'&quot;'" />
                     <xsl:with-param name="to" 
                                     select="'&amp;quot;'" />
                     <xsl:with-param name="value" select="$tmp" />
                  </xsl:call-template>
               </xsl:with-param>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$tmp" />
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!-- replace in $value substring $from with $to -->
   <xsl:template name="replace-substring">
      <xsl:param name="value" />
      <xsl:param name="from" />
      <xsl:param name="to" />
      <xsl:choose>
         <xsl:when test="contains($value,$from)">
            <xsl:value-of select="substring-before($value,$from)" />
            <xsl:value-of select="$to" />
            <xsl:call-template name="replace-substring">
               <xsl:with-param name="value" 
                               select="substring-after($value,$from)" />
               <xsl:with-param name="from" select="$from" />
               <xsl:with-param name="to" select="$to" />
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$value" />
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!-- preformatted output: space as &nbsp;, tab as 8 &nbsp;
                             nl as <br> -->
   <xsl:template name="preformatted-output">
      <xsl:param name="text" />
      <xsl:call-template name="output-nl">
         <xsl:with-param name="text">
            <xsl:call-template name="replace-substring">
               <xsl:with-param name="value"
                               select="translate($text,' ','&#xA0;')" />
               <xsl:with-param name="from" select="'&#9;'" />
               <xsl:with-param name="to" 
                               select="'&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;&#xA0;'" />
            </xsl:call-template>
         </xsl:with-param>
      </xsl:call-template>
   </xsl:template>

   <!-- output nl as <br> -->
   <xsl:template name="output-nl">
      <xsl:param name="text" />
      <xsl:choose>
         <xsl:when test="contains($text,'&#xA;')">
            <xsl:value-of select="substring-before($text,'&#xA;')" />
            <br />
            <xsl:text>&#xA;</xsl:text>
            <xsl:call-template name="output-nl">
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
