
resource "aws_budgets_budget" "cost" {

  name              = "organization-api-monthly"
  budget_type       = "COST"
  limit_amount      = var.budget_amount
  limit_unit        = "USD"
  time_unit         = "MONTHLY"
  time_period_start = formatdate("YYYY-MM-01_12:00", timestamp())

  notification {
    comparison_operator       = "GREATER_THAN"
    threshold                 = 75
    threshold_type            = "PERCENTAGE"
    notification_type         = "FORECASTED"
    subscriber_sns_topic_arns = [aws_sns_topic.billing_alert_topic.arn]
    subscriber_email_addresses = ["nathaniel.coster@cgi.com","adam.hoplock@cgi.com","david.a.sharpe@cgi.com","filip.florek@cgi.com"]
  }

  cost_filters = {
    TagKeyValue = "user:Project$Organizations Api"
  }
}


resource "aws_sns_topic" "billing_alert_topic" {
  name = "startup-sample-billing-alert-topic"
}
