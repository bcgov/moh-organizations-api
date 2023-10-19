locals {
  #  tfc_hostname     = "app.terraform.io"
  #  tfc_organization = "bcgov"
  #  project          = get_env("LICENSE_PLATE")
  #  environment      = reverse(split("/", get_terragrunt_dir()))[0]
  #  app_image        = "003664705641.dkr.ecr.ca-central-1.amazonaws.com/ssp/sha256:36bf8b4342d35f4aa242e0d440f6edcfbed98bfe703a8a35d5f745f674135788"
  #  app_image        = "003664705641.dkr.ecr.ca-central-1.amazonaws.com/ssp:e7757cf7bf4bb3c593a172ef77d1c19186433ac6"
  app_image = "003664705641.dkr.ecr.ca-central-1.amazonaws.com/ssp:version1.0.3"
}

variable "environment" {
  description = "The workload account environment (e.g. dev, test, prod)"
  default     = "Test"
}

terraform {
  backend "s3" {
    bucket         = "terraform-remote-state-cey5wq-test"
    key            = "org-api-test-tfstate"
    region         = "ca-central-1"
    dynamodb_table = "terraform-remote-state-lock-cey5wq"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region
  assume_role {
    # Copied from IAM console
    role_arn = "arn:aws:iam::787619965950:role/BCGOV_test_Automation_Admin_Role"
    # role_arn = "arn:aws:iam::$${var.target_aws_account_id}:role/BCGOV_$${var.target_env}_Automation_Admin_Role"
  }
}

variable "aws_region" {
  description = "The AWS region things are created in"
  default     = "ca-central-1"
}

module "containers" {
  source                   = "./infrastructure"
  target_env               = var.environment
  target_aws_account_id    = "787619965950"
  cloudfront               = true
  cloudfront_origin_domain = "organizations-api.cey5wq-test.nimbus.cloud.gov.bc.ca"
  service_names            = ["organizations-api"]
  app_image                = local.app_image
}