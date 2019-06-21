package org.grobid.core.jni;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.utilities.GrobidProperties;

public class PythonVirtualEnvConfig {

    private Path virtualEnv;
    private Path sitePackagesPath;
    private Path jepPath;

    public PythonVirtualEnvConfig(Path virtualEnv, Path sitePackagesPath, Path jepPath) {
        this.virtualEnv = virtualEnv;
        this.sitePackagesPath = sitePackagesPath;
        this.jepPath = jepPath;
    }

    public boolean isEmpty() {
        return this.virtualEnv == null;
    }

    public Path getVirtualEnv() {
        return this.virtualEnv;
    }

    public Path getSitePackagesPath() {
        return this.sitePackagesPath;
    }

    public Path getJepPath() {
        return this.jepPath;
    }

    public static PythonVirtualEnvConfig getInstanceForVirtualEnv(String virtualEnv) throws GrobidResourceException {
        if (StringUtils.isEmpty(GrobidProperties.getPythonVirtualEnv())) {
            return new PythonVirtualEnvConfig(null, null, null);
        }
        List<Path> pythons;
        try {
            pythons = Files.find(
                Paths.get(virtualEnv, "lib"),
                1,
                (path, attr) -> path.getFileName().toString().contains("python3")
            ).collect(Collectors.toList());
        } catch (IOException e) {
            throw new GrobidResourceException("failed to get python versions from virtual environment", e);
        }

        List<String> pythonVersions = pythons
            .stream()
            .map(path -> FilenameUtils.getBaseName(path.getFileName().toString()).replace("libpython", ""))
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(pythonVersions)) {
            throw new GrobidException(
                "Cannot find a suitable version (3.5 or 3.6) of python in your virtual environment: " +
                virtualEnv
            );
        }

        Path sitePackagesPath = Paths.get(pythons.get(0).toString(), "site-packages");
        Path jepPath = Paths.get(sitePackagesPath.toString(), "jep");
        return new PythonVirtualEnvConfig(
            Paths.get(virtualEnv),
            sitePackagesPath,
            jepPath
        );
    }

    public static PythonVirtualEnvConfig getInstance() throws GrobidResourceException {
        return getInstanceForVirtualEnv(
            GrobidProperties.getPythonVirtualEnv()
        );
    }
}