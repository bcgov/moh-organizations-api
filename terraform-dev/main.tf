locals {
  #  tfc_hostname     = "app.terraform.io"
  #  tfc_organization = "bcgov"
  #  project          = get_env("LICENSE_PLATE")
  #  environment      = reverse(split("/", get_terragrunt_dir()))[0]
  #  app_image        = "646101433301.dkr.ecr.ca-central-1.amazonaws.com/ssp/sha256:36bf8b4342d35f4aa242e0d440f6edcfbed98bfe703a8a35d5f745f674135788"
  #  app_image        = "646101433301.dkr.ecr.ca-central-1.amazonaws.com/ssp:e7757cf7bf4bb3c593a172ef77d1c19186433ac6"
  app_image = "646101433301.dkr.ecr.ca-central-1.amazonaws.com/ssp:version1.0.0"
}

variable "environment" {
  description = "The workload account environment (e.g. dev, test, prod)"
  default     = "Dev"
}

terraform {
  backend "remote" {
    hostname     = "app.terraform.io"
    organization = "bcgov"
    workspaces {
      name = "cey5wq-dev"
    }
  }
}

provider "aws" {
  region = var.aws_region
  assume_role {
    # Copied from IAM console
    role_arn = "arn:aws:iam::750307557100:role/BCGOV_dev_Automation_Admin_Role"
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
  target_aws_account_id    = "750307557100"
  cloudfront               = true
  cloudfront_origin_domain = "organizations-api.cey5wq-dev.nimbus.cloud.gov.bc.ca"
  service_names            = ["organizations-api"]
  app_image                = local.app_image
}