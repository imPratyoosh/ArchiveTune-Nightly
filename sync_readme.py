#!/usr/bin/env python3
import shutil
import requests

# Copy temp/README.md to root folder
shutil.copy('temp/README.md', 'README.md')

# Fetch raw README.md content from koiverse/ArchiveTune repository
response = requests.get('https://raw.githubusercontent.com/koiverse/ArchiveTune/main/README.md')
raw_content = response.text

# Read the current README.md
with open('README.md', 'r') as f:
    content = f.read()

# Replace the placeholder line with the fetched raw content
placeholder = "Sync README.md content from https://github.com/koiverse/ArchiveTune raw."
new_content = content.replace(placeholder, raw_content)

# Write the updated content back to README.md
with open('README.md', 'w') as f:
    f.write(new_content)