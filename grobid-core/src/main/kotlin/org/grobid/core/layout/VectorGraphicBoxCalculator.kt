package org.grobid.core.layout

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import org.apache.batik.anim.dom.SAXSVGDocumentFactory
import org.apache.batik.bridge.BridgeContext
import org.apache.batik.bridge.DocumentLoader
import org.apache.batik.bridge.UserAgent
import org.apache.batik.bridge.UserAgentAdapter
import org.apache.batik.parser.AWTPathProducer
import org.apache.batik.parser.PathParser
import org.apache.batik.util.XMLResourceDescriptor
import org.apache.commons.lang3.StringUtils
import org.grobid.core.document.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.w3c.dom.svg.SVGDocument
import org.w3c.dom.svg.SVGElement
import org.w3c.dom.svg.SVGLocatable
import java.io.File
import java.io.IOException
import java.util.*
import java.util.stream.Collectors

/**
 * Working with vector graphics
 */
object VectorGraphicBoxCalculator {
    private val LOGGER: Logger = LoggerFactory.getLogger(VectorGraphicBoxCalculator::class.java)

    const val MINIMUM_VECTOR_BOX_AREA: Int = 3000
    val VEC_GRAPHICS_FILE_SIZE_LIMIT: Int = 100 * 1024 * 1024

    @JvmStatic
    @Throws(IOException::class)
    fun calculate(document: Document): Multimap<Int?, GraphicObject?> {
        val blockMultimap = document.getBlocksPerPage()
        val result: Multimap<Int?, GraphicObject?> = LinkedHashMultimap.create<Int?, GraphicObject?>()
        val finalListOfBoxes: MutableList<BoundingBox?> = ArrayList<BoundingBox?>()

        // init BATIK stuff
        val docFactory = SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName())
        val ua: UserAgent = UserAgentAdapter()
        val loader = DocumentLoader(ua)
        val ctx = BridgeContext(ua, loader)
        ctx.setDynamicState(BridgeContext.DYNAMIC)

        for (pageNum in 1..document.getPages().size) {
            val mainPageArea = document.getPage(pageNum).mainArea

            //String q = XQueryProcessor.getQueryFromResources("vector-coords.xq");
            val vecFile =
                File(document.getDocumentSource().xmlFile.absolutePath + "_data", "image-$pageNum.svg")
            if (vecFile.exists()) {
                if (vecFile.length() > VEC_GRAPHICS_FILE_SIZE_LIMIT) {
                    LOGGER.warn("The vector file " + vecFile + " is too large to be processed, size: " + vecFile.length())
                    continue
                }

                //XQueryProcessor pr = new XQueryProcessor(vecFile);
                val doc = docFactory.createSVGDocument(vecFile.path)

                //GVTBuilder builder = new GVTBuilder();
                // GraphicsNode rootGN = builder.build(ctx, doc);
                val boxes: MutableList<BoundingBox?> = ArrayList<BoundingBox?>()

                // Get clipPath elements
                val clipPaths = doc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "clipPath")
                for (i in 0 until clipPaths.length) {
                    val clipPathElement = clipPaths.item(i) as SVGElement

                    // Analyze child elements inside the clipPath
                    val childElements = clipPathElement.childNodes
                    var aggregatedBox: BoundingBox? = null

                    // Process each child element
                    for (j in 0 until childElements.getLength()) {
                        if (childElements.item(j) is SVGElement) {
                            val child = childElements.item(j) as SVGElement

                            // For path elements, get path data and calculate bounds
                            if (child.getTagName() == "path") {
                                val pathData = child.getAttribute("d")
                                // Use Batik's PathParser to parse path data
                                val pathBox = calculatePathBounds(pathData, pageNum)

                                // Merge with aggregated box
                                if (aggregatedBox == null) {
                                    aggregatedBox = pathBox
                                } else if (pathBox != null) {
                                    aggregatedBox = aggregatedBox.boundBox(pathBox)
                                }
                            } else if (child is SVGLocatable) {
                                try {
                                    val locatable = child as SVGLocatable
                                    val rect = locatable.getBBox()
                                    if (rect != null) {
                                        val elementBox = BoundingBox.fromPointAndDimensions(
                                            pageNum,
                                            rect.getX().toDouble(),
                                            rect.getY().toDouble(),
                                            rect.getWidth().toDouble(),
                                            rect.getHeight().toDouble()
                                        )

                                        if (aggregatedBox == null) {
                                            aggregatedBox = elementBox
                                        } else {
                                            aggregatedBox = aggregatedBox.boundBox(elementBox)
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Some elements might not support getBBox
                                    LOGGER.debug("Could not get bounding box for element: " + child.getTagName(), e)
                                }
                            }
                        }
                    }

                    // Add the resulting aggregated box if valid
                    if (aggregatedBox != null && aggregatedBox.area() > 0) {
                        boxes.add(aggregatedBox)
                    }
                }

                // Sort boxed by size
                val boxesSortedBySize: MutableList<BoundingBox> = boxes.stream()
                    .distinct()
                    .sorted { b1: BoundingBox?, b2: BoundingBox? -> b2!!.area().compareTo(b1!!.area()) }
                    .collect(Collectors.toList())

                val boxesToKeep: MutableList<BoundingBox?> = ArrayList<BoundingBox?>()
                for (box in boxesSortedBySize) {
                    // Remove boxes that
                    // - are contained in other boxes or
                    // - that are outside the main area (this might not work for supplementary material)
                    if (boxes.stream()
                            .noneMatch { b: BoundingBox? ->
                                b !== box && b!!.contains(box)
                            } && (mainPageArea.contains(box) || box.calculateOutsideRatio(mainPageArea) < 0.03)
                    ) {
                        boxesToKeep.add(box)
                    }
                }

                finalListOfBoxes.addAll(boxesToKeep)
            }
        }

        // Fallback method in case we did not extract any boxes
        if (finalListOfBoxes.isEmpty()) {
            finalListOfBoxes.addAll(extractBoxesFallback(document, docFactory))
        }


        // Transformation of bounding boxes to GraphicObjects
        finalListOfBoxes.stream()
            .map { b: BoundingBox? -> GraphicObject(b, GraphicObjectType.VECTOR_BOX) }
            .forEach { go: GraphicObject? -> result.put(go!!.boundingBox.page, go) }

        return result
    }

    /**
     * Calculates the visible boundaries of a path element when clipped by a clipPath.
     *
     * @param pathData The SVG path data string for the element
     * @param clipPathData The SVG path data string for the clipping path
     * @param pageNum The current page number
     * @return BoundingBox representing the visible portion of the path, or null if invalid
     */
    private fun getVisibleBounds(pathData: String, clipPathData: String, pageNum: Int): BoundingBox? {
        // Parse path coordinates
        val pathBox = calculatePathBounds(pathData, pageNum) ?: return null

        // Parse clipPath coordinates
        val clipBox = calculatePathBounds(clipPathData, pageNum) ?: return null

        // Check if there's any intersection
        if (!pathBox.intersect(clipBox)) {
            return null
        }

        // Calculate intersection by finding max of mins and min of maxes
        return BoundingBox.fromPointAndDimensions(
            pageNum,
            Math.max(pathBox.x, clipBox.x),
            Math.max(pathBox.y, clipBox.y),
            Math.min(pathBox.x + pathBox.width, clipBox.x + clipBox.width) - Math.max(pathBox.x, clipBox.x),
            Math.min(pathBox.y + pathBox.height, clipBox.y + clipBox.height) - Math.max(pathBox.y, clipBox.y)
        )
    }

    //                 !!!!!!
// TBD: try to get simply the clip box instead of recomputing all the SVG area (because it can take ages in rare cases!)
    //                 note: getBBox on the clipBox is producing nothing because no rendering of clipBox of course, we need to find another
    //                 way to get the BB of clipPath
    //                 !!!!!!

    private fun extractBoxesFallback(
        document: Document,
        docFactory: SAXSVGDocumentFactory
    ): List<BoundingBox> {
        val boxes = mutableListOf<BoundingBox>()

        // Process each page
        for (pageNum in 1..document.getPages().size) {
            val mainPageArea = document.getPage(pageNum).mainArea

            val vecFile = File(document.getDocumentSource().xmlFile.absolutePath + "_data", "image-$pageNum.svg")
            if (!vecFile.exists() || vecFile.length() > VEC_GRAPHICS_FILE_SIZE_LIMIT) {
                continue
            }

            try {
                val svgDoc = docFactory.createSVGDocument(vecFile.path)

                // Use the existing extractBoxesFromGroupElements method
                val pageBoxes = extractBoxesFromGroupElements(svgDoc, pageNum, mainPageArea)

                // Add valid boxes to our result list
                pageBoxes.forEach { box ->
                    if (box != null && box.area() > MINIMUM_VECTOR_BOX_AREA) {
                        boxes.add(box)
                    }
                }
            } catch (e: Exception) {
                LOGGER.debug("Error processing SVG file for page $pageNum: ${e.message}")
            }
        }

        LOGGER.info("Fallback method found ${boxes.size} vector boxes")
        return boxes
    }

    /**
     * Extracts bounding boxes from SVG group elements when no boxes were detected from clipPath elements.
     *
     * @param doc The SVG document
     * @param pageNum The current page number
     * @param mainPageArea The main area of the page
     * @return List of valid bounding boxes extracted from group elements
     */
    private fun extractBoxesFromGroupElements(
        doc: SVGDocument,
        pageNum: Int,
        mainPageArea: BoundingBox
    ): MutableList<BoundingBox?> {
        val boxes: MutableList<BoundingBox?> = ArrayList<BoundingBox?>()

        val nodeList = doc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "g")
        if (nodeList.length == 0) {
            return boxes
        }

        // Check if all groups are white cache elements
        var allDummyCache = true
        for (i in 0 until nodeList.length) {
            val item = nodeList.item(i) as SVGElement
            if (!isDummyCacheSVG(item)) {
                allDummyCache = false
                break
            }
        }

        // Skip if all groups are just white cache elements
        if (allDummyCache) {
            LOGGER.debug("Page {}: SVG document contains only white cache elements, skipping", pageNum)
            return boxes
        }

        // Process each group element
        for (i in 0 until nodeList.getLength()) {
            try {
                val item = nodeList.item(i) as SVGElement?
                val locatable = item as SVGLocatable
                val rect = locatable.getBBox()

                if (rect == null) {
                    continue
                }

                val box = BoundingBox.fromPointAndDimensions(
                    pageNum,
                    rect.getX().toDouble(),
                    rect.getY().toDouble(),
                    rect.getWidth().toDouble(),
                    rect.getHeight().toDouble()
                )

                // Filter out invalid boxes: outside main area, zero area, or too large
                if (!mainPageArea.contains(box) || box.area() == 0.0 || box.area() / mainPageArea.area() > 0.7) {
                    continue
                }

                boxes.add(box)
            } catch (e: Exception) {
                LOGGER.debug("Error processing SVG group element: {}", e.message)
            }
        }

        // Filter out boxes that are just lines (height or width < 1)
        return boxes.stream()
            .filter { box: BoundingBox? -> box!!.getHeight() >= 1 && box.getWidth() >= 1 }
            .collect(Collectors.toList())
    }

    /**
     * Calculates the bounding box for a given SVG path data.
     *
     * @param pathData The SVG path data string
     * @param pageNum The current page number
     * @return BoundingBox if the path is valid, null otherwise
     */
    private fun calculatePathBounds(pathData: String?, pageNum: Int): BoundingBox? {
        if (pathData == null || pathData.trim { it <= ' ' }.isEmpty()) {
            return null
        }

        try {
            // Create a path producer to convert SVG path to a Java2D shape
            val pathProducer = AWTPathProducer()

            val pathParser = PathParser()
            pathParser.setPathHandler(pathProducer)
            pathParser.parse(pathData)

            val shape = pathProducer.getShape()
            val bounds = shape.getBounds2D()

            if (bounds.getWidth() > 0 && bounds.getHeight() > 0) {
                return BoundingBox.fromPointAndDimensions(
                    pageNum,
                    bounds.getX(),
                    bounds.getY(),
                    bounds.getWidth(),
                    bounds.getHeight()
                )
            }
        } catch (e: Exception) {
            LOGGER.debug("Could not parse SVG path data: {}", pathData, e)
        }

        return null
    }

    /**
     * Determines if an SVG element is a "dummy cache" element.
     *
     *
     * Dummy cache elements are typically white filling areas used for aesthetics
     * rather than actual graphics content. These elements usually have a style
     * attribute with "fill: #FFFFFF" or equivalent white color values.
     *
     * @param element The SVG element to check
     * @return true if the element is determined to be a dummy cache element
     */
    fun isDummyCacheSVG(element: SVGElement): Boolean {
        val styleValue = element.getAttribute("style")
        if (StringUtils.isBlank(styleValue)) {
            return false
        }

        for (attributePair in styleValue.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            var attributePair = attributePair
            attributePair = attributePair.trim { it <= ' ' }
            val separatorIndex = attributePair.indexOf(":")

            if (separatorIndex != -1) {
                val name = attributePair.substring(0, separatorIndex).trim { it <= ' ' }.lowercase(Locale.getDefault())
                val value =
                    attributePair.substring(separatorIndex + 1).trim { it <= ' ' }.lowercase(Locale.getDefault())

                if ("fill" == name &&
                    ("#ffffff" == value || "#fff" == value || "white" == value)
                ) {
                    return true
                }
            }
        }

        return false
    }

    /**
     * Merge bounding boxes in case of intersection
     */
    @JvmStatic
    fun mergeBoxes(boxes: MutableList<BoundingBox?>?): MutableList<BoundingBox?> {
        if (boxes == null || boxes.isEmpty()) {
            return ArrayList<BoundingBox?>()
        }

        // Create a copy to avoid modifying the input list
        val workingCopy: MutableList<BoundingBox?> = ArrayList<BoundingBox?>(boxes)

        var mergeOccurred: Boolean
        do {
            mergeOccurred = false
            for (i in workingCopy.indices) {
                val current = workingCopy.get(i)
                if (current == null) continue

                for (j in i + 1 until workingCopy.size) {
                    val other = workingCopy.get(j)
                    if (other == null) continue

                    if (current.intersect(other)) {
                        // Merge the boxes
                        workingCopy.set(i, current.boundBox(other))
                        workingCopy.set(j, null)
                        mergeOccurred = true
                    }
                }
            }
        } while (mergeOccurred)

        // Filter out null values and return
        return workingCopy.stream()
            .filter { obj: BoundingBox? -> Objects.nonNull(obj) }
            .collect(Collectors.toList())
    }

    /**
     * Merge bounding boxes in case of close proximity defined by a max proximity distance
     */
    @JvmStatic
    fun glueBoxes(boxes: MutableList<BoundingBox?>?, maxProximityDistance: kotlin.Double): MutableList<BoundingBox?> {
        if (boxes == null || boxes.isEmpty()) {
            return ArrayList<BoundingBox?>()
        }

        // Create a working copy to avoid modifying the input list
        val workingCopy: MutableList<BoundingBox?> = ArrayList<BoundingBox?>(boxes)

        var mergeOccurred: Boolean
        do {
            mergeOccurred = false
            for (i in workingCopy.indices) {
                val current = workingCopy.get(i)
                if (current == null) continue

                for (j in i + 1 until workingCopy.size) {
                    val other = workingCopy.get(j)
                    if (other == null) continue

                    if (current.distanceTo(other) < maxProximityDistance) {
                        // Merge boxes that are within the proximity distance
                        workingCopy.set(i, current.boundBox(other))
                        workingCopy.set(j, null)
                        mergeOccurred = true
                    }
                }
            }
        } while (mergeOccurred)

        // Filter out null values and boxes that are too small
        return workingCopy.stream()
            .filter { box: BoundingBox? -> box != null && box.getHeight() >= 5 && box.getWidth() >= 5 }
            .collect(Collectors.toList())
    }
}
