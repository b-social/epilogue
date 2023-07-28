# Common development actions

## Lint/test/etc.

```shell
clojure -M:lint           # Lint
clojure -T:cljfmt fix     # Fix formatting
clojure -T:antq outdated  # List outdated deps
```

<!-- clojure -X:test           # Run tests -->

## Build

> **Warning**<br>
> If you have any local changes that haven't been committed, they will be included in the built jar file.

Build and install the jar with these commands.

```shell
clojure -T:build jar
clojure -T:build install
```

If there was no Git tag pointing to the commit, the jar will have the version: `local`.

## Deploy

Create a Git tag for the version to build (preferrably prefixed with `v`) pointing at the relevant commit.

Run this command (replacing the username and password with your own and the version with the Git tag), which will test, build and deploy the jar to Clojars.

```shell
CLOJARS_USERNAME=username CLOJARS_PASSWORD=CLOJARS_pat ./build/deploy.sh vX.X
```
