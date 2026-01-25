#!/usr/bin/env kotlin

@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("com.google.code.gson:gson:2.10.1")

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonArray
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

// --- Data Models ---
data class CommitInfo(
    val sha: String,
    val message: String,
    val repository: String,
    val branch: String
)

data class History(val commit: CommitInfo)

// --- Global Instances ---
val gson = Gson()
val client = HttpClient.newHttpClient()

// --- Helper Functions ---

fun fetch(url: String): String {
    val request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("User-Agent", "Kotlin-Script-Changelog-Bot")
        .GET()
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    if (response.statusCode() !in 200..299) {
        throw RuntimeException("Failed to fetch: $url - Status: ${response.statusCode()} - Body: ${response.body()}")
    }
    return response.body()
}

fun fetchLatestSha(owner: String, repo: String, branch: String): String {
    val url = "https://api.github.com/repos/$owner/$repo/branches/$branch"
    val json = fetch(url)
    val data = gson.fromJson(json, JsonObject::class.java)
    return data.getAsJsonObject("commit").get("sha").asString
}

fun fetchCommits(owner: String, repo: String, sinceSha: String, untilSha: String): JsonArray {
    val url = "https://api.github.com/repos/$owner/$repo/compare/$sinceSha...$untilSha"
    val json = fetch(url)
    val data = gson.fromJson(json, JsonObject::class.java)
    return data.getAsJsonArray("commits") ?: JsonArray()
}

fun formatChangelog(commits: JsonArray): String {
    val sb = StringBuilder("## ✨ Changelog\n\n")
    
    // The GitHub API returns the commit order from oldest to newest in the compare view.
    // We reverse the order to display the newest commit at the top.
    val commitList = commits.map { it.asJsonObject }.reversed()

    // Date and time format
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.of("UTC")) // Timezone

    for (commit in commitList) {
        val sha = commit.get("sha").asString
        val commitDetails = commit.getAsJsonObject("commit")
        
        // Get the first line of the commit message
        var message = commitDetails.get("message").asString.split("\n")[0]
        
        // Replace issue number #123 with the link
        val issueRegex = "#(\\d+)".toRegex()
        message = issueRegex.replace(message) { matchResult ->
            val number = matchResult.groupValues[1]
            "[#$number](https://github.com/koiverse/ArchiveTune/issues/$number)"
        }

        // Get author information (preferably GitHub login, fallback to Git username)
        val author = if (commit.has("author") && !commit.get("author").isJsonNull) {
            commit.getAsJsonObject("author").get("login").asString
        } else {
            commitDetails.getAsJsonObject("author").get("name").asString
        }

        // Format date and time
        val dateStr = commitDetails.getAsJsonObject("author").get("date").asString
        val date = dateFormatter.format(Instant.parse(dateStr))

        // Create log line
        sb.append("- <code>$date</code>: [<code>${sha.take(7)}</code>](https://github.com/koiverse/ArchiveTune/commit/$sha) - **\"$message\"** by @$author\n")
    }
    
    return sb.toString()
}

// --- Main Execution ---

fun main() {
    try {
        var lastSha: String? = null
        var repoPath: String? = null
        var branch: String? = null

        // 1. Check environment variables first
        if (System.getenv("LAST_SHA") != null) {
            lastSha = System.getenv("LAST_SHA")
            repoPath = System.getenv("LAST_REPO") ?: "koiverse/ArchiveTune"
            branch = System.getenv("LAST_BRANCH") ?: "dev"
        } else {
            // 2. If there is no environment, read the history/commit.json file.
            val historyFile = File("history/commit.json")
            
            // Check the GITHUB_ACTIONS environment variable to ensure the logic matches the old file.
            if (System.getenv("GITHUB_ACTIONS") != null) {
                if (historyFile.exists()) {
                    try {
                        val historyData = historyFile.readText()
                        val history = gson.fromJson(historyData, History::class.java)
                        lastSha = history.commit.sha
                        repoPath = history.commit.repository
                        branch = history.commit.branch
                    } catch (e: Exception) {
                        println("History file found but invalid, skipping...")
                        return
                    }
                } else {
                    println("History file not found.")
                    return
                }
            } else {
                // Fallback for local developers if needed.
                return
            }
        }

        // Unwrap nullable values
        if (lastSha == null || repoPath == null || branch == null) {
            println("Missing required information (SHA/Repo/Branch). Exiting.")
            return
        }

        val (owner, repoName) = repoPath.split("/")
        
        // Get the latest SHA from remote
        val latestSha = fetchLatestSha(owner, repoName, branch)

        println("Comparing from $lastSha to $latestSha")

        // If there are no changes
        if (lastSha == latestSha) {
            println("No new commits.")
            File("changelog.md").writeText("No new changes.")
            return
        }

        // Get the commit list and create a changelog.
        val commits = fetchCommits(owner, repoName, lastSha, latestSha)
        val changelog = formatChangelog(commits)
        
        File("changelog.md").writeText(changelog)
        println("Changelog generated: changelog.md")

    } catch (e: Exception) {
        System.err.println("Error generating changelog: ${e.message}")
        e.printStackTrace()
        exitProcess(1) // Báo lỗi cho GitHub Actions biết để fail job
    }
}

// Run the main function
main()
