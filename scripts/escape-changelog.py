#!/usr/bin/env python3
"""
Escape changelog text for Telegram MarkdownV2 format.
Reads from /tmp/changelog.txt and outputs escaped text.
"""

import sys

def escape_markdown_v2(text: str) -> str:
    """
    Escape special characters for Telegram MarkdownV2.
    Characters that need escaping: _ * [ ] ( ) ~ ` > # + - = | { } . !
    """
    special_chars = ["\\", "_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+", "-", "=", "|", "{", "}", ".", "!"]
    for char in special_chars:
        text = text.replace(char, "\\" + char)
    return text

def main():
    try:
        with open("/tmp/changelog.txt", "r", encoding="utf-8") as f:
            text = f.read()
        
        # Truncate to 3500 chars for Telegram limit (4096 total, leave room for header)
        text = text[:3500]
        
        # Escape for MarkdownV2
        escaped = escape_markdown_v2(text)
        
        # Replace newlines with %0A for JSON
        escaped = escaped.replace("\n", "%0A")
        
        print(escaped)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
