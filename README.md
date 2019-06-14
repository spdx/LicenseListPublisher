[![Build Status](https://travis-ci.org/spdx/LicenseListPublisher.svg?branch=master)](https://travis-ci.org/spdx/LicenseListPublisher)

# LicenseListPublisher

This is the source code repository for the tool that generates license data found in the [license-list-data](https://github.com/spdx/license-list-data) repository.  The source for the the data is located in the [license-list-XML](https://github.com/spdx/license-list-XML) repository.

## Contributing
See the file [CONTRIBUTING.md](CONTRIBUTING.md) for information on making contributions to the LicenseListPublisher.

## Syntax
The command line interface of the licenseListPublisher can be used like this:

    java -jar licenseListPublisher.jar <function> <parameters> 

Where the following functions and parameters are supported:

```
LicenseRDFAGenerator licencenseXmlDir outputDirectory [version] [releasedate] [testfiles] [ignoredwarnings]
   licencenseXmlDir - a directory of license XML files
   outputDirectory - Directory to store the output from the license generator
   [version] - Version of the SPDX license list
   [releasedate] - Release date of the SPDX license list
   [testfiles] - Directory of original text files to compare the generated licenses against
   [ignoredwarnings] - Either a file name or a comma separated list of warnings to be ignored
```

```
LicenseListPublisher
 -d,--directory <arg>        Input XML directory
 -h,--help                   Prints out this message
 -I,--ignoreAllWarnings      Ignore all warnings
 -O,--outputrepo <arg>       Git repository to output the license list
                             data to.  The git user must have update
                             access to this repository
 -p,--password <arg>         Github password
 -r,--release <arg>          License list release tag or version
 -t,--testOnly               Only tests the license XML files - does not
                             update or publish the results
 -u,--user <arg>             Github Username
 -w,--ignoreWarnings <arg>   Ignore specific warning messages
 -x,--xmlrepo <arg>          Input license XML repository
 -z,--debug                  Prints debug information while processing
```

# License
See the [NOTICE](NOTICE) file for licensing information
including info from 3rd Party Software

See [LICENSE](LICENSE) file for full license text

    SPDX-License-Identifier:	Apache-2.0
    PackageLicenseDeclared:		Apache-2.0

# Development

## Build
You need [Apache Maven](http://maven.apache.org/) to build the project:

    mvn clean install
