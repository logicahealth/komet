db-builder
==============

Notes:

This project in and of itself, doesn't do anything.  It only serves as a parent pom of specific database build configurations that extend this project.

Since specific database builds need to be versioned with their own version number, there are no sub-projects here.  

This project contains a lot of boilerplate stuff that is used by each database builder that extends from this pom.  Things like populating a metadata tree in the final 
artifact, publishing the artifacts, etc.  This saves a lot of copy/paste inheritance.

If you follow the existing patterns when creating a new child pom - things will work great.  If you don't follow the existing pattern - things won't work well, as 
the parent pom expects the child modules to have certain files in certain places, and follow established naming conventions.

See https://github.com/VA-CTT/db-builder-snomed for an example databse builder that extends this project.