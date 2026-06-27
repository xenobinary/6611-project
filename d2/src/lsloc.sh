#!/bin/bash

# 1. SETUP TRAP FOR SAFETY
# This guarantees that the temporary .clean files are deleted when the script finishes,
# even if you cancel it halfway through using Ctrl+C.
trap 'find . -type f -name "*.java.clean" -delete 2>/dev/null' EXIT

echo "Analyzing Java files..."

# 2. SILENTLY GENERATE CLEAN FILES
# We use cloc to generate .java.clean files. 
# > /dev/null 2>&1 hides cloc's default summary table from the terminal.
find . -type f -name "*.java" -not -path "*/test/*" -not -path "*/tests/*" \
    -exec cloc --quiet --strip-comments=clean --original-dir {} + > /dev/null 2>&1

# 3. PRINT TABLE HEADER
printf "\n%-60s | %-12s\n" "File Path" "Logical SLOC"
echo "-------------------------------------------------------------+--------------"

total_sloc=0

# 4. PROCESS FILES AND POPULATE TABLE
# We use -print0 and read -d '' to safely handle filenames that might have spaces in them.
while IFS= read -r -d '' clean_file; do
    
    # Run your grep regex on the clean file
    count=$(grep -E -o ";|\bif\s*\(|\bfor\s*\(|\bwhile\s*\(" "$clean_file" | wc -l)
    
    # Format the display name (remove the .clean extension and the ./ prefix)
    display_name="${clean_file%.clean}"
    display_name="${display_name#./}"
    
    # Print the row (Truncates the filename to 60 characters to keep the table aligned)
    printf "%-60.60s | %12d\n" "$display_name" "$count"
    
    # Add to running total
    total_sloc=$((total_sloc + count))

done < <(find . -type f -name "*.java.clean" -print0)

# 5. PRINT TABLE FOOTER
echo "-------------------------------------------------------------+--------------"
printf "%-60s | %12d\n\n" "TOTAL" "$total_sloc"

# (The trap command from step 1 will now automatically clean up the files as the script exits!)