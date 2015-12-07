package com.selesse.gradle.git

import com.google.common.base.Splitter
import com.selesse.gradle.git.changelog.generator.ComplexChangelogGenerator
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class GitCommandExecutor {
    Logger logger = Logging.getLogger(ComplexChangelogGenerator)
    private File executionContext
    private String changelogFormat
    private String gitDirectory = '.'

    GitCommandExecutor(String changelogFormat) {
        this.changelogFormat = changelogFormat
    }

    GitCommandExecutor(String changelogFormat, File context) {
        this.changelogFormat = changelogFormat
        this.executionContext = context
    }

    public void setGitDirectory(String gitDirectory) {
        this.gitDirectory = gitDirectory;
    }

    public List<String> getTags() {
        Splitter.on("\n").omitEmptyStrings().trimResults().splitToList(
                executeCommand('git', '-C', gitDirectory, 'for-each-ref', '--format=%(objectname) | %(taggerdate)', 'refs/tags')
        )
    }

    public String getLastTag() {
        return executeCommand('git', '-C', gitDirectory, 'describe', '--abbrev=0', '--tags')
    }

    public List<String> getTagsSince(String ref) {
        Splitter.on("\n").omitEmptyStrings().trimResults().splitToList(
                executeCommand('git', '-C', gitDirectory, 'tag', '--contains', ref)
        )
    }

    private String executeCommand(String... args) {
        if (executionContext != null) {
            args.execute(null, executionContext).text.trim()
        } else {
            args.execute().text.trim()
        }
    }

    public String getCommitDate(String commit) {
        executeCommand('git', '-C', gitDirectory, 'log', '-1', '--format=%ai', commit)
    }

    private String[] getBaseGitCommand() {
        ['git', '-C', gitDirectory, 'log', "--pretty=format:${changelogFormat}"]
    }

    public String getGitChangelog() {
        executeCommand('git', '-C', gitDirectory, 'log', "--pretty=format:${changelogFormat}")
    }

    public String getGitChangelog(String reference) {
        logger.info("Getting Git changelog for {}", reference)
        executeCommand((getBaseGitCommand() + reference) as String[])
    }

    public String getGitChangelog(String firstReference, String secondReference) {
        logger.info("Getting Git changelog for {}...{}", firstReference, secondReference)
        executeCommand((getBaseGitCommand() + "${firstReference}...${secondReference}") as String[])
    }

    public String getTagName(String commit) {
        executeCommand('git', '-C', gitDirectory, 'describe', '--tags', commit)
    }

    public String getTagDate(String tag) {
        executeCommand('git', '-C', gitDirectory, 'log', '-1', '--format=%ai', tag)
    }

    public String getLatestCommit() {
        executeCommand('git', '-C', gitDirectory, 'log', '-1', '--pretty=format:%H')
    }
}
