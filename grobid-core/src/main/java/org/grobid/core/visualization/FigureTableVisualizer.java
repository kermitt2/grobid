package org.grobid.core.visualization;

import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.grobid.core.data.Figure;
import org.grobid.core.data.Table;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.BoundingBoxCalculator;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.PathUtil;
import org.grobid.core.utilities.XQueryProcessor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.grobid.core.utilities.PathUtil.getOneFile;

/**
 * Created by zholudev on 18/01/16.
 * Visualize figures and tables
 */
public class FigureTableVisualizer {

    private static Set<Integer> blacklistedPages;
    private static File inputPdf;
    private static boolean annotated;
    private static boolean annotatedFigure;
    static boolean singleFile = true;


    public static void main(String[] args) {
        try {
//            File input = new File("/Work/temp/context/coords/1.pdf");
//            File input = new File("/Work/temp/figureExtraction/newtest/1.pdf");
//            File input = new File("/Work/temp/figureExtraction/newtest/2.pdf");
//            File input = new File("/Work/temp/figureExtraction/5.pdf");
//            File input = new File("/Work/temp/figureExtraction/vector/6.pdf");

//            File input = new File("/Users/zholudev/Downloads/AS-134079286616064@1408978401811_content_1.pdf");
            File input = new File("/Users/zholudev/Downloads/AS-411011216625664@1475004118387_content_1.pdf");

//
//
// File input = new File("/Users/zholudev/Downloads/AS-322050973995010@1453794344041_content_1.pdf"); //separate blocks for 1 caption
//            File input = new File("/Users/zholudev/Downloads/AS-327630265044992@1455124550118_content_1.pdf");
//            File input = new File("/Work/temp/images/pdf_image_extraction_results/Synaptotagmin 11 interacts with components of the RNA-induced (2)/Synaptotagmin 11 interacts with components of the RNA-induced (2).pdf"); //double caption attached

//            File input = new File("/Work/temp/images/pdf_image_extraction_results/Synaptotagmin 11 interacts with components of the RNA-induced (2)/Synaptotagmin 11 interacts with components of the RNA-induced (2).pdf");


//            File input = new File("/Users/zholudev/Downloads/AS-102952320634884@1401557154467_content_1.pdf");

//            File input = new File("/Users/zholudev/Downloads/AS-328608011833344@1455357663088_content_1.pdf");


//            File input = new File("/Users/zholudev/Downloads/fail.pdf");

            // TABLES
//            File input = new File("/Users/zholudev/Downloads/AS-301642189688834@1448928510544_content_1.pdf");
//            File input = new File("/Users/zholudev/Downloads/710973.pdf"); //big fancy tables
//            File input = new File("/Users/zholudev/Downloads/AS-345110018576384@1459292048595_content_1.pdf"); // lots of stuff annotatted
//            File input = new File("/Users/zholudev/Downloads/AS-351355282706432@1460781035600_content_1.pdf"); // 5 tables
//            File input = new File("/Users/zholudev/Downloads/ICCCI 2014 - SciRecSys (1).pdf"); //

//            File input = new File("//Work/temp/context/1000k/AS_97456738013192_1400246905951.pdf"); // some captions take too much
//            File input = new File("//Work/temp/context/1000k/AS_97469140570124_1400249863029.pdf"); // some incorrect distribution between captions and content
//            File input = new File("//Work/temp/context/1000k/AS_97469983625226_1400250064243.pdf"); // one table takes too much
//            File input = new File("//Work/temp/context/1000k/AS_97492947439622_1400255538259.pdf"); //


            //-------------

                    //new chunk
            // AS_101470770827287_1401203928712.pdf - weird table caption



            // END - TABLES
//
//
// File input = new File("/Users/zholudev/Downloads/AS-334144056905730@1456677559953_content_1.pdf");
//
//
//            File input = new File("/Work/temp/figureExtraction/1.pdf");


//            File input = new File("/Work/temp/context/1000k/AS_101478173773832_1401205691162.pdf");
//            File input = new File("/Work/temp/context/1000k/AS_103455624531988_1401677151824.pdf");

//            File input = new File("/Users/zholudev/Downloads/1603.02478v1.pdf");

            // VECTOR
//            File input = new File("/Work/temp/figureExtraction/vector/6.pdf");

//            File input = new File("//Users/zholudev/Downloads/AS-324757835190273@1454439709828_content_1.pdf"); // // glued images
//            File input = new File("//Users/zholudev/Downloads/AS-104148838125570@1401842426392_content_1.pdf"); // 3 column

//

//            File input = new File("//Work/temp/context/1000k/AS_103486037430289_1401684402827.pdf"); //

            processPdfFile(input, null);

            // "AS_97204878446614_1400186857444.pdf" //annotated twice
            //AS_103608674684939_1401713641754.pdf - vector graphics

            //AS_101475778826244_1401205119181.pdf - weird annotations -- FIXED
            // AS_101489636806656_1401208423051.pdf - cut too much -- // if intersects normal blocks then skip // FIXED
            // AS_101479885049868_1401206099913.pdf - figure should be annotated on page 8 // FIXED
            // AS_101482636513286_1401206754652.pdf - cut should be done from a different side //
            // AS_101483391488007_1401206934646.pdf - half of the figure is annotated
            // AS_101483399876615_1401206936959.pdf - one figure consisting of two bitmaps
            // AS_101488558870530_1401208166373.pdf - bitmap image is not assigned // FIXED
            // AS_103443628822540_1401674291617.pdf - duplicate figure captions and smaller vector boxes

            // AS_101478173773832_1401205691162.pdf - two captions match one figure
            // AS_103436670472202_1401672632695.pdf interesting vector graphics
            // AS_199986580070402_1424691924638.pdf  different captions
            // AS_97502904717322_1400257912690.pdf - not annotated figure on page 5 and 8
            // AS_97568985976833_1400273667294.pdf - too big cut // FIXED
            // AS_98989504466949_1400612345795.pdf - wrong vector image cut

//            List<Path> allPaths = PathUtil.getAllPaths(Paths.get("/Volumes/teams/common/Niall/habibi_pdfs"), "pdf");
//            List<Path> allPaths = PathUtil.getAllPaths(Paths.get("/Work/temp/context/1000k"), "pdf");
//
//            singleFile = false;
//            System.out.println("Processing " + allPaths.size());
//            for (Path p : allPaths) {
//                try {
//                    processPdfFile(p.toFile(), new File("/Work/temp/tableExtraction/out1000k_2"));
//                } catch (Exception e) {
//                    Engine.getCntManager().i("EXCEPTIONS", e.getClass().getSimpleName());
//                    e.printStackTrace();
//                }
//            }
//

            System.out.println(Engine.getCntManager());

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static Set<Integer> getVectorGraphicPages(File pdf2xmlDirectory) throws XPathException, IOException {
        //TODO: temp

        if (true) {
            return new HashSet<>();
        }
        XQueryProcessor xq = new XQueryProcessor(getOneFile(pdf2xmlDirectory, ".xml"));
        String query = XQueryProcessor.getQueryFromResources("self-contained-images.xq");

        SequenceIterator it = xq.getSequenceIterator(query);
        Item item;
        Set<Integer> blacklistedPages = new HashSet<>();
        while ((item = it.next()) != null) {
            blacklistedPages.add(Integer.parseInt(item.getStringValue()));
            it.next();
        }
        return blacklistedPages;
    }

    private static void processPdfFile(File input, File outputFolder) throws Exception {
        inputPdf = input;
        annotated = false;
        annotatedFigure = false;
        final PDDocument document = PDDocument.load(input);
        File outPdf = new File("/tmp/testFigures.pdf");

        final Engine engine = setupEngine();

        File contentDir = new File("/tmp/contentDir");
        FileUtils.deleteDirectory(contentDir);

        File assetPath = new File(contentDir, "tei");


        GrobidAnalysisConfig config = new GrobidAnalysisConfig.GrobidAnalysisConfigBuilder()
                .pdfAssetPath(assetPath)
                .withPreprocessImages(false)
                .withProcessVectorGraphics(true)
                .build();


        DocumentSource documentSource = DocumentSource.fromPdf(input);

        File pdf2xmlDirectory = new File(contentDir, "pdf2xml");
        pdf2xmlDirectory.mkdirs();
        FileUtils.copyFileToDirectory(input, contentDir);
        FileUtils.copyFile(documentSource.getXmlFile(), new File(pdf2xmlDirectory, "input.xml"));
        FileUtils.copyDirectory(new File(documentSource.getXmlFile().getAbsolutePath() + "_data"), new File(pdf2xmlDirectory, documentSource.getXmlFile().getName() + "_data"));
        System.out.println(documentSource.getXmlFile());

        blacklistedPages = getVectorGraphicPages(pdf2xmlDirectory);

        Document teiDoc = engine.fullTextToTEIDoc(documentSource, config);

        PDDocument out = annotateFigureAndTables(
                document, documentSource.getXmlFile(), teiDoc,
                false, false, true, true);

        if (out != null) {
            out.save(outPdf);
            if (singleFile) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(outPdf);
                }
            }
        }

        if (outputFolder != null) {
            if (annotated) {
                Engine.getCntManager().i("TABLES_TEST", "ANNOTATED_PDFS");
                FileUtils.copyFile(outPdf, new File(outputFolder, annotated ?
                        (annotatedFigure ? input.getName() + "_annotatedFigure.pdf" : input.getName() + "_annotated.pdf")
                        : input.getName()));
            }
        }
    }

    private static Engine setupEngine() {
        GrobidProperties.set_GROBID_HOME_PATH("grobid-home");
        GrobidProperties.setGrobidPropertiesPath("grobid-home/config/grobid.properties");
        LibraryLoader.load();
        return GrobidFactory.getInstance().getEngine();
    }

    public static PDDocument annotateFigureAndTables(
            PDDocument document,
            File xmlFile, Document teiDoc,
            boolean visualizeTeiFigures,
            boolean visualizePdf2xmlImages,
            boolean visualizeGraphicObjects,
            boolean visualizeTables
    ) throws IOException, XPathException {
        String q = XQueryProcessor.getQueryFromResources("figure-table-coords.xq");
        String tei = teiDoc.getTei();
        if (singleFile) {
            System.out.println(tei);
        }
        XQueryProcessor pr = new XQueryProcessor(tei);
        SequenceIterator it = pr.getSequenceIterator(q);
        Item item;

//        System.out.println(new TaggingTokenClusteror(GrobidModels.FULLTEXT, ));

        // visualizing TEI image coords
        if (visualizeTeiFigures) {
            while ((item = it.next()) != null) {
                String coords = item.getStringValue();
                String stringValue = it.next().getStringValue();
                boolean isFigure = Boolean.parseBoolean(stringValue);
                AnnotationUtil.annotatePage(document, coords, isFigure ? 1 : 2);
            }
        }

        //VISUALIZING "IMAGE" elements from pdf2xml
        if (visualizePdf2xmlImages) {
            q = XQueryProcessor.getQueryFromResources("figure-coords-pdf2xml.xq");

            pr = new XQueryProcessor(xmlFile);
            it = pr.getSequenceIterator(q);
            while ((item = it.next()) != null) {
                String coords = item.getStringValue();
                AnnotationUtil.annotatePage(document, coords, 3);
            }
        }

        if (visualizeGraphicObjects) {
            int i = 10;
            if (teiDoc.getFigures() != null) {
                for (Figure f : teiDoc.getFigures()) {
                    if (f == null) {
                        continue;
                    }

//                    if (blacklistedPages.contains(f.getPage())) {
//                        System.out.println("Page " + f.getPage() + " of " + inputPdf + " contains vector graphics");
//                        continue;
//                    }

                    i++;
                    List<GraphicObject> boxedGo = f.getBoxedGraphicObjects();

                    if (f.getTextArea() != null) {
                        for (BoundingBox b : f.getTextArea()) {
                            annotated = true;
                            AnnotationUtil.annotatePage(document, b.toString(),
//                        AnnotationUtil.getCoordString(f.getPage(), f.getX(), f.getY(),
//                                f.getWidth(), f.getHeight()),
                                    i, boxedGo == null ? 1 : 2
                            );
                        }
                    }

                    if (boxedGo != null) {
                        for (GraphicObject go : boxedGo) {
                            annotatedFigure = true;
                            AnnotationUtil.annotatePage(document,
                                    AnnotationUtil.getCoordString(go.getPage(), go.getX(), go.getY(),
                                            go.getWidth(), go.getHeight()), i, 2
                            );

                        }
                    }
                }
            }
        }

        if (visualizeTables) {
            boolean hasSomeTables = false;
            if (teiDoc.getTables() != null) {
                for (Table t : teiDoc.getTables()) {
                    hasSomeTables = true;
                    if (!t.isGoodTable()) {
                        System.out.println("Skipping bad table on page: " + t.getTextArea().get(0).getPage());
                        Engine.getCntManager().i("TABLES_TEST", "BAD_TABLES");

                        continue;
                    }

                    BoundingBox contentBox = BoundingBoxCalculator.calculateOneBox(t.getContentTokens());
                    BoundingBox descBox = BoundingBoxCalculator.calculateOneBox(t.getFullDescriptionTokens());

                    System.out.println("Annotating TABLE on page: " + contentBox.getPage());
                    AnnotationUtil.annotatePage(document,
                            AnnotationUtil.getCoordString(descBox), 100, 2);
                    AnnotationUtil.annotatePage(document,
                            AnnotationUtil.getCoordString(contentBox), 101, 2);
                    annotatedFigure = true;
                    annotated = true;
                    Engine.getCntManager().i("TABLES_TEST", "ANNOTATED_TABLES");
                }
            }

            if (hasSomeTables) {
                Engine.getCntManager().i("TABLES_TEST", "PDF_HAS_SOME_TABLES");
            }
        }

        return document;
    }

}
