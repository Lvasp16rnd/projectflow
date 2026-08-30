#!/bin/bash
set -euo pipefail

echo "Creating SQS dead-letter queue: request-events-dlq"
awslocal sqs create-queue --queue-name request-events-dlq

DLQ_ARN=$(awslocal sqs get-queue-attributes \
  --queue-name request-events-dlq \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' \
  --output text)

echo "DLQ ARN: ${DLQ_ARN}"

REDRIVE_POLICY="{\\\"deadLetterTargetArn\\\":\\\"${DLQ_ARN}\\\",\\\"maxReceiveCount\\\":\\\"3\\\"}"

echo "Creating SQS queue: request-events (redrive -> request-events-dlq, maxReceiveCount=3)"
awslocal sqs create-queue \
  --queue-name request-events \
  --attributes "{\"RedrivePolicy\":\"${REDRIVE_POLICY}\"}"

echo "SQS initialization complete."
