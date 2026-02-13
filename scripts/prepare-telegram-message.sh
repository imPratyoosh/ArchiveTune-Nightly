#!/bin/bash
# Script to prepare Telegram message

RELEASE_TAG="$1"
REPO="$2"
RELEASE_URL="https://github.com/$REPO/releases/tag/$RELEASE_TAG"

# Create APK links (plain text for Markdown)
{
  echo "ArchiveTune Nightly $RELEASE_TAG Released"
  echo ""
  echo "Check out changelog here: $RELEASE_URL"
  echo ""
  echo "*Download:*"
  echo "- Mobile 64-bit (arm64): https://github.com/$REPO/releases/download/$RELEASE_TAG/app-arm64-nightly.apk"
  echo "- Mobile 32-bit (armeabi): https://github.com/$REPO/releases/download/$RELEASE_TAG/app-armeabi-nightly.apk"
  echo "- Tablet 32-bit (x86): https://github.com/$REPO/releases/download/$RELEASE_TAG/app-x86-nightly.apk"
  echo "- Tablet 64-bit (x86_64): https://github.com/$REPO/releases/download/$RELEASE_TAG/app-x86_64-nightly.apk"
  echo "- Universal: https://github.com/$REPO/releases/download/$RELEASE_TAG/ArchiveTune-Nightly.apk"
} > /tmp/message.txt

echo "release_url=$RELEASE_URL"
