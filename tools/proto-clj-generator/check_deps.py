#!/usr/bin/env python3
import json
import sys

# Load the JSON file
with open('references/json-descriptors/jon_shared_cmd_rotary.json', 'r') as f:
    data = json.load(f)

# Find the actual proto file (not google/protobuf descriptors)
for file_desc in data['file']:
    if 'jon_shared' in file_desc['name']:
        print(f"File: {file_desc['name']}")
        print(f"Package: {file_desc.get('package', 'N/A')}")
        print(f"Dependencies: {file_desc.get('dependency', [])}")
        print()