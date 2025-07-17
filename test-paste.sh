#!/bin/bash

# Test script for paste.rs-like functionality
# Make sure the server is running on localhost:3000

BASE_URL="http://localhost:3000/api/v1/paste"

echo "🧪 Testing paste.rs-like functionality..."
echo "==========================================="

# Test 1: Create a simple text paste
echo ""
echo "📝 Test 1: Creating a simple text paste"
echo "Hello, world! This is a test paste." > test_file.txt
PASTE_URL=$(curl -s --data-binary @test_file.txt $BASE_URL)
echo "Paste created: $PASTE_URL"
PASTE_ID=$(basename "$PASTE_URL")
echo "Paste ID: $PASTE_ID"

# Test 2: Retrieve the paste as plain text
echo ""
echo "📖 Test 2: Retrieving paste as plain text"
curl -s "$BASE_URL/$PASTE_ID"
echo ""

# Test 3: Create a JSON paste
echo ""
echo "📝 Test 3: Creating a JSON paste"
echo '{"name": "John Doe", "age": 30, "city": "New York"}' > test.json
JSON_PASTE_URL=$(curl -s --data-binary @test.json $BASE_URL)
JSON_PASTE_ID=$(basename "$JSON_PASTE_URL")
echo "JSON paste created: $JSON_PASTE_URL"

# Test 4: Retrieve JSON with .json extension
echo ""
echo "📖 Test 4: Retrieving JSON with .json extension"
curl -s "$BASE_URL/$JSON_PASTE_ID.json"
echo ""

# Test 5: Create a markdown paste
echo ""
echo "📝 Test 5: Creating a markdown paste"
cat > test.md << 'EOF'
# Hello Markdown

This is a **bold** text and this is *italic*.

## Code Example

```javascript
function hello() {
    console.log("Hello, world!");
}
```

[Visit Google](https://google.com)
EOF

MD_PASTE_URL=$(curl -s --data-binary @test.md $BASE_URL)
MD_PASTE_ID=$(basename "$MD_PASTE_URL")
echo "Markdown paste created: $MD_PASTE_URL"

# Test 6: Retrieve markdown as HTML
echo ""
echo "📖 Test 6: Retrieving markdown as HTML"
curl -s "$BASE_URL/$MD_PASTE_ID.md" | head -20
echo "... (truncated)"

# Test 7: Create a Python code paste
echo ""
echo "📝 Test 7: Creating a Python code paste"
cat > test.py << 'EOF'
def fibonacci(n):
    if n <= 1:
        return n
    return fibonacci(n-1) + fibonacci(n-2)

# Test the function
for i in range(10):
    print(f"F({i}) = {fibonacci(i)}")
EOF

PY_PASTE_URL=$(curl -s --data-binary @test.py $BASE_URL)
PY_PASTE_ID=$(basename "$PY_PASTE_URL")
echo "Python paste created: $PY_PASTE_URL"

# Test 8: Retrieve Python code with syntax highlighting
echo ""
echo "📖 Test 8: Retrieving Python code with syntax highlighting"
curl -s "$BASE_URL/$PY_PASTE_ID.py" | head -15
echo "... (truncated HTML)"

# Test 9: Get paste info
echo ""
echo "📊 Test 9: Getting paste metadata"
curl -s "$BASE_URL/$PASTE_ID/info" | python3 -m json.tool

# Test 10: Test stdin pasting (like in examples)
echo ""
echo "📝 Test 10: Pasting from stdin"
STDIN_PASTE_URL=$(echo "This paste was created from stdin!" | curl -s --data-binary @- $BASE_URL)
STDIN_PASTE_ID=$(basename "$STDIN_PASTE_URL")
echo "Stdin paste created: $STDIN_PASTE_URL"
echo "Content:"
curl -s "$BASE_URL/$STDIN_PASTE_ID"
echo ""

# Test 11: Delete a paste
echo ""
echo "🗑️ Test 11: Deleting a paste"
curl -s -X DELETE "$BASE_URL/$STDIN_PASTE_ID"
echo ""
echo "Trying to access deleted paste (should return 404):"
curl -s "$BASE_URL/$STDIN_PASTE_ID" || echo "Paste not found (as expected)"

# Clean up test files
rm -f test_file.txt test.json test.md test.py

echo ""
echo "✅ All tests completed!"
echo ""
echo "💡 You can now use the paste functionality like this:"
echo "   # Paste a file:"
echo "   curl --data-binary @filename.txt $BASE_URL"
echo ""
echo "   # Paste from stdin:"
echo "   echo 'Hello world' | curl --data-binary @- $BASE_URL"
echo ""
echo "   # Add this function to your .bashrc for easy pasting:"
echo "   function paste() {"
echo "       local file=\${1:-/dev/stdin}"
echo "       curl --data-binary @\${file} $BASE_URL"
echo "   }"
