import { readFileSync, writeFileSync } from 'fs';

interface CommitInfo {
  sha: string;
  message: string;
  repository: string;
  branch: string;
}

interface History {
  commit: CommitInfo;
}

async function fetchLatestSha(owner: string, repo: string, branch: string): Promise<string> {
  const url = `https://api.github.com/repos/${owner}/${repo}/branches/${branch}`;
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Failed to fetch latest SHA: ${response.statusText}`);
  }
  const data = await response.json();
  return data.commit.sha;
}

async function fetchCommits(owner: string, repo: string, sinceSha: string, untilSha: string): Promise<any[]> {
  const url = `https://api.github.com/repos/${owner}/${repo}/compare/${sinceSha}...${untilSha}`;
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Failed to fetch commits: ${response.statusText}`);
  }
  const data = await response.json();
  return data.commits || [];
}

function formatChangelog(commits: any[]): string {
  let changelog = '## ✨ Changelog\n\n';
  
  commits.reverse();
  commits.forEach(commit => {
    const sha = commit.sha;
    let message = commit.commit.message.split('\n')[0]; // First line of commit message
    // Mask #number to links
    message = message.replace(/#(\d+)/g, '[#$1](https://github.com/koiverse/ArchiveTune/issues/$1)');
    const author = commit.commit.author.name;
    const date = new Date(commit.commit.author.date).toISOString().split('T')[0];
    changelog += `- ${date}: [\`${sha.slice(0, 7)}\`](https://github.com/koiverse/ArchiveTune/commit/${sha}) - **${message}** by @${author}\n`;
  });
  return changelog;
}

async function main() {
  try {
    let lastSha: string;
    let repo: string;
    let branch: string;

    if (process.env.LAST_SHA) {
      lastSha = process.env.LAST_SHA;
      repo = process.env.LAST_REPO || 'koiverse/ArchiveTune';
      branch = process.env.LAST_BRANCH || 'dev';
    } else {
      // Read history/commit.json
      if (process.env.GITHUB_ACTIONS) {
          try {
            const historyData = readFileSync('history/commit.json', 'utf-8');
            const history: History = JSON.parse(historyData);
            lastSha = history.commit.sha;
            repo = history.commit.repository;
            branch = history.commit.branch;
          } catch (e) {
             console.log("History file not found or invalid, skipping...");
             return; 
          }
      } else {
          // Fallback local test
          return;
      }
    }

    const [owner, repoName] = repo.split('/');

    // Get the latest SHA from dev branch
    const latestSha = await fetchLatestSha(owner, repoName, branch);

    console.log(`Comparing from ${lastSha} to ${latestSha}`);

    if (lastSha === latestSha) {
        console.log("No new commits.");
        writeFileSync('changelog.md', "No new changes.");
        return;
    }

    // Fetch commits
    const commits = await fetchCommits(owner, repoName, lastSha, latestSha);

    // Generate changelog
    const changelog = formatChangelog(commits);

    // Write to changelog.md
    writeFileSync('changelog.md', changelog);
    console.log('Changelog generated: changelog.md');
  } catch (error) {
    console.error('Error generating changelog:', error);
    process.exit(1); // Exit with error code
  }
}

main();
