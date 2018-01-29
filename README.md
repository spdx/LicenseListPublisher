# LicenseListPublisher

This is the source code repository for the tool that generates license data found in the [license-list-data](https://github.com/spdx/license-list-data) repository.  The source for the the data is located in the [license-list-XML](https://github.com/spdx/license-list-XML) repository.

## Contributing
See the file CONTRIBUTING.md for information on making contributions to the LicenseListPublisher.

## Syntax
The command line interface of the licenseListPublisher can be used like this:

    java -jar licenseListPublisher.jar <function> <parameters> 

Where the following functions are supported:
	generate licenseSpreadsheet.xls|inputDirectory outputDirectory [version] [releasedate] [licenseTestFileDirectory] [warningsToIgnore (either a file or comma separated list)]
	publish -u gitUser -p gitPassword [-r releaseTagOrVersion] [-o outputGitRepository] [--ignoreAllWarnings] [-w warningsToIgnore] [-d inputXmlDirectory] [-x inputXmlRepositoryUrl]
	
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