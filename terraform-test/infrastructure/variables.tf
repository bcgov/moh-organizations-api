# variables.tf

variable "target_env" {
  description = "AWS workload account env (e.g. dev, test, prod, sandbox, unclass)"
}

variable "target_aws_account_id" {
  description = "AWS workload account id"
}

variable "aws_region" {
  description = "The AWS region things are created in"
  default     = "ca-central-1"
}

variable "ecs_task_execution_role_name" {
  description = "ECS task execution role name"
  default     = "organizationsApiEcsTaskExecutionRole"
}

variable "ecs_auto_scale_role_name" {
  description = "ECS auto scale role Name"
  default     = "organizationsApiEcsAutoScaleRole"
}

variable "az_count" {
  description = "Number of AZs to cover in a given region"
  default     = "2"
}

variable "app_name" {
  description = "Name of the application"
  type        = string
  default     = "organizations-api"
}

variable "app_image" {
  description = "Docker image to run in the ECS cluster. _Note_: there is a blank default value, which will cause service and task resource creation to be supressed unless an image is specified."
  type        = string
  default     = ""
}

variable "app_port" {
  description = "Port exposed by the docker image to redirect traffic to"
  default     = 80
}

variable "app_count" {
  description = "Number of docker containers to run"
  default     = 1
}

variable "container_name" {
  description = "Container name"
  default     = "organizations-api-container"
}

variable "health_check_path" {
  ## TODO: Document this
  default = "/health"
  ## END TODO
#  default = "/"
}

variable "fargate_cpu" {
  description = "Fargate instance CPU units to provision (1 vCPU = 1024 CPU units)"
  default     = 512
}

variable "fargate_memory" {
  description = "Fargate instance memory to provision (in MiB)"
  default     = 1024
}

variable "db_name" {
  description = "DynamoDB DB Name"
  default     = "organizations-api-db"
}

variable "repository_name" {
  description = "Name for the container repository to be provisioned."
  type        = string
  default     = "oap"
}

variable "budget_amount" {
  description = "The amount of spend for the budget. Example: enter 100 to represent $100"
  default     = "100.0"
}

variable "budget_tag" {
  description = "The Cost Allocation Tag that will be used to build the monthly budget. "
  default     = "Project=Organizations Api"
}

variable "common_tags" {
  description = "Common tags for created resources"
  default = {
    Application = "Organizations Api"
  }
}

variable "service_names" {
  description = "List of service names to use as subdomains"
  default     = ["startup-sample-project", "ssp"]
  type        = list(string)
}

variable "alb_name" {
  description = "Name of the internal alb"
  default     = "default"
  type        = string
}

variable "cloudfront" {
  description = "enable or disable the cloudfront distrabution creation"
  type        = bool
}

variable "cloudfront_origin_domain" {
  description = "domain name of the ssp"
  type        = string
}

/*variable "cf_origin_id" {
  description = "id"
  type        = string
}*/