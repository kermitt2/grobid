# GROBID and containers (Docker)

Docker is an open-source project that automates the deployment of applications inside software containers.
The documentation on how to install it and start using it can be found [here](https://docs.docker.com/engine/understanding-docker/).

GROBID can be instantiated and run using Docker. The image information can be found [here](https://hub.docker.com/r/lfoppiano/grobid/).

We assume in the following that docker is installed and working on your system. Note that the default memory available for your container might need to be increased for using all the available Grobid services, in particular on `macos`, see the Troubleshooting section below.

The process for fetching and running the image is as follow:

- Pull the image from docker HUB

```bash
> docker pull lfoppiano/grobid:${latest_grobid_version}
```

For instance, latest stable version:

```bash
> docker pull lfoppiano/grobid:0.5.6
```

- Run the container (note the new version running on 8070, however it will be mapped on the 8080 of your host):

```bash
> docker run -t --rm --init -p 8080:8070 -p 8081:8071 lfoppiano/grobid:${latest_grobid_version}
```

(alternatively you can also get the image ID)  

```bash
> docker images | grep lfoppiano/grobid | grep ${latest_grobid_version}
> docker run -t --rm --init -p 8080:8070 -p 8081:8071 $image_id_from_previous_command
```

- Access the service:
  - open the browser at the address `http://localhost:8080`
  - the health check will be accessible at the address `http://localhost:8081`

Grobid web services are then available as described in the [service documentation](https://grobid.readthedocs.io/en/latest/Grobid-service/).

## Configuration using Environment Variables

Properties from the `grobid-home/config/grobid.properties` can be overridden using environment variables.
Given a property key, the corresponding environment variable is the property key converted to upper case and the dot (`.`) replaced by two underscores `__`. (Property keys must be all lower case)

e.g. to configure `grobid.nb_threads` use `GROBID__NB_THREADS`.

```bash
> docker run -t --rm --init -p 8080:8070 -p 8081:8071 \
    --env GROBID__NB_THREADS=10 \
    lfoppiano/grobid:${latest_grobid_version}
```

## Troubleshooting

### Out of memory or container being killed while processing

This is usually be due to insufficient memory allocated to the docker machine. Depending on the intended usage, we recommend to allocate 4GB of RAM to structure entirely all the PDF content (`/api/processFulltextDocument`), otherwise 2GB are sufficient to extract only header information, and 3GB for citations. In case of more intensive usage and batch parallel processing, allocating 6 or 8GB is recommended.

On `macos`, see for instance [here](https://stackoverflow.com/questions/32834082/how-to-increase-docker-machine-memory-mac/39720010#39720010) on how to increase the RAM from the Docker UI.

The memory can be verified directly using the docker desktop application or via CLI:  

```bash
> docker-machine inspect
```

You should see something like:

```json
{
    "ConfigVersion": 3,
    "Driver": {
        "IPAddress": "192.168.99.100",
        "MachineName": "default",
        "SSHUser": "docker",
        "SSHPort": 55933,
        "SSHKeyPath": "/Users/lfoppiano/.docker/machine/machines/default/id_rsa",
        "StorePath": "/Users/lfoppiano/.docker/machine",
        "SwarmMaster": false,
        "SwarmHost": "tcp://0.0.0.0:3376",
        "SwarmDiscovery": "",
        "VBoxManager": {},
        "HostInterfaces": {},
        "CPU": 1,
        "Memory": 2048,     #<---- Memory: 2GB
        "DiskSize": 204800,
        "NatNicType": "82540EM",
        "Boot2DockerURL": "",
        "Boot2DockerImportVM": "",
        "HostDNSResolver": false,
        "HostOnlyCIDR": "192.168.99.1/24",
        "HostOnlyNicType": "82540EM",
        "HostOnlyPromiscMode": "deny",
        "NoShare": false,
        "DNSProxy": true,
        "NoVTXCheck": false
    },
    "DriverName": "virtualbox",
    "HostOptions": {
      [...]
        },
        "SwarmOptions": {
         [...]
        },
        "AuthOptions": {
           [...]
        }
    },
    "Name": "default"
}
```

See for instance [here](https://stackoverflow.com/a/36982696) for allocating to the Docker machine more than the default RAM on `macos` with command lines.

### pdfalto zombie processes

When running docker without an init process, the pdfalto processes will be hang as zombie eventually filling up the machine. The docker solution is to use `--init` as parameter when running the image, however we are discussing some more long-term solution compatible with Kubernetes for example.

The solution shipped with the current Dockerfile, using [tini](https://github.com/krallin/tini) should provide the correct init process to cleanup
killed processes.

## Building an image

The following part is normally only for development purposes. You can use the official stable docker images from the docker HUB as described above.
However if you are interested in using the master version of Grobid in container, building a new image is the way to go.

The docker build for a particular version (here for example the latest stable version `0.5.6`) will clone the repository using git, so no need to custom builds. Only important information is the version which will be checked out from the tags.

```bash
> docker build -t grobid/grobid:0.5.6 --build-arg GROBID_VERSION=0.5.6 .
```

Similarly, if you want to create a docker image from the current master, development version:

```bash
> docker build -t grobid/grobid:0.6.0-SNAPSHOT --build-arg GROBID_VERSION=0.6.0-SNAPSHOT .
```

In order to run the container of the newly created image for version `0.5.6`:

```bash
> docker run -t --rm --init -p 8080:8070 -p 8081:8071 grobid/grobid:0.5.6
```

For testing or debugging purposes, you can connect to the container with a bash shell (logs are under `/opt/grobid/logs/`):

```bash
> docker exec -i -t {container_name} /bin/bash
```

The container name is given by the command:

```bash
> docker container ls
```
