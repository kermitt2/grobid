---
name: General report
about: Create a report to help us improve
title: ''
labels: ''
assignees: ''

---

- What is your OS and architecture? Windows is not supported and Mac OS arm64 is not yet supported. For non-supported OS, you can use Docker (https://grobid.readthedocs.io/en/latest/Grobid-docker/)

- What is your Java version (`java --version`)? Expected versions are currently Java 8, 9 and 10. 

- In case of build or run errors, please submit the error while running gradlew with ``--stacktrace`` and ``--info`` for better log traces (e.g. `./gradlew run --stacktrace --info`) or attach the log file `logs/grobid-service.log`.
