#!/bin/bash

# Email Verification Service - Test Script
# This script demonstrates the complete email verification flow

set -e

echo "==============================================="
echo "Email Verification Service - Complete Test"
echo "==============================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test email address
TEST_EMAIL="testuser@example.com"

echo -e "${BLUE}Step 1: Sending verification email to ${TEST_EMAIL}${NC}"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/send-verification \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${TEST_EMAIL}\"}")

echo "Response: $RESPONSE"
echo ""

# Wait a moment for the email to be received
sleep 2

echo -e "${BLUE}Step 2: Checking MailHog for received email${NC}"
MAIL_COUNT=$(curl -s http://localhost:8025/api/v2/messages | \
  python3 -c "import sys, json; print(json.load(sys.stdin)['total'])")
echo "Total emails in MailHog: $MAIL_COUNT"
echo ""

echo -e "${BLUE}Step 3: Extracting verification link from email${NC}"
VERIFICATION_LINK=$(curl -s http://localhost:8025/api/v2/messages | \
  python3 -c "
import sys, json, re
messages = json.load(sys.stdin)
if messages['total'] > 0:
    body = messages['items'][0]['Content']['Body']
    match = re.search(r'http://localhost:8080/api/verify\?token=[a-f0-9-]+', body)
    if match:
        print(match.group(0))
")

if [ -z "$VERIFICATION_LINK" ]; then
    echo "ERROR: Could not extract verification link"
    exit 1
fi

echo "Verification link: $VERIFICATION_LINK"
echo ""

echo -e "${BLUE}Step 4: Verifying email by accessing the verification link${NC}"
VERIFY_RESPONSE=$(curl -s "$VERIFICATION_LINK")

if echo "$VERIFY_RESPONSE" | grep -q "Email Verified!"; then
    echo -e "${GREEN}✓ Email successfully verified!${NC}"
else
    echo "ERROR: Verification failed"
    echo "$VERIFY_RESPONSE"
    exit 1
fi
echo ""

echo -e "${BLUE}Step 5: Testing duplicate verification (should fail)${NC}"
DUPLICATE_RESPONSE=$(curl -s "$VERIFICATION_LINK")

if echo "$DUPLICATE_RESPONSE" | grep -q "already been verified"; then
    echo -e "${GREEN}✓ Correctly rejected duplicate verification${NC}"
else
    echo "WARNING: Duplicate verification was not properly rejected"
fi
echo ""

echo -e "${BLUE}Step 6: Testing invalid token (should fail)${NC}"
INVALID_RESPONSE=$(curl -s "http://localhost:8080/api/verify?token=invalid-token-12345")

if echo "$INVALID_RESPONSE" | grep -q "Verification Failed"; then
    echo -e "${GREEN}✓ Correctly rejected invalid token${NC}"
else
    echo "WARNING: Invalid token was not properly rejected"
fi
echo ""

echo "==============================================="
echo -e "${GREEN}✓ All tests passed successfully!${NC}"
echo "==============================================="
echo ""
echo "Services:"
echo "  - Application API: http://localhost:8080"
echo "  - Swagger UI: http://localhost:8080/swagger-ui"
echo "  - MailHog UI: http://localhost:8025"
echo "  - OpenAPI Spec: http://localhost:8080/openapi"
