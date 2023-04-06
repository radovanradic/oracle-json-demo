This is sample for Micronaut Data using Oracle Json Duality View.

It uses test container with docker image "gvenzl/oracle-free:latest-faststart" by default and then
datasource configuration is taken from the container before starting Micronaut application context.
For simplicity purposes, it is just simple flag in Main class

```boolean useContainer = true;```

Other option is running this docker command 
```
docker run -p 1521:1521 -e ORACLE_PWD=test -d --name oracle container-registry.oracle.com/database/free
```
in combination with

```boolean useContainer = false;```

might be simpler and faster to run.

This demo just runs some scenarios with relational tables and then uses/modifies data from the tables using Oracle Json Duality View.

Current issues with Micronaut Data solution for Oracle Json View:
1. When creating new record with auto increment ID, we are not able to return newly generated id. 
   That will be worked on in near future.