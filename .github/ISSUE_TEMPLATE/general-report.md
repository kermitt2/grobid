---
name: General report
about: Create a report to help us improve
title: ''
labels: ''
assignees: ''
---

Before creating a new issue, make sure you had a look at the [official documentation](https://grobid.readthedocs.com). For specific questions, you can try the [Mendable Q/A chat](https://www.mendable.ai/demo/723cfc12-fdd6-4631-9a9e-21b80241131b) (**NOTE**: This is rather experimental, if not sure, make sure you double-check using the official documentation.)

- What is your OS and architecture? Windows is not supported and Mac OS arm64 is experimentally supported. For non-supported OS, you can use Docker (https://grobid.readthedocs.io/en/latest/Grobid-docker/)

- What is your Java version (`java --version`)? 

- In case of build or run errors, please submit the error while running gradlew with ``--stacktrace`` and ``--info`` for better log traces (e.g. `./gradlew run --stacktrace --info`) or attach the log file `logs/grobid-service.log` or the console log. 
