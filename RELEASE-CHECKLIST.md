# Release Checklist for the LicenseListPublisher

- [ ] Check for any warnings from the compiler and findbugs
- [ ] Run unit tests for all packages that depend on the library
- [ ] Run the the publisher against the current license list and check for any errors
- [ ] Run dependency check to find any potential vulnerabilities `mvn dependency-check:check`
- [ ] Run `mvn release:prepare` - you will be prompted for the release - typically take the defaults
- [ ] Run `mvn release:perform`
- [ ] Release artifacts to Maven Central
- [ ] Create a Git release including release notes
- [ ] Zip up the files from the Maven archive and add them to the release
