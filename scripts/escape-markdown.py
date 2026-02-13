#!/usr/bin/env python3
"""Escape text for Telegram MarkdownV2 and URL encode."""

import sys
import urllib.parse

def escape_markdown_v2(text: str) -> str:
    """Escape special characters for Telegram MarkdownV2."""
    special = ['\\', '_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!']
    for c in special:
        text = text.replace(c, '\\' + c)
    return text

def main():
    # Read from /tmp/message.txt
    try:
        with open('/tmp/message.txt', 'r', encoding='utf-8') as f:
            text = f.read()
        
        # Escape for MarkdownV2
        escaped = escape_markdown_v2(text)
        
        # URL encode
        encoded = urllib.parse.quote(escaped)
        
        print(encoded)
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == '__main__':
    main()
