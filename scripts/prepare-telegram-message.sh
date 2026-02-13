#!/bin/bash
# Script to prepare Telegram message

RELEASE_TAG="$1"
REPO="$2"
RELEASE_URL="https://github.com/$REPO/releases/tag/$RELEASE_TAG"

# Create APK links
echo "*Download:" > /tmp/apk_links.txt
echo "- Mobile 64-bit (arm64): https://github.com/$REPO/releases/download/$RELEASE_TAG/app-arm64-nightly.apk" >> /tmp/apk_links.txt
echo "- Mobile 32-bit (armeabi): https://github.com/$REPO/releases/download/$RELEASE_TAG/app-armeabi-nightly.apk" >> /tmp/apk_links.txt
echo "- Tablet 32-bit (x86): https://github.com/$REPO/releases/download/$RELEASE_TAG/app-x86-nightly.apk" >> /tmp/apk_links.txt
echo "- Tablet 64-bit (x86_64): https://github.com/$REPO/releases/download/$RELEASE_TAG/app-x86_64-nightly.apk" >> /tmp/apk_links.txt
echo "- Universal: https://github.com/$REPO/releases/download/$RELEASE_TAG/ArchiveTune-Nightly.apk" >> /tmp/apk_links.txt

# Create main message
{
  echo "ArchiveTune Nightly $RELEASE_TAG Released"
  echo ""
  echo "Check out changelog here: $RELEASE_URL"
  echo ""
  cat /tmp/apk_links.txt
} > /tmp/message.txt

echo "release_url=$RELEASE_URL"
