import * as pulumi from "@pulumi/pulumi";
import * as awsx from "@pulumi/awsx";

// Create a container repository.
const repo = new awsx.ecr.Repository("tasks-repo");
// build and push the image to the repository
const image = repo.buildAndPushImage("../")

// Create a load balancer to listen for requests and route them to the container. The network LB will be created under the default VPC
const networkLoadBalancer = new awsx.lb.NetworkLoadBalancer("tasks", { external: true });
const target = networkLoadBalancer.createTargetGroup("tasks", { port: 8080 }); // app port
const listener = target.createListener("tasks", { port: 80 }); // exposed port


// Create an ECS Cluster. This cluster will be
// associated with the default VPC for the region.  To override that, pass in a VPC manually.
const cluster = new awsx.ecs.Cluster("tasks-cluster");

// Define the service, building and publishing our image, and using the load balancer.
const service = new awsx.ecs.FargateService("tasks", {
    cluster, // if not sent, pulumi will know that a cluster is required, and it will create one for it
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