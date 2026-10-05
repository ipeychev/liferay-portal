resource "kubernetes_config_map_v1" "fixture_exports" {
	data={
		retain="5"
	}
	metadata {
		name="fixture-exports"
		namespace=var.fixture_namespace
	}
	lifecycle {
		ignore_changes=[data]
	}
}
variable "fixture_namespace" {
	default="pr-reviewer-fixture"
	description="The namespace that holds the fixture exports."
	type=string
}