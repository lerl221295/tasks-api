import * as pulumi from "@pulumi/pulumi";
import * as awsx from "@pulumi/awsx";
import * as aws from "@pulumi/aws"

// Create a container repository.
const repo = new awsx.ecr.Repository("tasks-repo", {
    repository: new aws.ecr.Repository("tasks-repo", { name: "tasks-repo", forceDelete: true })
});
export const imageUri = pulumi.interpolate `${repo.repository.repositoryUrl}:latest`
// if we want to build and push the image to the repository
// const image = repo.buildAndPushImage("../") // use instead of imageUri

// Create a load balancer to listen for requests and route them to the container. The network LB will be created under the default VPC
// If desired, a custom VPC can be created and used bellow to create the NLB
const networkLoadBalancer = new awsx.lb.NetworkLoadBalancer("tasks", { external: true });
const target = networkLoadBalancer.createTargetGroup("tasks", { port: 8080 }); // app port
const listener = target.createListener("tasks", { port: 80 }); // exposed port


// Create an ECS Cluster. This cluster will be associated with the default VPC for the region.
// To override that, pass in a VPC manually.
const cluster = new awsx.ecs.Cluster("tasks-cluster", { name: "tasks-cluster" });

//task definition using the Docker image built, and the LB created
const taskDefinition = new awsx.ecs.FargateTaskDefinition("tasks-definition", {
    containers: {
        tasks: {
            image: imageUri,
            memory: 512,
            portMappings: [listener],
        }
    },
})

// Define the service using the cluster and the task definition
const service = new awsx.ecs.FargateService("tasks", {
    name: "tasks-service",
    cluster, // if not sent, pulumi will know that a cluster is required, and it will create one for it
    taskDefinition, // either this, or taskDefinitionArgs object to create the task definition here
});

const bucketName = "lerl221295-tasks-images" // it has to be unique
const bucket = new aws.s3.Bucket("tasks-images", {
    bucket: bucketName,
    forceDestroy: true,
    policy: {
        Version: "2012-10-17",
        Statement: [{
            Effect: "Allow",
            Principal: "*",
            Action: ["s3:GetObject"],
            Resource: [`arn:aws:s3:::${bucketName}/*`] // policy refers to bucket name explicitly
        }]
    }
});


// OUTPUT
export const frontendURL = pulumi.interpolate `http://${listener.endpoint.hostname}/`;
export const taskDefinitionArn = taskDefinition.taskDefinition.arn
export const bucketDomainName = bucket.bucketDomainName