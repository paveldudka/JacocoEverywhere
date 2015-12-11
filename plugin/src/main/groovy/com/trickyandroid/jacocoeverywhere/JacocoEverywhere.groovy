package com.trickyandroid.jacocoeverywhere

import org.gradle.api.Plugin
import org.gradle.api.Project

class JacocoEverywhere implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def task = project.task("test") << {
            println "Hello from test!   "
        }
    }
}