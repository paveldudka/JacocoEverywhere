# JacocoEverywhere
Gradle plugin which allows to generate Jacoco coverage report for both integration &amp; unit tests

# Why?

Android plugin has built-in support for generating test coverage report:
```gradle
android {
    buildTypes {
        debug {
            testCoverageEnabled = true
        }
    }
}
```

However, this report only shows test coverage for your integration tests (aka UI tests, aka those ones you run with `./gradlew connectedCheck`). 

But what we really want - is to see test coverage report for both integration **AND** unit test suites!

jacoco-everywhere plugin does just that!

![](https://github.com/paveldudka/JacocoEverywhere/blob/master/screenshot.png)

# Usage

1) Enable code coverage like you normally would for your integration tests:
```gradle
android {
    buildTypes {
        debug {
            testCoverageEnabled = true
        }
    }
}
```
2) Apply `jacoco-coverage` plugin to your Android module:
`build.gradle`:
```gradle
apply plugin: 'com.android.application'
apply plugin: 'jacoco-everywhere'
```
3) execute `./gradlew connectedCheck` from your command line
4) Observe Jacoco test coverage report in `<your_project_dir>/<android_module>/build/reports/coverage/debug/index.html`




