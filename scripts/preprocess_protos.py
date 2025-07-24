#!/usr/bin/env python3
"""
Preprocess proto files to remove buf.validate annotations
"""

import os
import re
import sys

def remove_buf_validate(content):
    """Remove buf.validate annotations from proto content"""
    
    # Remove import statements
    content = re.sub(r'import\s+"buf/validate/validate\.proto";\s*\n?', '', content)
    
    # Remove option (buf.validate.oneof).required = true; and similar
    content = re.sub(r'option\s+\(buf\.validate\.[^)]+\)[^;]*;\s*\n?', '', content)
    
    # Remove [...] blocks that contain buf.validate, handling nested brackets
    result = []
    i = 0
    while i < len(content):
        if content[i] == '[' and 'buf.validate' in content[i:i+1000]:  # Quick check
            # Found a bracket that might contain buf.validate
            bracket_count = 1
            j = i + 1
            while j < len(content) and bracket_count > 0:
                if content[j] == '[':
                    bracket_count += 1
                elif content[j] == ']':
                    bracket_count -= 1
                j += 1
            
            # Check if this bracket block contains buf.validate
            bracket_content = content[i:j]
            if 'buf.validate' in bracket_content:
                # Skip this entire bracket block
                i = j
                continue
            else:
                # Keep this bracket block
                result.append(content[i])
                i += 1
        else:
            result.append(content[i])
            i += 1
    
    return ''.join(result)

def process_file(input_path, output_path):
    """Process a single proto file"""
    with open(input_path, 'r') as f:
        content = f.read()
    
    processed = remove_buf_validate(content)
    
    # Keep original package names (ser) - no changes needed
    
    with open(output_path, 'w') as f:
        f.write(processed)

def main():
    proto_dir = "proto"
    temp_dir = "temp_proto"
    
    # Create temp directory
    os.makedirs(temp_dir, exist_ok=True)
    
    # Process all proto files
    for filename in os.listdir(proto_dir):
        if filename.endswith('.proto'):
            input_path = os.path.join(proto_dir, filename)
            output_path = os.path.join(temp_dir, filename)
            print(f"Processing {filename}...")
            process_file(input_path, output_path)
    
    print("Preprocessing complete")

if __name__ == "__main__":
    main()