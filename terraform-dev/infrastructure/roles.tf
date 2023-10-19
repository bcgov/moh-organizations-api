data "aws_caller_identity" "current" {}
# ECS task execution role data
data "aws_iam_policy_document" "ecs_task_execution_role" {
  version = "2012-10-17"
  statement {
    sid     = ""
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}
# ECS task execution role
resource "aws_iam_role" "ecs_task_execution_role" {
  name               = var.ecs_task_execution_role_name
  assume_role_policy = data.aws_iam_policy_document.ecs_task_execution_role.json

  tags = local.common_tags

  inline_policy {
    name = "ecs_task_execution_cwlogs"
    policy = jsonencode(
      {
        Statement = [
          {
            Action = [
              "logs:CreateLogGroup",
            ]
            Effect = "Allow"
            Resource = [
              "arn:aws:logs:*:*:*",
            ]
          },
        ]
        Version = "2012-10-17"
      }
    )
  }
}

# ECS task execution role policy attachment
resource "aws_iam_role_policy_attachment" "ecs_task_execution_role" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy" "ecs_task_execution_cwlogs" {
  name = "ecs_task_execution_cwlogs"
  role = aws_iam_role.ecs_task_execution_role.id

  policy = <<-EOF
  {
      "Version": "2012-10-17",
      "Statement": [
          {
              "Effect": "Allow",
              "Action": [
                  "logs:CreateLogGroup"
              ],
              "Resource": [
                  "arn:aws:logs:*:*:*"
              ]
          }
      ]
  }
EOF
}

resource "aws_iam_role" "organizations_api_container_role" {
  name = "organizations_api_container_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "ecs-tasks.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF

  tags = local.common_tags

  inline_policy {
    name = "dynamodb_organizations_table_role_policy"
    policy = jsonencode(
      {
        Statement = [
          {
            Action = [
              "dynamodb:BatchGet*",
              "dynamodb:DescribeStream",
              "dynamodb:DescribeTable",
              "dynamodb:Get*",
              "dynamodb:Query",
              "dynamodb:Scan",
              "dynamodb:BatchWrite*",
              "dynamodb:CreateTable",
              "dynamodb:Delete*",
              "dynamodb:Update*",
              "dynamodb:PutItem",
            ]
            Effect   = "Allow"
            Resource = "arn:aws:dynamodb:ca-central-1:750307557100:table/Organization"
          },
        ]
        Version = "2012-10-17"
      }
    )
  }

  inline_policy {
    name = "organizations_api_container_cwlogs"
    policy = jsonencode(
      {
        Statement = [
          {
            Action = [
              "logs:CreateLogGroup",
              "logs:CreateLogStream",
              "logs:PutLogEvents",
              "logs:DescribeLogStreams",
            ]
            Effect = "Allow"
            Resource = [
              "arn:aws:logs:*:*:*",
            ]
          },
        ]
        Version = "2012-10-17"
      }
    )
  }
  inline_policy {
    name = "upload_bucket_policy"
    policy = jsonencode(
      {
        Statement = [
          {
            Action = [
              "s3:PutObject",
              "s3:GetObject",
              "kms:Decrypt",
              "kms:Encrypt",
              "s3:PutBucketCORS",
            ]
            Effect = "Allow"
            Resource = [
              "arn:aws:s3:::upload-bucket-decent-duck",
              "arn:aws:s3:::upload-bucket-decent-duck/*",
              "arn:aws:kms:*:750307557100:key/*",
            ]
          },
        ]
        Version = "2012-10-17"
      }
    )
  }

}

resource "aws_iam_role_policy" "organizations_api_container_cwlogs" {
  name = "organizations_api_container_cwlogs"
  role = aws_iam_role.organizations_api_container_role.id

  policy = <<-EOF
  {
      "Version": "2012-10-17",
      "Statement": [
          {
              "Effect": "Allow",
              "Action": [
                  "logs:CreateLogGroup",
                  "logs:CreateLogStream",
                  "logs:PutLogEvents",
                  "logs:DescribeLogStreams"
              ],
              "Resource": [
                  "arn:aws:logs:*:*:*"
              ]
          }
      ]
  }
EOF
}
resource "aws_iam_role_policy" "ssp_bucket_policy" {
  name   = "upload_bucket_policy"
  role   = aws_iam_role.organizations_api_container_role.id
  policy = <<-EOF
  {
    "Version": "2012-10-17",
    "Statement": [
      {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "kms:Decrypt",
                "kms:Encrypt",
                "s3:PutBucketCORS"
            ],
            "Resource": [
                "${aws_s3_bucket.upload_bucket.arn}",
                "${aws_s3_bucket.upload_bucket.arn}/*",
                "arn:aws:kms:*:${data.aws_caller_identity.current.account_id}:key/*"
            ]
        }
        
    ]
  }
  EOF
}


resource "aws_iam_role_policy" "dynamodb_organizations_table_role_policy" {
  name = "dynamodb_organizations_table_role_policy"
  role = aws_iam_role.organizations_api_container_role.id

  policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Statement" : [
        {
          "Effect" : "Allow",
          "Action" : [
            "dynamodb:BatchGet*",
            "dynamodb:DescribeStream",
            "dynamodb:DescribeTable",
            "dynamodb:Get*",
            "dynamodb:Query",
            "dynamodb:Scan",
            "dynamodb:BatchWrite*",
            "dynamodb:CreateTable",
            "dynamodb:Delete*",
            "dynamodb:Update*",
            "dynamodb:PutItem"
          ],
          "Resource" : "${aws_dynamodb_table.organization_table.arn}"
        }
      ]
    }
  )
}
