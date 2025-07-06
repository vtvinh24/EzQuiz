#!/bin/bash

# EzQuiz Backend API Test Script
# This script tests all the main API endpoints

BASE_URL="http://localhost:3000"
echo "🧪 Testing EzQuiz Backend API Endpoints..."
echo "Base URL: $BASE_URL"
echo

# Test 1: Health Check
echo "1. Testing Health Check..."
curl -s -X GET "$BASE_URL/health" | jq .
echo

# Test 2: Admin Login
echo "2. Testing Admin Login..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')
echo "$LOGIN_RESPONSE" | jq .

# Extract JWT token
JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.token')
echo "JWT Token: $JWT_TOKEN"
echo

# Test 3: Generate License Keys
echo "3. Testing License Generation..."
curl -s -X POST "$BASE_URL/api/v1/license/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"amount":2,"maxActivations":3,"expiresInDays":30}' | jq .
echo

# Test 4: List License Keys
echo "4. Testing License List..."
curl -s -X GET "$BASE_URL/api/v1/license/list" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq .
echo

# Test 5: Validate License Key (we'll use a dummy key)
echo "5. Testing License Validation..."
curl -s -X POST "$BASE_URL/api/v1/license/validate" \
  -H "Content-Type: application/json" \
  -d '{"key":"DUMMY-KEY-TEST-1234","deviceInfo":{"platform":"Linux","version":"Ubuntu 22.04"},"installId":"test-install-123"}' | jq .
echo

# Test 6: Payment Plans
echo "6. Testing Payment Plans..."
curl -s -X GET "$BASE_URL/api/v1/payment/plans" | jq .
echo

# Test 7: Token Verification
echo "7. Testing Token Verification..."
curl -s -X GET "$BASE_URL/api/v1/auth/verify" \
  -H "Authorization: Bearer $JWT_TOKEN" | jq .
echo

# Test 8: Quiz Generation (will fail without API key)
echo "8. Testing Quiz Generation (will fail without Gemini API key)..."
curl -s -X POST "$BASE_URL/api/v1/generate/generate-quiz" \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Create 1 simple question about Node.js"}' | jq .
echo

echo "✅ API Testing Complete!"
echo "Note: Quiz generation requires a valid Gemini API key in .env file"
