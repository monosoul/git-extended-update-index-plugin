[![Build Status](https://github.com/monosoul/git-extended-update-index-plugin/actions/workflows/build.yaml/badge.svg?branch=master)](https://github.com/monosoul/git-extended-update-index-plugin/actions/workflows/build.yaml?query=branch%3Amaster)
[![codecov](https://codecov.io/gh/monosoul/git-extended-update-index-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/monosoul/git-extended-update-index-plugin)
[![JetBrains IntelliJ Plugins](https://img.shields.io/jetbrains/plugin/v/11217)](https://plugins.jetbrains.com/plugin/11217-git-extended-update-index)
[![JetBrains IntelliJ plugins](https://img.shields.io/jetbrains/plugin/d/11217)](https://plugins.jetbrains.com/plugin/11217-git-extended-update-index)
![license](https://img.shields.io/github/license/monosoul/git-extended-update-index-plugin.svg)

# Git extended update-index plugin
IntelliJ IDEA plugin to add the extended support of the `git update-index` command.

It adds the support for the next subcommands:
 - --skip-worktree
 - --no-skip-worktree
 - --chmod=+x
 - --chmod=-x

## Screenshots
![New items in the git context menu](screenshots/screenshot_1.png)

![New items in the git context menu](screenshots/screenshot_2.png)

![Show Skipped Worktree option](screenshots/screenshot_3.png)

![Show skipped worktree changes view element](screenshots/screenshot_4.png)

## Release History
* 0.1.1
  * Use the latest IntelliJ API.
* 0.1.0
  * Introduced a way to see skipped worktree files in the changes view tool window. 
* 0.0.5
  * Run the commands in a background task (fixes the exception).
* 0.0.4
  * Switched to modern IntelliJ API.
  * Set Java 11 as compile target.
  * Use the latest Kotlin compiler.
* 0.0.3
    * Rewrote the plugin in Kotlin.
    * Switched to modern IntelliJ API.
    * Added a plugin icon.
* 0.0.2
    * Updated dependencies to make the plugin available on all platforms.
* 0.0.1
    * Initial release.
    
## License
The software is licensed under the [Apache-2.0 License](LICENSE).
