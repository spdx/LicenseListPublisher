# LicenseListPublisher

This is the source code repository for the tool that generates license data found in the [license-list-data](https://github.com/spdx/license-list-data) repository.  The source for the data is located in the [license-list-XML](https://github.com/spdx/license-list-XML) repository.

## Code quality badges

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=licenseListPublisher&metric=bugs)](https://sonarcloud.io/dashboard?id=licenseListPublisher)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=licenseListPublisher&metric=security_rating)](https://sonarcloud.io/dashboard?id=licenseListPublisher)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=licenseListPublisher&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=licenseListPublisher)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=licenseListPublisher&metric=sqale_index)](https://sonarcloud.io/dashboard?id=licenseListPublisher)

## Getting Starting

The package is available in
[Maven Central](https://search.maven.org/artifact/org.spdx/licenseListPublisher)
(organization: `org.spdx`, artifact: `licenseListPublisher`).

## Contributing

See the file [CONTRIBUTING.md](CONTRIBUTING.md) for information on making contributions to the LicenseListPublisher.

## Syntax

The command line interface of the licenseListPublisher can be used like this:

```shell
java -jar licenseListPublisher.jar <function> <parameters> 
```

Where the following functions are supported:

- `LicenseRDFAGenerator` - Generates license data
- `TestLicenseXML` - Tests a license XML file

### LicenseRDFAGenerator

Usage:

```text
LicenseRDFAGenerator licenseXmlFileOrDir outputDirectory [version] [releasedate] [testfiles] [ignoredwarnings]
   licenseXmlFileOrDir - a license XML file or a directory of license XML files
   outputDirectory - Directory to store the output from the license generator
   [version] - Version of the SPDX license list
   [releasedate] - Release date of the SPDX license list
   [testfiles] - Directory of original text files to compare the generated licenses against
   [ignoredwarnings] - Either a file name or a JSON-formatted collection of warnings to be ignored
```

***WARNING:** Running the LicenseRDFAGenerator for a single file
will overwrite any index.html, licenses.json etc. with the single file results.*

### TestLicenseXML

Usage:

```text
TestLicenseXML licenseXmlFile textFile
   licenseXmlFile XML - file to test
   textFile - Text file which should match the the license text for the licenseXmlFile
   testDirectory - Optional directory of test files in the form {license-id}/(license|header|exception)/(good|bad)/{test-id}.txt
```

## License

See the [NOTICE](NOTICE) file for licensing information
including info from 3rd Party Software.

See [LICENSE](LICENSE) file for full license text.

```text
SPDX-License-Identifier: Apache-2.0
PackageLicenseDeclared:  Apache-2.0
```

## Development

### Build

You need [Apache Maven](http://maven.apache.org/) to build the project:

```shell
mvn clean install
```
