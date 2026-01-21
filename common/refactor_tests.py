
import os
import shutil

src_dir = r"c:\Users\ikerb\OneDrive\Escritorio\MU\3-Maila\PBL5\Memorylens\common\src\test\java\tests"
dest_dir = r"c:\Users\ikerb\OneDrive\Escritorio\MU\3-Maila\PBL5\Memorylens\common\src\test\java\me\sebz\mu\pbl5\tests"

if not os.path.exists(dest_dir):
    os.makedirs(dest_dir)

for filename in os.listdir(src_dir):
    if filename.endswith(".java"):
        old_path = os.path.join(src_dir, filename)
        new_path = os.path.join(dest_dir, filename)
        
        with open(old_path, "r", encoding="utf-8") as f:
            content = f.read()
            
        # Replace package declaration
        new_content = content.replace("package tests;", "package me.sebz.mu.pbl5.tests;")
        
        with open(new_path, "w", encoding="utf-8") as f:
            f.write(new_content)
        
        print(f"Moved and updated {filename}")
        os.remove(old_path)

# Remove old directory if empty
try:
    os.rmdir(src_dir)
except OSError:
    pass
