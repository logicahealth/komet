ISAAC Changelog 

This changelog summarizes changes and fixes which are a part of each revision.  For more details on the fixes, refer tracking numbers 
where provided, and the git commit history.

* 2017/03/?? - 3.40 - PENDING
    * 

* 2017/03/08 - 3.39
    * Improved debugging code for chasing various issues in AITC.

* 2017/03/02 - 3.38
    * update loader to allow the passing a a folder that contains ibdf files, rather than requiring specific lists.  part of jazz 468085
    * add nucc and cvx to prisme integrations.  jazz 469791, 469788
    * refactoring for reduced bugs on prisme integrations, updates for content-per-module design changes.  jazz 468087
    * Much work on the backend code for HL7 messaging protocol to VistA sites via VIE (checksum, discovery)

* 2017/02/16 - 3.37
    * refix 460134 defect with HL7v3 source upload (again)
    * VetsExporter code modified to no rely on string identifiers for mapping types
    * Update metadata constant descriptions, definitions for mapping related column headers (jazz ids 440248 and 440195)
    * Adding constants for IPO mapset columns (related to jazz id 462456) 
    * Adding a constant for Name
    * VetsExporter fix for defect 461049
    * read any state description (active or inactive) when reading dynamic sememe column names

* 2017/02/09 - 3.36
    * Adding code for mapping dynamic columns.
    * HL7 messageing integration work.
    * Changing remaining instances of qualifier to equivalence type.
    * Disabled unused VHAT Export code path.

* 2017/02/04 - 3.35
    * Bug fix for terminology converter shared code

* 2017/02/03 - 3.34
    * Fix for Property exports, defect 452239
    * 444799 - HL7 upload configuration details added for PRISME to pick up

* 2017/01/26 - 3.33
    * Make the GitPublish methods used by the source upload / convert / db builder portions of prisme automatically create the remote git repository if it doesn't exist.
    * HL7 messaging callback and testing.
    * Fortify code changes.
    
* 2017/01/19 - 3.32
    * Added configuration options for remote service configs (like git)
    * Tweaked the way that DBIDs were created / stored, added the IDs to a sememe on root, and to the changeset data files.
    * Fix bugs with shutdown sequence, which would crop up during a corrupt DB recovery
    * fix a bug with the lucene index config, where it cached a reference to the data store location which it shouldn't have
    * Added the git syncronization service.  If the configuration service is configured with valid GIT details, changeset files
        will be automatically synced up and down from a database specific git repository. 

* 2017/01/17 - 3.31
    * Workflow changes to align with web, correct time conversion.
    * Refactoring the gitblit repo create code.
    * Add a UUID to the DB, when the db is created, to aid in changeset repo alignment.
    * Adding ISAAC_DEPENDENTS_RUNLEVEL = 5

* 2017/01/05 - 3.30
    * Adding MAPPING_QUALIFIER_UNMAPPED as child of MAPPING_QUALIFIERS
    * Ensure system shuts down cleanly preventing database locks from corrupting database
    * Add mechanism to review database to ensure isn't corrupt
    * Fix an issue that caused invalid UUIDs to be generated after adding the semantic tag into FSNs of metadata concepts
    * Clean up APIs to make it clear what is going on
    * Adding non-logging getServiceWithNoLog() and Removing getServiceWithNoLog() from LookupService

* 2016/12/21 - 3.29
    * Fixed NPE found during SQA testing (SubsetMemberships)
    * Big MapSets commit with miscellaneous cleanup and comments
    * Fixed fragile MapEntry code
    * Fixed minor Subset issue of including MapSets during a full export

* 2016/12/13 - 3.28
    * Fixed Association code to use correct Actions and NewTargetCode/OldTargetCode values.
    * Fixed a 'null dereference' issue that was flagged by Fortify.
    * Long overdue metadata cleanup / alignment between ISAAC and DB Loaders
    * Added a time-based write to the changeset writers, to ensure they are flushed to disk frequently

* 2016/12/07 - 3.27
    * Fix properties on to ensure they export and have correct action. Make sure new value is not present when the same as old value. Fix coded
        concept to ensure correct action is updated.

* 2016/12/06 - 3.26
    * Add ValueOld to VetsExporter in buildProperty and getDesignations.
    * Fix a nasty bug dealing with the metadata for VHAT Module and SOLOR Module Overlay, which lead to phantom / missing 
        modules in the database.

* 2016/12/04 - 3.25
    * Add threading to ChangesSetWriterHandler.  Add functionality to disable and enable writing. 
    * Adding a db build mode, so that indexes and changesets aren't done incrementally while building
        a database
    * Adjust some configuration on the lucene indexer to enhance performance during DB build.

* 2016/12/02 - 3.24
    * Fixing bugs with VHAT XML export (workaround for data model issue, many other fixes)

* 2016/11/30 - 3.23
    * (Jazz 418368) Fix the lucene indexer so it doesn't miss commit notifications
    * Clean up some errors that were being (erroneously) logged internally

* 2016/11/29 - 3.22
    * Move the changesets down into a changesets folder to resolve db build issues.
    * Fix some indexer config API issues that previously went undetected (but came out due to better edge case checking)

* 2016/11/28 - 3.21
    * Fixed a null pointer in association reading.

* 2016/11/28 - 3.20
    * Changes to make the concept builder more flexible with respect to creation of FSN and preferred term.
    * Integrating commit handling / notification

* 2016/11/22 - 3.19
    * Enhancements and bug fixes to VETs XML Export

* 2016/11/17 - 3.18: 
    * Enhancements to VETs XML Export
    * Added more convenience methods to Frills
    * Rename a couple of API methods to have sensible names that actually match their behavior
    * Fix a bug with the concept builder that caused extra description builders to get lost (or the default one to get lost)
    * Fix defect 392895 by restricting XML upload to XML files only.

* 2016/11/02 - 3.17: 
    * Add a scheduled thread pool executor
    * Enhancements to the VETs XMl Export

* 2016/11/01 - 3.16: 
    * See the GIT changelog for updates prior to this release.
