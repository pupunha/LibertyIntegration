package net.pupunha.liberty.integration.view.toolwindow.applications.looseapplication;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import net.pupunha.liberty.integration.configuration.LibertyConfiguration;
import net.pupunha.liberty.integration.configuration.LibertyConfigurationRepository;
import net.pupunha.liberty.integration.constants.MavenConstants;
import net.pupunha.liberty.integration.exception.LibertyConfigurationException;
import net.pupunha.liberty.integration.util.ReadPom;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LooseApplicationGenerate {


    public static final String PACK_LOCAL = "C:/devtools/home/a23418/projects/packages/2017.11";
    //    public static final String PROJECT_EAR = "/projects/java8/apps/pims-product/pims-product-ui-ear";
//    public static final String PROJECT_EAR = "/projects/java8/apps/policy-tool/policytool-ear";
//    public static final String PROJECT_EAR = "/projects/java8/apps/pims-money-inout/pims-money-inout-ui-ear";
//    public static final String PROJECT_EAR = "/projects/java8/apps/pims-product/pims-product-ear";
    public static final String PROJECT_EAR = "\\projects\\java8\\apps\\batch-module\\batch-module-ear";


    public static final String META_INF = "META-INF";
    public static final String WEB_INF = "WEB-INF";
    public static final String CLASSES = "classes";
    public static final String LIB = "lib";

    public static final String PATTERN_JAR_WITH_VERSION = "^(.+?)-(\\d.*?)\\.jar$";
    public static final String PATTERN_WAR_WITH_VERSION = "^(.+?)-(\\d.*?)\\.war$";
    public static final String PATTERN_WAR_WITHOUT_VERSION = "^(.+?).war$";

    public static final String ARCHIVE = "archive";
    public static final String DIR = "dir";
    public static final String FILE = "file";
    public static final String TARGET_IN_ARCHIVE = "targetInArchive";
    public static final String SOURCE_ON_DISK = "sourceOnDisk";
    public static final String TARGET = "/target";
    public static final String WAR = ".war";
    public static final String EAR = ".ear";

    private LooseApplicationParameter parameter;
    private Project project;
    private JTextPane textPane;

    private List<String> listNotBuilt;

    public LooseApplicationGenerate(Project project, LooseApplicationParameter parameter, JTextPane textPane) {
        this.parameter = parameter;
        this.project = project;
        this.textPane = textPane;
        this.listNotBuilt = new ArrayList<>();
    }

    public void insertText(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = textPane.getStyledDocument();
                System.out.println(String.format("[%s] - %s - %s", Thread.currentThread(), doc.getLength(), text));
                doc.insertString(doc.getLength(), text.concat("\n"), null);
                textPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                Messages.showMessageDialog(project, e.getMessage(), "Error", Messages.getErrorIcon());
            }
        });
    }

    public List<Path> listDependenciesInPackage(Path projectEAR, List<Path> listModulesInPackage) throws IOException, LibertyConfigurationException {
        List<Path> dependenciesInPackage = new ArrayList<>();

        Path pathFileEARGenerated = getPathFileEARGenerated();

        if (pathFileEARGenerated != null) {
            String fileEAR = pathFileEARGenerated.getFileName().toString();
            final int lastPeriodPos = fileEAR.lastIndexOf('.');
            String nameEARWithoutExtension = fileEAR.substring(0, lastPeriodPos);

            Path folderEAR = Paths.get(projectEAR.toString(), TARGET, nameEARWithoutExtension);
            if (folderEAR != null) {
                DirectoryStream<Path> streamArchiveWAR = Files.newDirectoryStream(folderEAR, f -> f.toString().endsWith(WAR));
                for (Path entry : streamArchiveWAR) {
                    Pattern r = Pattern.compile(PATTERN_WAR_WITH_VERSION);
                    Matcher m = r.matcher(entry.getFileName().toString());
                    if (m.find()) {
                        String projectWarName = m.group(1);
                        String projectWarVersion = m.group(2);

                        Path pathProjectWAR = getPathProjectWAR(projectWarName);

                        String projectWARTarget = entry.getFileName().toString().substring(0, entry.getFileName().toString().lastIndexOf('.'));
                        Path pathProjectTargetWAR = Paths.get(pathProjectWAR.toString(), TARGET, projectWARTarget);
                        if (!Files.exists(pathProjectTargetWAR)) {
                            pathProjectTargetWAR = Files.walk(Paths.get(pathProjectWAR.toString(), TARGET))
                                    .filter(p -> p.toString().endsWith(projectWarVersion))
                                    .findFirst()
                                    .orElse(null);
                        }

                        DirectoryStream<Path> streamContentWAR = Files.newDirectoryStream(pathProjectTargetWAR, f -> f.toFile().isDirectory());
                        for (Path entryContentWAR : streamContentWAR) {
                            if (entryContentWAR.endsWith(WEB_INF)) {
                                DirectoryStream<Path> streamContentWebInfWAR = Files.newDirectoryStream(entryContentWAR, f -> f.toFile().isDirectory());
                                for (Path entryContentWebInfWAR : streamContentWebInfWAR) {
                                    if (entryContentWebInfWAR.endsWith(LIB)) {
                                        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(entryContentWebInfWAR.toString()));
                                        for (Path path : directoryStream) {
                                            String fileNameJar = path.getFileName().toString();
                                            Pattern patternJar = Pattern.compile(PATTERN_JAR_WITH_VERSION);
                                            Matcher matcherJar = patternJar.matcher(fileNameJar);
                                            if (matcherJar.find()) {
                                                String projectName = matcherJar.group(1);
                                                Path pathProject = listModulesInPackage.stream()
                                                        .filter(p -> p.endsWith(projectName))
                                                        .findFirst().orElse(null);
                                                if (pathProject != null) {
                                                    dependenciesInPackage.add(path);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return dependenciesInPackage;
    }

    public Path createLooseApplication(Boolean saveFile) throws Exception {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Path pathFileEARGenerated = getPathFileEARGenerated();

        if (pathFileEARGenerated != null) {
            String fileEAR = pathFileEARGenerated.getFileName().toString();
            final int lastPeriodPos = fileEAR.lastIndexOf('.');
            String nameEARWithoutExtension = fileEAR.substring(0, lastPeriodPos);

            Path folderEAR = Paths.get(parameter.getProjectEAR().toString(), TARGET, nameEARWithoutExtension);
            if (folderEAR != null) {

                Element archiveEAR = doc.createElement(ARCHIVE);
                doc.appendChild(archiveEAR);
                if (textPane != null) insertText("Generate Archive EAR");

                DirectoryStream<Path> streamDirs = Files.newDirectoryStream(folderEAR, f -> f.toFile().isDirectory());
                for (Path entry : streamDirs) {
                    Element dir = doc.createElement(DIR);
                    dir.setAttribute(TARGET_IN_ARCHIVE, "/" + entry.getFileName().toString());
                    dir.setAttribute(SOURCE_ON_DISK, entry.toString().replace("\\","/"));
                    archiveEAR.appendChild(dir);
                    if (textPane != null) insertText("Generate EAR DIR " + "/" + entry.getFileName().toString());
                }

                DirectoryStream<Path> streamFiles = Files.newDirectoryStream(folderEAR, f -> !f.toFile().isDirectory() && !f.toString().endsWith(WAR));
                for (Path entry : streamFiles) {
                    Element dir = doc.createElement(FILE);
                    dir.setAttribute(TARGET_IN_ARCHIVE, "/" + entry.getFileName().toString());
                    dir.setAttribute(SOURCE_ON_DISK, entry.toString().replace("\\","/"));
                    archiveEAR.appendChild(dir);
                    if (textPane != null) insertText("Generate EAR FILE " + "/" + entry.getFileName().toString());
                }

                DirectoryStream<Path> streamArchiveWAR = Files.newDirectoryStream(folderEAR, f -> f.toString().endsWith(WAR));
                for (Path entry : streamArchiveWAR) {

                    Element archiveWAR = doc.createElement(ARCHIVE);
                    archiveWAR.setAttribute(TARGET_IN_ARCHIVE, "/" + entry.getFileName().toString());
                    archiveEAR.appendChild(archiveWAR);
                    if (textPane != null) insertText("Generate Archive WAR");

                    Pattern r = Pattern.compile(PATTERN_WAR_WITH_VERSION);
                    Matcher m = r.matcher(entry.getFileName().toString());
                    if (!m.find()) {
                        r = Pattern.compile(PATTERN_WAR_WITHOUT_VERSION);
                        m = r.matcher(entry.getFileName().toString());
                    }

                    if (m.find()) {
                        String projectWarName = m.group(1);
                        String projectWarVersion = null;
                        if (m.groupCount() > 1) {
                            projectWarVersion = m.group(2);
                        }
                        String projectWARTarget = entry.getFileName().toString().substring(0, entry.getFileName().toString().lastIndexOf('.'));

                        /**TO POLICY-TOOL**/
                        Path pathProjectWAR = getPathProjectWAR(projectWarName);
                        Path pathProjectWithWebAppDirWAR = Paths.get(pathProjectWAR.toString(), "/src/main/webapp/WEB-INF");
                        Path pathProjectTargetWAR = Paths.get(pathProjectWAR.toString(), TARGET, projectWARTarget);
                        if (!Files.exists(pathProjectTargetWAR)) {
                            if (projectWarVersion != null) {
                                final String version = projectWarVersion;
                                pathProjectTargetWAR = Files.walk(Paths.get(pathProjectWAR.toString(), TARGET))
                                        .filter(p -> p.toString().endsWith(version))
                                        .findFirst()
                                        .orElse(null);
                            } else {
                                pathProjectTargetWAR = Files.walk(Paths.get(pathProjectWAR.toString(), TARGET))
                                        .filter(p -> p.getFileName().toString().startsWith(projectWarName) && Files.isDirectory(p))
                                        .findFirst()
                                        .orElse(null);
                            }
                        }
                        Path pathProjectTargetClassesWAR = Paths.get(pathProjectWAR.toString(), "/target/classes");

                        DirectoryStream<Path> streamContentWAR = Files.newDirectoryStream(pathProjectTargetWAR, f -> f.toFile().isDirectory());
                        for (Path entryContentWAR : streamContentWAR) {
                            if (entryContentWAR.endsWith(META_INF)) {

                                Element dir = doc.createElement(DIR);
                                dir.setAttribute(TARGET_IN_ARCHIVE, "/" + META_INF);
                                dir.setAttribute(SOURCE_ON_DISK, entryContentWAR.toString().replace("\\", "/"));
                                archiveWAR.appendChild(dir);
                                if (textPane != null) insertText("Generate WAR DIR " + "/" + META_INF);

                            } else if (entryContentWAR.endsWith(WEB_INF)) {
                                DirectoryStream<Path> streamContentWebInfWAR = Files.newDirectoryStream(entryContentWAR, f -> f.toFile().isDirectory());
                                for (Path entryContentWebInfWAR : streamContentWebInfWAR) {
                                    if (entryContentWebInfWAR.endsWith(LIB)) {
                                        createDependencies(doc, archiveWAR, entryContentWebInfWAR.toString(), parameter.getModules(), parameter.getModulesInPackage());
                                    } else {
                                        Element dir = doc.createElement(DIR);
                                        dir.setAttribute(TARGET_IN_ARCHIVE, "/" + WEB_INF + "/" + entryContentWebInfWAR.getFileName());
                                        Path pathInWebAppDir = Paths.get(pathProjectWithWebAppDirWAR.toString(), entryContentWebInfWAR.getFileName().toString());
                                        if (Files.exists(pathInWebAppDir)) {
                                            dir.setAttribute(SOURCE_ON_DISK, pathInWebAppDir.toString().replace("\\", "/"));
                                            archiveWAR.appendChild(dir);
                                        } else {
                                            if (CLASSES.equals(entryContentWebInfWAR.getFileName().toString())) {
                                                dir.setAttribute(SOURCE_ON_DISK, pathProjectTargetClassesWAR.toString().replace("\\", "/"));
                                            } else {
                                                dir.setAttribute(SOURCE_ON_DISK, entryContentWebInfWAR.toString().replace("\\", "/"));
                                            }
                                            archiveWAR.appendChild(dir);
                                        }
                                    }
                                }

                                DirectoryStream<Path> streamContentWebInfFileWAR = Files.newDirectoryStream(entryContentWAR, f -> !f.toFile().isDirectory());
                                for (Path entryContentWebInfFileWAR : streamContentWebInfFileWAR) {
                                    Element file = doc.createElement(FILE);
                                    file.setAttribute(TARGET_IN_ARCHIVE, "/" + WEB_INF + "/" + entryContentWebInfFileWAR.getFileName());
                                    Path pathInWebAppFile = Paths.get(pathProjectWithWebAppDirWAR.toString(), entryContentWebInfFileWAR.getFileName().toString());
                                    if (Files.exists(pathInWebAppFile)) {
                                        file.setAttribute(SOURCE_ON_DISK, pathInWebAppFile.toString().replace("\\", "/"));
                                        archiveWAR.appendChild(file);
                                    } else {
                                        file.setAttribute(SOURCE_ON_DISK, entryContentWebInfFileWAR.toString().replace("\\", "/"));
                                        archiveWAR.appendChild(file);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LibertyConfigurationRepository repository = new LibertyConfigurationRepository();
            LibertyConfiguration configuration = repository.load();
            Path absolutePathApps = configuration.getAbsolutePathApps();
            Path pathFileLooseApplication = getPathFileLooseApplication(absolutePathApps);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);

            OutputStream outputStream;
            if (saveFile) {
                outputStream = new FileOutputStream(pathFileLooseApplication.toFile());
            } else {
                outputStream = System.out;
            }
            StreamResult result = new StreamResult(outputStream);
            transformer.transform(source, result);
            outputStream.close();

            if (textPane != null) insertText("FINISH. Loose Application generated in: " + pathFileLooseApplication.toString());
            if (textPane != null) {
                if (!listNotBuilt.isEmpty()) {
                    insertText("------------------------------------------------");
                    insertText("WARNING: Projects that were not previously built");
                    listNotBuilt.forEach(this::insertText);
                    insertText("------------------------------------------------");
                }
            }
        }
        return pathFileEARGenerated;
    }

    @Nullable
    private Path getPathProjectWAR(String projectWarName) throws IOException {
        return Files.walk(parameter.getProjectEAR().getParent())
                                    .filter(p -> p.toString().endsWith(MavenConstants.POM_XML))
                                    .filter(p -> ReadPom.getPackaging(p).equals(MavenConstants.WAR))
                                    .map(Path::getParent)
                                    .distinct()
                                    .findFirst()
                                    .orElse(null);
    }

    public Path getPathFileLooseApplication(Path absolutePathApps) throws IOException {
        if (getPathFileEARGenerated() != null) {
            String fileName = getPathFileEARGenerated().getFileName().toString();
            return Paths.get(absolutePathApps.toString(), fileName.concat(".xml"));
        }
        return null;
    }

    @Nullable
    public Path getPathFileEARGenerated() throws IOException {
        return Files.walk(parameter.getProjectEAR())
                    .filter(p -> p.toString().endsWith(EAR))
                    .findFirst()
                    .orElse(null);
    }

    public static void main(String args[]) throws IOException {
        LooseApplicationParameter parameter = new LooseApplicationParameter();
//        Path projectEAR = Paths.get("C:\\devtools\\home\\a23418\\projects\\packages\\2017.11\\projects\\java8\\apps\\batch-module\\batch-module-ear");
//        Path projectEAR = Paths.get("C:\\devtools\\home\\a23418\\projects\\packages\\2017.11\\projects\\java8\\apps\\batch-module\\batch-module-ui-ear");
        Path projectEAR = Paths.get("C:\\devtools\\home\\a23418\\projects\\packages\\2017.11\\projects\\java8\\apps\\pims-money-inout\\pims-money-inout-ear");
        parameter.setProjectEAR(projectEAR);
        List<Path> allPackProjectsStream = Files.walk(Paths.get(PACK_LOCAL))
                .filter(p -> p.endsWith(MavenConstants.POM_XML))
                .map(Path::getParent)
                .collect(Collectors.toList());
        parameter.setModulesInPackage(allPackProjectsStream);
        parameter.setModules(allPackProjectsStream);
        LooseApplicationGenerate generate = new LooseApplicationGenerate(null, parameter, null);
        try {
            generate.createLooseApplication(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String findRepositoryLocal() throws Exception {
        String home = System.getProperty("user.home");
        Path pathMavenSettings = Paths.get(home, "/.m2/settings.xml");
        if (Files.exists(pathMavenSettings)) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pathMavenSettings.toFile());

            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//localRepository";
            Node evaluate = (Node) xPath.compile(expression).evaluate(doc, XPathConstants.NODE);
            String textContent = evaluate.getTextContent();
            return textContent;
        } else {
            Path path = Paths.get(home, "/.m2/repository");
            return path.toString();
        }
    }

    private static boolean isDirEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    private void createDependencies(Document doc, Element archiveWAR, String directoryLibraries, List<Path> selectedModules, List<Path> allPackProjectsStream) throws Exception {

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryLibraries))) {
            for (Path path : directoryStream) {
                String fileNameJar = path.getFileName().toString();
                Pattern r = Pattern.compile(PATTERN_JAR_WITH_VERSION);
                Matcher m = r.matcher(fileNameJar);
                if (m.find()) {
                    String projectName = m.group(1);
                    Path pathProject = allPackProjectsStream.stream().filter(p -> p.endsWith(projectName)).findFirst().orElse(null);
                    boolean containInDependenciesSelected = selectedModules != null && selectedModules.contains(path);
                    if (pathProject != null && containInDependenciesSelected) {

                        Element archiveJAR = doc.createElement(ARCHIVE);
                        archiveJAR.setAttribute(TARGET_IN_ARCHIVE, "/WEB-INF/lib/" + fileNameJar);

                        if (textPane != null) insertText("Generate WAR JAR " + "/WEB-INF/lib/" + fileNameJar);

                        Path pathProjectTargetClasses = Paths.get(pathProject.toString(), "/target/classes");
                        if (isDirEmpty(pathProjectTargetClasses)) {
                            listNotBuilt.add(pathProjectTargetClasses.toString());
                        }

                        Element dir = doc.createElement(DIR);
                        dir.setAttribute(TARGET_IN_ARCHIVE, "/");
                        dir.setAttribute(SOURCE_ON_DISK, pathProjectTargetClasses.toString().replace("\\","/"));
                        archiveJAR.appendChild(dir);

                        archiveWAR.appendChild(archiveJAR);

                    } else {
                        Files.walk(Paths.get(findRepositoryLocal()))
                                .filter(p -> p.toString().endsWith(fileNameJar))
                                .distinct()
                                .findFirst().ifPresent(fullPath -> {
                            Element fileJAR = doc.createElement(FILE);
                            fileJAR.setAttribute(TARGET_IN_ARCHIVE, "/WEB-INF/lib/" + fileNameJar);
                            fileJAR.setAttribute(SOURCE_ON_DISK, fullPath.toString().replace("\\","/"));
                            archiveWAR.appendChild(fileJAR);

                            if (textPane != null) insertText("Generate WAR JAR /WEB-INF/lib/" + fileNameJar);
                        });
                    }
                } else {
                    System.out.println("NO MATCH");
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
