import * as pulumi from "@pulumi/pulumi";
import * as awsx from "@pulumi/awsx";

// Create a container repository.
const repo = new awsx.ecr.Repository("tasks-repo");

// build and push the image to the repository
const image = repo.buildAndPushImage("../")

// Create a load balancer to listen for requests and route them to the container.
const listener = new awsx.elasticloadbalancingv2.NetworkListener("tasks", { port: 80 });

// Create an ECS Cluster
const cluster = new awsx.ecs.Cluster("tasks-cluster");

// Define the service, building and publishing our image, and using the load balancer.
const service = new awsx.ecs.FargateService("tasks", {
    cluster,
    taskDefinitionArgs: {
        containers: {
            tasks: {
                image,
                memory: 512,
                portMappings: [listener],
            },
        },
    },
});

// Export the URL so we can easily access it.
export const frontendURL = pulumi.interpolate `http://${listener.endpoint.hostname}/`;