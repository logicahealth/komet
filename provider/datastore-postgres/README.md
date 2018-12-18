# ISAAC PostgreSQL Notes

<a id="toc"></a>
[Setup](#Setup) •
[Run KOMET Application](#RunApp) •
[Command Line Launch](#CliLaunch) •
[SQL Trace Logging](#SqlLogging) •
[Initial Data Load](#InitialDataLoad) •
[PostgreSQL v9 & v10 Notes](#PostgreSQLv9v10) •
[Resources](#Resources)

_The examples below are for PostgreSQL 9.6 and based on a macOS High Sierra computer configured with:_

* [Java SE JDK 1.8](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Apache Maven 3.5.4](https://maven.apache.org/download.cgi)
* [Netbeans IDE 8.2](https://netbeans.org/downloads/)
* [Postgres.app v2.2](https://postgresapp.com/) _additional release (provides PostgreSQL 9.5, 9.6, 10 and 11)_

## Setup <a id="Setup">[▴](#toc)</a>

**Step A.1. Setup PostgreSQL**

Download [Postgres.app](https://postgresapp.com/downloads.html) with PostgreSQL 9.5, 9.6, 10 and 11. Mount Postgres-2.2-9.5-9.6-10-11.dmg or newer. Drag Postgres.app to the /Applications folder.

Configure `$PATH` in a terminal window:

``` sh
## Note: use specific version "9.6" or "10" or else "current" version.
sudo mkdir -p /etc/paths.d &&
echo /Applications/Postgres.app/Contents/Versions/9.6/bin | \
sudo tee /etc/paths.d/postgresapp
```

Close and reopen the terminal window for the `$PATH` change to take effect. Verify `$PATH` to `psql` in the new terminal window.

``` sh
which -a psql
# /Applications/Postgres.app/Contents/Versions/9.6/bin/psql
psql --version
# psql (PostgreSQL) 9.6.10
```

Launch Postgres.app. Stop any other running version of server. Open the left server panel.

![](README_files/images/PostgresApp.10.SideBar.png)

Create a new PostgreSQL server with the `+` in the lower left.

![](README_files/images/PostgresApp.11.AddServer.v96.png)

Click "Initialize".

![](README_files/images/PostgresApp.12.Initialize.v96.png)

For convenience, check "Automatically start server" in "Server Settings…".

![](README_files/images/PostgresApp.13.AutoStart.v96.png)

Quit and restart Postgres.app. Verify that the PostgreSQL 9.6 server is running.

![](README_files/images/PostgresApp.14.Running.v96.png)

Log into a PostgreSQL terminal session by either double clicking the postgresql database in Postgres.app, or else by using the terminal command line `psql` command.

``` sh
psql --username=$USER
```

At the `psql` prompt, create the `isaac_user` user and `isaac_db` database.

``` sql
CREATE DATABASE isaac_db;
CREATE USER isaac_user WITH ENCRYPTED PASSWORD 'isaac_pwd';
GRANT ALL PRIVILEGES ON DATABASE isaac_db TO isaac_user;
```

![](README_files/images/PostgresApp.16.Terminal.v96.png)

![](README_files/images/PostgresApp.17.isaac_db.v96.png)

A double click the `isaac_db` image in the Postgres.app window will launch a terminal command line instance logged into `isaac_db`.

**Step A.2. Setup Maven `postgres` Profile**

Download and install [Apache Maven](https://maven.apache.org/download.cgi).

Edit `~/.m2/settings.xml` with `nano` or some other [plain text editor](https://github.com/coteditor/CotEditor). Add the following XML lines to _settings.xml_.  The `explicit-datstore` property tells ISAAC to use the PostgreSQL database backend instead of the FileStore backend.

``` xml
<profiles>
    <profile>
        <id>postgres</id>
        <properties>
            <explicit-datstore>postgres</explicit-datstore>
        </properties>
    </profile>
    <!-- ... other profiles -->
<profiles>
```

``` xml
<activeProfiles>
    <activeProfile>postgres</activeProfile>
    <!-- ... other active profiles -->
</activeProfiles>
```

**Step A.3. Install SQL Scripts**

Place the SQL scripts _[drop_all.sql](README_files/sql_scripts/drop_all.sql)_ and _[stats.sql](README_files/sql_scripts/stats.sql)_ somewhere convenient for command line use, for example `~/psql`. The `~/psql` directory is used in the following examples.

``` sh
mkdir ~/psql
# copy or move in SQL scripts
```

* _[drop_all.sql](README_files/sql_scripts/drop_all.sql)_ drops all tables in the database.
* _[stats.sql](README_files/sql_scripts/stats.sql)_ queries for a row count of the tables in `isaac_db`.

``` sh
psql -p5432 --username=isaac_user --dbname=isaac_db
# psql (9.6.10)
# Type "help" for help.

isaac_db=#\i psql/create_table_schema.sql
isaac_db=#\i psql/stats.sql
isaac_db=#\i psql/drop_all.sql
```

**Step A.4. Setup ISAAC Project**

Clone to download the [OSEHRA/ISAAC](https://github.com/OSEHRA/ISAAC) git repository to the local machine.

## Run KOMET Application <a id="RunApp">[▴](#toc)</a>

**Step B.1. Clean & Build ISAAC: (MMP) Informatics Analytic Architecture**

Clean & build _ISAAC: (MMP) Informatics Analytic Architecture_ project. Clean and Build can be done from the IDE or command line.

![](README_files/images/Netbeans.02.png)

``` sh
cd /PATH_TO/ISAAC
mvn --errors clean install
```

**Step B.2. Drop Database Tables**

When either the _ISAAC: (MMP) Informatics Analytic Architecture_ or _ISAAC: core metadata_ projects are built, then the `isaac_db` table are populated with data. This test data needs to be cleared before running KOMET.

![](README_files/images/Netbeans.01.png)

Verify that the `isaac_db` database is empty with either `\d` or `\i psql/stats.sql`. Use [drop_all.sql](README_files/sql_scripts/drop_all.sql) to clear the `isaac_db` database is if not empty.

``` sh
isaac_db=#\i psql/stats.sql
isaac_db=#\i psql/drop_all.sql
isaac_db=#\d
# verify: "did not find any relations."
```

**Step B.3. Run ISAAC KOMET**

Verify that the `isaac_db` database is empty prior to building and running the ISAAC KOMET application. (see step 2.)

The ISAAC KOMET application module can can be built without requiring a full build of the parent MMP project.

``` sh
cd /PATH_TO/KOMET
mvn --errors clean install
```
Change the configuration of ISAAC KOMET to `postgres-datastore`.

![](README_files/images/Netbeans.04.png)

Run ISAAC KOMET application.

![](README_files/images/Netbeans.03.png)

## Command Line Launch <a id="CliLaunch">[▴](#toc)</a>

If launched from the command line or script the PostgreSQL database name, username and user password can be provided as properties.  

* `ISAAC_PSQL_URL` Database URL in the format **`jdbc:postgresql://host:port/database`**. Default: `jdbc:postgresql://localhost/isaac_db`
* `ISAAC_PSQL_UNAME` User name with PostgreSQL privileges. Default: **`isaac_user `**
* `ISAAC_PSQL_UPWD`  User password. Default: **`isaac_pwd`**

The properties can be provided on the command line.

``` ini
-DISAAC_PSQL_URL='jdbc:postgresql://localhost/isaac_db'
-DISAAC_PSQL_UNAME='isaac_user'
-DISAAC_PSQL_UPWD='isaac_pwd'
```

If the properties are not found, then defaults will be used.

``` java
String isaacDbUrl    = System.getProperty("ISAAC_PSQL_URL", "jdbc:postgresql://localhost/isaac_db");
String isaacUsername = System.getProperty("ISAAC_PSQL_UNAME", "isaac_user");
String isaacUserpwd  = System.getProperty("ISAAC_PSQL_UPWD", "isaac_pwd");
```

## SQL Trace Logging <a id="SqlLogging">[▴](#toc)</a>

Note: For full logging of java generated SQL statements, find and set `LOG_SQL_FLAG` to true and rebuild the project.

``` java
private static final boolean LOG_SQL_FLAG = false;
```

## Initial Data Load <a id="InitialDataLoad">[▴](#toc)</a>

![](README_files/images/KometFileMenu.png)

**Step 1 Create Native Format File.**

Run a version of a filestore based KOMET in Netbeans which contains all the data to be used for an initial PostgreSQL database load. Execute menu item _File > Native format export to file…_ to created a `native-export.zip` file.

**Step 2. Create PostgreSQL CSV Import Files.**

Execute menu item _File > Native format file to CSV…_ on the previously created `native-export.zip` file.

The menu time will generate several PostgreSQL style CSV files and one `initial_data_load.sql` script.  The files generated in a `…/target/csv` directory relative to where KOMET is running.

```
initial_data_load.sql
stamp_committed_table.csv
concepts_table.csv
taxonomy_data_table.csv
type_for_assemblage_table.csv
uuid_additional_table.csv
uuid_primordial_table.csv
semantics_table.csv
```

The `initial_data_load.sql` script contains sequence initialization values based on the exported data. The `initial_data_load.sql` will be call by the `_initial_data_load_main_script.sql` script in Step 3.

**Step 3. Import Initial PostgreSQL CSV Data.**

Move a copy of the [`_initial_data_load_main_script.sql`](README_files/sql_scripts/_initial_data_load_main_script.sql), `initial_data_load.sql` and `*.csv` files to a directory that will be used for staging the import.

The [`create_table_schema.sql`](README_files/sql_scripts/create_table_schema.sql),
[`drop_all.sql`](README_files/sql_scripts/drop_all.sql), and
[`stats.sql`](README_files/sql_scripts/stats.sql) scripts will also need to be on a known reachable system path(s) relative to where the data load will occur.

Edit the `/PATH_TO/…` instances in `_initial_data_load_main_script.sql` based on the CSV files and helper SQL script have been located.

Launch PostgreSQL. Run the `_initial_data_load_main_script.sql` script from an appropriate `psql` prompt.

> Note: Steps 1 & 2 only need to run once for a given starting set of data. Once the CSV files and associcates SQL scripting are created, then the initial load can be done as needed for a new PostgreSQL database instance.

## PostgreSQL v9 & v10 Notes <a id="PostgreSQLv9v10">[▴](#toc)</a>

PostgreSQL v9.6 does not support the ANSI/ISO SQL Standard expression `AS data_type` for a `SEQUENCE`. Any `SEQUENCE` in v9.6 is type uses type `BIGINT`.

Obtaining the next value is done using the `nextval()` function instead of the standard's `NEXT VALUE FOR` expression.

## Resources <a id="Resources">[▴](#toc)</a>

* [Apache Maven ⇗](https://maven.apache.org/download.cgi)
* [Apache Doxia Markdown ⇗](https://maven.apache.org/doxia/doxia/doxia-modules/doxia-module-markdown/)
* [CotEditor ⇗](https://github.com/coteditor/CotEditor) open-source plain text editor
* [Netbeans IDE ⇗](https://netbeans.org/downloads/)
* [Oracle Java SE JDK ⇗](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [GitHub: OSEHRA/ISAAC ⇗](https://github.com/OSEHRA/ISAAC)
* [Postgres.app ⇗](https://postgresapp.com/)
* PostgreSQL Docs: [9.6⇗](https://www.postgresql.org/docs/9.6/)
