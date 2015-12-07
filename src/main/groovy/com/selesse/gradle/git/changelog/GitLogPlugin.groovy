package com.selesse.gradle.git.changelog
import com.selesse.gradle.git.changelog.tasks.GenerateChangelogTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin

class GitLogPlugin implements Plugin<Project> {
    Logger logger = Logging.getLogger(GitLogPlugin)
    private GitChangelogExtension extension

    @Override
    void apply(Project project) {
        applyPluginDependency(project)

        extension = project.extensions.create("changelog", GitChangelogExtension)

        extension.with {
            title = project.name
            outputDirectory = project.buildDir
        }

        logger.info("Initialized with settings: ${extension}")
    }

    def applyPluginDependency(Project project) {
        GenerateChangelogTask task = project.tasks.create("generateChangelog", GenerateChangelogTask)
        task.setGitDirectory(project.projectDir.absolutePath)

        project.afterEvaluate {
            project.plugins.withType(JavaPlugin) {
                logger.info("Configuring Java plugin")
                Task processResources = project.tasks.processResources
                if (processResources != null) {
                    logger.debug("Making assembleTask depend on ${task.name}")
                    processResources.dependsOn(task)

                    logger.debug("Setting destination directory to {}", processResources.destinationDir)
                    project.extensions.changelog.outputDirectory = processResources.destinationDir
                }
            }

            if (project.plugins.findPlugin(JavaPlugin) == null) {
                project.plugins.withType(BasePlugin) {
                    logger.debug("Configuring Base Plugin")

                    project.tasks.getByName("assemble")
                    project.tasks.findAll { t -> t.name.startsWith('assemble') }.each {
                        it.dependsOn(task)
                    }

//                    Task assembleTask = project.tasks.getByName("assemble") as Task
//                    if (assembleTask != null) {
//                        logger.debug("Making assembleTask depend on ${task.name}")
//                        assembleTask.dependsOn(task)
//                    }
                }
            }
        }
    }
}
