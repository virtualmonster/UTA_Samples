# HCL OneTest Script Visual Studio Code Language Server extension
## Getting started
Install extension dependencies in project directory.
```
npm install
```
Build XText Langauge Server Fat Jar ("com.hcl.products.onetest.lang.script.ide-1.0.0-SNAPSHOT-ls.jar") and place in extension directory root.

```
mvn install
```
Or Eclipse Maven build configuration with the "install" goal from the parent project.



Run extension launch configuration in VS Code which opens a new window. Open a file with the ".script" file extension which will load the language server and TextMate grammar after a few seconds.

## Debugging

Breakpoints can be used to debug extension code ("extension.ts").